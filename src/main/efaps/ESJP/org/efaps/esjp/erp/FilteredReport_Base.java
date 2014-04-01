/*
 * Copyright 2003 - 2014 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.erp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.ui.wicket.util.FilterDefault;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("fc64ff47-d1f6-4aed-8d7d-2a9128a51a19")
@EFapsRevision("$Rev$")
public abstract class FilteredReport_Base
    extends AbstractCommon
{

    public final static String KEY4SESSION = FilteredReport.class.getName() + ".Key4SessionContext";


    public Return setDefaultFilter(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Object> filterMap = getFilterMap(_parameter);
        if (filterMap.isEmpty()) {
            final Map<Integer, String> fields = analyseProperty(_parameter, "Field");
            final Map<Integer, String> types = analyseProperty(_parameter, "FilterType");
            final Map<Integer, String> defaults = analyseProperty(_parameter, "FilterDefault");

            for (final Entry<Integer, String> field : fields.entrySet()) {
                filterMap.put(field.getValue(),
                                getDefaultValue(_parameter, types.get(field.getKey()), defaults.get(field.getKey())));
            }
        }
        return new Return();
    }

    protected Object getDefaultValue(final Parameter _parameter,
                                     final String _type,
                                     final String _default)
    {
        Object ret = null;
        if ("DateTime".equalsIgnoreCase(_type)) {
            final String[] value = _default.split(":");
            final int move = value.length > 1 ? Integer.parseInt(value[1]) : 0;

            final FilterDefault def = FilterDefault.valueOf(value[0].toUpperCase());
            DateTime tmp = new DateTime().withTimeAtStartOfDay();
            switch (def) {
                case TODAY:
                    tmp = tmp.plusDays(move);
                    break;
                case WEEK:
                    tmp = tmp.plusWeeks(move);
                    break;
                case MONTH:
                    tmp = tmp.plusMonths(move);
                    break;
                case YEAR:
                    tmp = tmp.plusYears(move);
                    break;
                case ALL:
                    break;
                case NONE:
                    break;
                default:
                    break;
            }
            ret = tmp;
        } else {
            //TODO
        }
        return ret;
    }


    public Return getDateFromFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = fieldValue.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        if (!map.containsKey(key)) {
            map.put(key, new DateTime());
        }
        ret.put(ReturnValues.VALUES, map.get(key));
        return ret;
    }

    public Return getDateToFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = fieldValue.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        if (!map.containsKey(key)) {
            map.put(key, new DateTime());
        }
        ret.put(ReturnValues.VALUES, map.get(key));
        return ret;
    }


    protected Map<String, Object> getFilterMap(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Map<String, Object>> map = getCtxMap(_parameter);
        final String filterKey = getFilterKey(_parameter);
        Map<String, Object> ret = map.get(filterKey);
        if (ret == null) {
            ret = new HashMap<String, Object>();
            map.put(filterKey, ret);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, Object>> getCtxMap(final Parameter _parameter)
        throws EFapsException
    {
        Map<String, Map<String, Object>> map;
        if (Context.getThreadContext().containsSessionAttribute(FilteredReport_Base.KEY4SESSION)) {
            map = (Map<String, Map<String, Object>>) Context.getThreadContext().getSessionAttribute(
                            FilteredReport_Base.KEY4SESSION);
        } else {
            map = new HashMap<String, Map<String, Object>>();
            Context.getThreadContext().setSessionAttribute(FilteredReport_Base.KEY4SESSION, map);
        }
        return map;
    }

    protected String getFilterKey(final Parameter _parameter)
        throws EFapsException
    {
        String ret = getProperty(_parameter, "FilterKey");
        if (ret == null) {
            final Object callCmd = _parameter.get(ParameterValues.CALL_CMD);
            if (callCmd != null && callCmd instanceof AbstractCommand) {
                final AbstractCommand cmd = (AbstractCommand) callCmd;
                ret = cmd.getProperty("FilterKey");
            }
        }
        return ret;
    }

    public Return setFilter(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Map<String, Object>> map = getCtxMap(_parameter);
        final String filterKey = getFilterKey(_parameter);
        final Map<String, Object> filter = new HashMap<String, Object>();
        map.put(filterKey, filter);
        final AbstractCommand cmd = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Form form = cmd.getTargetForm();
        for (final Field field : form.getFields()) {
            final Object obj;
            final String val = _parameter.getParameterValue(field.getName());
            final IUIProvider uiProvider = field.getUIProvider();
            if (uiProvider instanceof DateTimeUI || uiProvider instanceof DateUI) {
                obj = new DateTime(val);
            } else {
                obj = val;
            }
            filter.put(field.getName(), obj);
        }
        return new Return();
    }

    public Return getFilterFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();

        final Map<String, Map<String, Object>> map = getCtxMap(_parameter);
        final String filterKey = getFilterKey(_parameter);
        if (map.containsKey(filterKey)) {
            final Map<Integer, String> fields = analyseProperty(_parameter, "Field");
            boolean first = true;
            if (fields.isEmpty()) {
                for (final Entry<String, Object> entry : map.get(filterKey).entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        html.append("<br/>");
                    }
                    html.append(entry.getKey()).append(": ").append(entry.getValue());
                }
            } else {
                final Map<String, Object> filters = map.get(filterKey);
                final Map<Integer, String> dBProperties = analyseProperty(_parameter, "DBProperty");
                for (final Entry<Integer, String> entry : fields.entrySet()) {
                    String value = "-";
                    final Object valueTmp = filters.get(entry.getValue());
                    if (valueTmp != null) {
                        if (valueTmp instanceof DateTime) {
                            value = ((DateTime)valueTmp).toString("dd/MM/yyyy", Context.getThreadContext().getLocale());
                        } else {
                            value = valueTmp.toString();
                        }
                    }
                    if (first) {
                        first = false;
                    } else {
                        html.append("<br/>");
                    }
                    html.append(DBProperties.getProperty(dBProperties.get(entry.getKey()))).append(": ").append(value);
                }
            }
        }
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }
}
