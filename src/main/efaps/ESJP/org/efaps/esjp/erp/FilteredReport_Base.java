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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.esjp.common.uiform.Field_Base.ListType;
import org.efaps.ui.wicket.models.objects.UIForm;
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
    /**
     * Key used to store the filters in the context.
     */
    public static final String KEY4SESSION = FilteredReport.class.getName() + ".Key4SessionContext";

    /**
     * Set the default filter in the context map. Must be called
     * before any other access to work.
     * @param _parameter Parameter as passed by the eFasp API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return setDefaultFilter(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Object> filterMap = getFilterMap(_parameter);
        if (filterMap.isEmpty()) {
            final Map<Integer, String> fields = analyseProperty(_parameter, "Field");
            final Map<Integer, String> types = analyseProperty(_parameter, "FilterType");
            final Map<Integer, String> defaults = analyseProperty(_parameter, "FilterDefault");

            for (final Entry<Integer, String> field : fields.entrySet()) {
                filterMap.put(field.getValue(), getDefaultValue(_parameter, field.getValue(),
                                types.get(field.getKey()), defaults.get(field.getKey())));
            }
        }
        return new Return();
    }

    /**
     * Evaluate the properties of the event to get an default value.
     * @param _parameter Parameter as passed by the eFasp API
     * @param _type   type of filter
     * @param _default default value
     * @return default value for the context map
     */
    protected Object getDefaultValue(final Parameter _parameter,
                                     final String _field,
                                     final String _type,
                                     final String _default) throws EFapsException
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
        } else if ("Type".equalsIgnoreCase(_type)) {
            if (isUUID(_default)) {
                ret = new TypeFilterValue().setObject(Type.get(UUID.fromString(_default)).getId());
            } else {
                ret = new TypeFilterValue().setObject(Type.get(_default).getId());
            }
        } else if ("Instance".equalsIgnoreCase(_type)) {
            ret = new ContactFilterValue().setObject(Instance.get(_default));
        }
        return ret;
    }

    /**
     * Get the fieldvalue for the from dateField.
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
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

    /**
     * Get the fieldvalue for the from contact.
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
    public Return getContactFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = fieldValue.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        if (!map.containsKey(key)) {
            map.put(key, "");
        }
        ret.put(ReturnValues.VALUES, map.get(key));
        return ret;
    }

    /**
     * Get the fieldvalue for the to dateField.
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
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

    public Return getTypeFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final Command cmd = (Command) _parameter.get(ParameterValues.CALL_CMD);
        final String formStr = cmd.getProperty("FilterTargetForm");
        final String fieldStr = cmd.getProperty("FilterTargetField");

        final Form form = isUUID(formStr) ? Form.get(UUID.fromString(formStr)) : Form.get(formStr);
        final Field field = form.getField(fieldStr);
        final EventDefinition event = field.getEvents(EventType.UI_FIELD_VALUE).get(0);

        final List<DropDownPosition> values = new ArrayList<DropDownPosition>();
        if (event.getProperty("Type") != null) {
            final Type type = isUUID(event.getProperty("Type")) ? Type.get(UUID.fromString(event.getProperty("Type")))
                            : Type.get(event.getProperty("Type"));
            values.add(new DropDownPosition(type.getId(), type.getLabel()));
        }
        for (int i = 1; i < 100; i++) {
            final String nameTmp = "Type" + String.format("%02d", i);
            if (event.getProperty(nameTmp) != null) {
                final Type type = isUUID(event.getProperty(nameTmp)) ? Type.get(UUID.fromString(event
                                .getProperty(nameTmp)))
                                : Type.get(event.getProperty(nameTmp));
                values.add(new DropDownPosition(type.getId(), type.getLabel()));
            } else {
                break;
            }

            final String key = fieldValue.getField().getName();
            final Map<String, Object> map = getFilterMap(_parameter);
            if (!map.containsKey(key)) {
                map.put(key, new TypeFilterValue().setObject((Long) values.get(0).getValue()));
            }
            final FilterValue<?> selected = (FilterValue<?>) map.get(key);
            for (final DropDownPosition pos : values) {
                pos.setSelected(pos.getValue().equals(selected.getObject()));
            }
        }
        return ret.put(ReturnValues.SNIPLETT,
                        new org.efaps.esjp.common.uiform.Field().getInputField(_parameter, values, ListType.RADIO));
    }

    /**
     * Get the filter map from the context.
     * @param _parameter Parameter as passed by the eFaps API
     * @return the mpa from the context
     * @throws EFapsException on error
     */
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




    /**
     * Get the basic map from the context.
     * @param _parameter Parameter as passed by the eFaps API
     * @return the map from the context
     * @throws EFapsException on error
     */
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

    /**
     * Get the filterkey used for storing the map in the context.
     * @param _parameter Parameter as passed by the eFaps API
     * @return the filterkey
     * @throws EFapsException on error
     */
    protected String getFilterKey(final Parameter _parameter)
        throws EFapsException
    {
        String ret = getProperty(_parameter, "FilterKey");
        if (ret == null) {
            final Object callCmd = _parameter.get(ParameterValues.CALL_CMD);
            if (callCmd instanceof AbstractCommand) {
                final AbstractCommand cmd = (AbstractCommand) callCmd;
                ret = cmd.getProperty("FilterKey");
            } else {
                final Object uiForm = _parameter.get(ParameterValues.CLASS);
                if (uiForm instanceof UIForm) {
                    final AbstractCommand cmd = ((UIForm) uiForm).getCommand();
                    ret = cmd.getProperty("FilterKey");
                }
            }
        }
        return ret;
    }

    /**
     * Set the filter in the Context. Executed on an execute event.
     * @param _parameter Parameter as passed by the eFaps API
     * @return new empty return
     * @throws EFapsException on error
     */
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
            filter.put(field.getName(), getFilterValue(_parameter, field));
        }
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _field field teh valu eis wanted for
     * @return object
     */
    protected Object getFilterValue(final Parameter _parameter,
                                    final Field _field)
    {
        final Object obj;
        final String val = _parameter.getParameterValue(_field.getName());
        final IUIProvider uiProvider = _field.getUIProvider();
        if (uiProvider instanceof DateTimeUI || uiProvider instanceof DateUI) {
            obj = new DateTime(val);
        } else if ("type".equals(_field.getName())) {
            obj = new TypeFilterValue().setObject(Long.valueOf(val));
        } else if ("contact".equals(_field.getName())) {
            obj = new ContactFilterValue().setObject(Instance.get(val));
        } else {
            obj = val;
        }
        return obj;
    }

    /**
     * Get a fieldvalue that show the current selected filter.
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing html snipplet
     * @throws EFapsException on error
     */
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
                            value = ((DateTime) valueTmp).toString("dd/MM/yyyy",
                                            Context.getThreadContext().getLocale());
                        } else if (valueTmp instanceof FilterValue) {
                            value = ((FilterValue<?>) valueTmp).getLabel();
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


    public static abstract class FilterValue<T>
    {

        private T object;

        public String getLabel()
            throws EFapsException
        {
            return this.object.toString();
        }

        /**
         * Getter method for the instance variable {@link #object}.
         *
         * @return value of instance variable {@link #object}
         */
        public T getObject()
        {
            return this.object;
        }

        /**
         * Setter method for instance variable {@link #object}.
         *
         * @param _object value for instance variable {@link #object}
         */
        public FilterValue<T> setObject(final T _object)
        {
            this.object = _object;
            return this;
        }
    }

    public static class TypeFilterValue
        extends FilterValue<Long>
    {
        @Override
        public String getLabel()
            throws EFapsException
        {
            return Type.get(getObject()).getLabel();
        }
    }

    public static class ContactFilterValue
    extends FilterValue<Instance>
{
    @Override
    public String getLabel()
        throws EFapsException
    {
        String ret;
        if (getObject().isValid()) {
            final PrintQuery print = new PrintQuery(getObject());
            print.addAttribute("Name");
            print.execute();

            ret = print.<String>getAttribute("Name");
        } else {
            ret = "";
        }


        return ret;
    }
}
}
