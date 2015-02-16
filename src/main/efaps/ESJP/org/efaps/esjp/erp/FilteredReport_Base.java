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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.datamodel.ui.UIValue;
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
import org.efaps.api.ui.IOption;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.AbstractCommon_Base;
import org.efaps.esjp.common.datetime.JodaTimeUtils;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.esjp.common.uiform.Field_Base.ListType;
import org.efaps.esjp.ui.html.Table;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: FilteredReport_Base.java 12893 2014-05-27 23:29:01Z
 *          jan@moxter.net $
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
     * Enum used for display.
     */
    public enum GroupDisplay
    {
        /** No display. */
        NONE,
        /** Display as Column. */
        COLUMN,
        /** Display as Group. */
        GROUP;
    }

    /**
     * Set the default filter in the context map. Must be called before any
     * other access to work.
     *
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
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @param _field FieldName
     * @param _type type of filter
     * @param _default default value
     * @return default value for the context map
     * @throws EFapsException on error
     */
    protected Object getDefaultValue(final Parameter _parameter,
                                     final String _field,
                                     final String _type,
                                     final String _default)
        throws EFapsException
    {
        Object ret = null;
        if ("DateTime".equalsIgnoreCase(_type)) {
            final String[] value = _default.split(":");
            final Properties props = new Properties();
            props.setProperty(value[0], value.length > 1 ? value[1] : "");
            ret = JodaTimeUtils.getDefaultvalue(_parameter, props);
        } else if ("Type".equalsIgnoreCase(_type)) {
            final Set<Long> set = new HashSet<>();
            if (isUUID(_default)) {
                set.add(Type.get(UUID.fromString(_default)).getId());
            } else {
                set.add(Type.get(_default).getId());
            }
            ret = new TypeFilterValue().setObject(set);
        } else if ("Instance".equalsIgnoreCase(_type)) {
            ret = new InstanceFilterValue().setObject(Instance.get(_default));
        } else if ("InstanceSet".equalsIgnoreCase(_type)) {
            ret = new InstanceSetFilterValue().setObject(new HashSet<Instance>(Arrays.asList(Instance.get(_default))));
        } else if ("Boolean".equalsIgnoreCase(_type)) {
            ret = BooleanUtils.toBoolean(_default);
        } else if ("Currency".equalsIgnoreCase(_type)) {
            ret = new CurrencyFilterValue().setObject(Instance.get(_default));
        } else if ("Enum".equalsIgnoreCase(_type)) {
            try {
                final Class<?> clazz = Class.forName(_default);
                if (clazz.isEnum()) {
                    final Object[] consts = clazz.getEnumConstants();
                    ret = new EnumFilterValue().setObject((Enum<?>) consts[0]);
                }
            } catch (final ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Get the fieldvalue for the from dateField.
     *
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
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
    public Return getContactFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue value = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = value.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        String val = "";
        if (map.containsKey(key)) {
            final Object obj = map.get(key);
            if (obj instanceof AbstractFilterValue) {
                val = ((AbstractFilterValue<?>) obj).getLabel(_parameter);
                ret.put(ReturnValues.INSTANCE, ((AbstractFilterValue<?>) obj).getObject());
            } else {
                val = obj.toString();
            }
        } else {
            map.put(key, "");
        }
        ret.put(ReturnValues.VALUES, val);
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFasp API
     * @return Return containing the file
     * @throws EFapsException on error
     */
    public Return getCurrencyFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final List<DropDownPosition> values = new ArrayList<>();
        final Map<String, Object> filterMap = getFilterMap(_parameter);
        Object current = null;
        if (filterMap.containsKey("currency")) {
            final CurrencyFilterValue filter = (CurrencyFilterValue) filterMap.get("currency");
            current = filter.getObject();
        }
        for (final CurrencyInst currency : CurrencyInst.getAvailable()) {
            final DropDownPosition dropdown = new DropDownPosition(currency.getInstance().getOid(), currency.getName());
            dropdown.setSelected(currency.getInstance().equals(current));
            values.add(dropdown);
        }
        if (!"false".equalsIgnoreCase(getProperty(_parameter, "ShowEmptyValue"))) {
            values.add(new DropDownPosition("-", "-"));
        }
        if ("true".equalsIgnoreCase(getProperty(_parameter, "ShowBaseCurrency"))) {
            final DropDownPosition dropdown = new DropDownPosition("BASE", DBProperties.getProperty(
                            FilteredReport.class.getName() + ".BaseCurrency"));
            dropdown.setSelected(current != null && "BASE".equals(((Instance) current).getKey()));
            values.add(dropdown);
        }

        Collections.sort(values, new Comparator<DropDownPosition>()
        {

            @Override
            public int compare(final DropDownPosition _o1,
                               final DropDownPosition _o2)
            {
                return String.valueOf(_o1.getOrderValue()).compareTo(String.valueOf(_o2.getOrderValue()));
            }
        });
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT, new org.efaps.esjp.common.uiform.Field().getDropDownField(_parameter, values));
        return ret;
    }

    /**
     * Get the fieldvalue for the from contact.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
    public Return getInstanceSetFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue value = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = value.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);

        final List<IOption> tokens = new ArrayList<IOption>();
        if (map.containsKey(key)) {
            final Object obj = map.get(key);
            if (obj instanceof InstanceSetFilterValue) {
                for (final Instance instance : ((InstanceSetFilterValue) obj).getObject()) {
                    if (instance.isValid()) {
                        tokens.add(new InstanceOption(instance.getOid(), getInstanceLabel(_parameter, instance)));
                    }
                }
            }
        } else {
            map.put(key, "");
        }
        Collections.sort(tokens, new Comparator<IOption>()
        {
            @Override
            public int compare(final IOption _arg0,
                               final IOption _arg1)
            {
                return _arg0.getLabel().compareTo(_arg1.getLabel());
            }
        });
        ret.put(ReturnValues.VALUES, tokens);
        return ret;
    }

    /**
     * Get the fieldvalue for the from contact.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
    public Return getInstanceFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final IUIValue value = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = value.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        String val = "";
        if (map.containsKey(key)) {
            final Object obj = map.get(key);
            if (obj instanceof AbstractFilterValue) {
                val = ((AbstractFilterValue<?>) obj).getLabel(_parameter);
                ret.put(ReturnValues.INSTANCE, ((AbstractFilterValue<?>) obj).getObject());
            } else {
                val = obj.toString();
            }
        } else {
            map.put(key, "");
        }
        ret.put(ReturnValues.VALUES, val);
        return ret;
    }

    /**
     * Get the fieldvalue for the to dateField.
     *
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

    /**
     * Get the fieldvalue for the to dateField.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
    public Return getBooleanFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        final String key;
        if (uiObject instanceof UIValue) {
            final UIValue uiValue = (UIValue) uiObject;
            key = uiValue.getField().getName();
        } else {
            final FieldValue fieldValue = (FieldValue) uiObject;
            key = fieldValue.getField().getName();
        }

        final Map<String, Object> map = getFilterMap(_parameter);
        if (!map.containsKey(key)) {
            map.put(key, new Boolean(true));
        }
        ret.put(ReturnValues.VALUES, map.get(key));
        return ret;
    }

    /**
     * Get the fieldvalue for the to dateField.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return value for the form
     * @throws EFapsException on error
     */
    public Return getEnumFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final List<DropDownPosition> values = new ArrayList<>();
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        final String key;
        if (uiObject instanceof UIValue) {
            final UIValue uiValue = (UIValue) uiObject;
            key = uiValue.getField().getName();
        } else {
            final FieldValue fieldValue = (FieldValue) uiObject;
            key = fieldValue.getField().getName();
        }

        final Map<String, Object> map = getFilterMap(_parameter);
        if (map.containsKey(key)) {
            final EnumFilterValue value = (EnumFilterValue) map.get(key);
            final Object[] consts = value.getObject().getDeclaringClass().getEnumConstants();
            for (final Object obj : consts) {
                final String option = DBProperties.getProperty(obj.getClass().getName() + "." + obj.toString());
                final DropDownPosition pos = new DropDownPosition(obj.toString(), option);
                values.add(pos);
                pos.setSelected(obj.equals(value.getObject()));
            }
        }

        ret.put(ReturnValues.SNIPLETT,
                        new org.efaps.esjp.common.uiform.Field().getInputField(_parameter, values, ListType.RADIO));
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return with SNIPPLET
     * @throws EFapsException on error
     */
    public Return getTypeFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final List<DropDownPosition> values = new ArrayList<>();
        final Map<String, Object> filter = getFilterMap(_parameter);
        final Set<Long> selected = new HashSet<>();
        if (filter.containsKey("type")) {
            final TypeFilterValue filters = (TypeFilterValue) filter.get("type");
            selected.addAll(filters.getObject());
        }
        final List<Type> types = getTypeList(_parameter);
        for (final Type type : types) {
            final DropDownPosition dropdown = new DropDownPosition(type.getId(), type.getLabel());
            dropdown.setSelected(selected.contains(type.getId()));
            values.add(dropdown);
        }
        final Return ret = new Return();
        ret.put(ReturnValues.SNIPLETT,
                        new org.efaps.esjp.common.uiform.Field().getInputField(_parameter, values,
                                    EnumUtils.getEnum(ListType.class, getProperty(_parameter, "ListType", "RADIO"))));
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return list of types
     * @throws EFapsException on error
     */
    protected List<Type> getTypeList(final Parameter _parameter)
        throws EFapsException
    {
        final List<Type> ret = new ArrayList<>();
        final Properties props = getProperties4TypeList(_parameter);

        if (props.containsKey("Type")) {
            final Type type = isUUID(props.getProperty("Type")) ? Type.get(UUID.fromString(props.getProperty("Type")))
                            : Type.get(props.getProperty("Type"));
            ret.add(type);
        }
        int i = 1;
        String nameTmp = "Type" + String.format("%02d", i);
        while (props.getProperty(nameTmp) != null) {
            final Type type = isUUID(props.getProperty(nameTmp)) ? Type.get(UUID.fromString(props
                            .getProperty(nameTmp)))
                            : Type.get(props.getProperty(nameTmp));
            ret.add(type);
            i++;
            nameTmp = "Type" + String.format("%02d", i);
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return Properties
     * @throws EFapsException on error
     */
    protected Properties getProperties4TypeList(final Parameter _parameter)
        throws EFapsException
    {
        final Properties ret;
        final Command cmd = (Command) _parameter.get(ParameterValues.CALL_CMD);
        if (cmd != null && cmd.getProperty("FilterTargetForm") != null) {
            final String formStr = cmd.getProperty("FilterTargetForm");
            final String fieldStr = cmd.getProperty("FilterTargetField");

            final Form form = isUUID(formStr) ? Form.get(UUID.fromString(formStr)) : Form.get(formStr);
            final Field field = form.getField(fieldStr);
            final EventDefinition event = field.getEvents(EventType.UI_FIELD_VALUE).get(0);
            ret = MapUtils.toProperties(event.getPropertyMap());
        } else {
            ret =  MapUtils.toProperties((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES));
        }
        return ret;
    }


    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return status
     * @throws EFapsException on error
     */
    public Return getStatusFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object uiObject = _parameter.get(ParameterValues.UIOBJECT);
        final String key;
        if (uiObject instanceof UIValue) {
            final UIValue uiValue = (UIValue) uiObject;
            key = uiValue.getField().getName();
        } else {
            final FieldValue fieldValue = (FieldValue) uiObject;
            key = fieldValue.getField().getName();
        }

        final Map<String, Object> map = getFilterMap(_parameter);
        final StatusFilterValue value = (StatusFilterValue) map.get(key);

        final List<DropDownPosition> values = new ArrayList<DropDownPosition>();
        final List<Status> statusList = getStatusListFromProperties(_parameter);

        for (final Status status : statusList) {
            final DropDownPosition position = new DropDownPosition(status.getId(), status.getLabel());
            values.add(position);
            position.setSelected(value.getObject().contains(status.getId()));
        }
        Collections.sort(values, new Comparator<DropDownPosition>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _arg0,
                               final DropDownPosition _arg1)
            {
                return _arg0.getOrderValue().compareTo(_arg1.getOrderValue());
            }
        });
        final ListType listtype = ListType.valueOf(getProperty(_parameter, "ListType", "CHECKBOX"));
        ret.put(ReturnValues.SNIPLETT,
                        new org.efaps.esjp.common.uiform.Field().getInputField(_parameter, values, listtype));
        return ret;
    }

    /**
     * Get the filter map from the context.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the mpa from the context
     * @throws EFapsException on error
     */
    public Map<String, Object> getFilterMap(final Parameter _parameter)
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
     *
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
     *
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
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return new empty return
     * @throws EFapsException on error
     */
    public Return setFilter(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Object> oldFilter = getFilterMap(_parameter);
        final Map<String, Object> newFilter = new HashMap<String, Object>();

        final AbstractCommand cmd = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Form form = cmd.getTargetForm();
        for (final Field field : form.getFields()) {
            newFilter.put(field.getName(), getFilterValue(_parameter, field, oldFilter));
        }
        oldFilter.clear();
        oldFilter.putAll(newFilter);
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _field field the value is wanted for
     * @param _oldFilter old filter
     * @return object
     */
    @SuppressWarnings("unchecked")
    protected Object getFilterValue(final Parameter _parameter,
                                    final Field _field,
                                    final Map<String, Object> _oldFilter)
    {
        final Object obj;
        final String val = _parameter.getParameterValue(_field.getName());
        final String[] values = _parameter.getParameterValues(_field.getName());
        final IUIProvider uiProvider = _field.getUIProvider();
        if (uiProvider instanceof DateTimeUI || uiProvider instanceof DateUI) {
            obj = new DateTime(val);
        } else if (uiProvider instanceof BooleanUI) {
            obj = BooleanUtils.toBoolean(val);
        } else if ("type".equals(_field.getName())) {
            final Set<Long> typeIds = new HashSet<>();
            if (values != null) {
                for (final String value : values) {
                    typeIds.add(Long.valueOf(value));
                }
            }
            obj = new TypeFilterValue().setObject(typeIds);
        } else if ("currency".equals(_field.getName())) {
            if ("BASE".equals(val)) {
                obj = new CurrencyFilterValue().setObject(Instance.get("", "", "BASE"));
            } else {
                obj = new CurrencyFilterValue().setObject(Instance.get(val));
            }
        } else if (_oldFilter.containsKey(_field.getName())) {
            final Object oldObj = _oldFilter.get(_field.getName());
            if (oldObj instanceof EnumFilterValue) {
                @SuppressWarnings("rawtypes")
                final Class clazz = ((EnumFilterValue) oldObj).getObject().getDeclaringClass();
                obj = new EnumFilterValue().setObject(Enum.valueOf(clazz, val));
            } else if (oldObj instanceof StatusFilterValue) {
                final Set<Long> statusIds = new HashSet<>();
                if (values != null) {
                    for (final String value : values) {
                        statusIds.add(Long.valueOf(value));
                    }
                }
                obj = new StatusFilterValue().setObject(statusIds);
            } else if (oldObj instanceof InstanceFilterValue) {
                obj = new InstanceFilterValue().setObject(Instance.get(val));
            } else if (oldObj instanceof InstanceSetFilterValue) {
                final Set<Instance> set = new HashSet<>();
                if (values != null) {
                    for (final String value : values) {
                        set.add(Instance.get(value));
                    }
                }
                obj = new InstanceSetFilterValue().setObject(set);
            } else {
                obj = val;
            }
        } else {
            obj = val;
        }
        return obj;
    }

    /**
     * Get a fieldvalue that show the current selected filter.
     *
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
                    html.append("<span style=\"font-weight: bold;\">").append(entry.getKey())
                                    .append(": ").append("</span>").append(entry.getValue());
                }
            } else {
                final Table table = new Table();
                final Map<String, Object> filters = map.get(filterKey);
                final Map<Integer, String> dBProperties = analyseProperty(_parameter, "DBProperty");
                int i = 2;
                for (final Entry<Integer, String> entry : fields.entrySet()) {
                    if (i % 2 == 0 || filters.size() < 8) {
                        table.addRow();
                    }
                    String value = "-";
                    final Object valueTmp = filters.get(entry.getValue());
                    if (valueTmp != null) {
                        if (valueTmp instanceof DateTime) {
                            value = ((DateTime) valueTmp).toString(DateTimeFormat.mediumDate().withLocale(
                                            Context.getThreadContext().getLocale()));
                        } else if (valueTmp instanceof Boolean) {
                            value = DBProperties.getProperty(dBProperties.get(entry.getKey()) + "." + valueTmp);
                        } else if (valueTmp instanceof AbstractFilterValue) {
                            value = ((AbstractFilterValue<?>) valueTmp).getLabel(_parameter);
                        } else {
                            value = valueTmp.toString();
                        }
                    }
                    final StringBuilder inner = new StringBuilder().append("<span style=\"font-weight: bold;\">")
                                    .append(DBProperties.getProperty(dBProperties.get(entry.getKey()))).append(" ")
                                    .append("</span>");
                    table.addColumn(inner).addColumn(value)
                        .getCurrentColumn().setStyle("max-width: 300px;white-space: normal");
                    i++;
                }
                html.append(table.toHtml());
            }
        }
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }

    @SuppressWarnings("unchecked")
    protected static <S> S getEnumValue(final Object _object)
    {
        Object ret = null;
        if (_object instanceof EnumFilterValue) {
            ret = ((EnumFilterValue) _object).getObject();
        } else {
            ret = _object;
        }
        return (S) ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _instances    Instances the label is wanted for
     * @return String
     * @throws EFapsException on error
     */
    public static String getInstanceLabel(final Parameter _parameter,
                                          final Instance... _instances)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        if (_instances != null) {
            for (final Instance instance : _instances) {
                if (instance.isValid()) {
                    @SuppressWarnings("unchecked")
                    final Map<String, String> props = (Map<String, String>) _parameter.get(ParameterValues.PROPERTIES);
                    String key = instance.getType().getName() + "_Select";
                    String select = null;
                    String phrase = null;
                    String msgPhraseStr = null;
                    MsgPhrase msgPhrase = null;
                    if (props.containsKey(key)) {
                        select = props.get(key);
                    } else {
                        key = instance.getType().getName() + "_Phrase";
                        if (props.containsKey(key)) {
                            phrase = props.get(key);
                        } else {
                            key = instance.getType().getName() + "_MsgPhrase";
                            msgPhraseStr = props.get(key);
                        }
                    }

                    final PrintQuery print = new PrintQuery(instance);
                    if (select != null) {
                        print.addSelect(select);
                    } else if (phrase != null) {
                        print.addPhrase("ph", phrase);
                    } else if (msgPhraseStr != null) {
                        if (msgPhraseStr.matches(AbstractCommon_Base.UUID_REGEX)) {
                            msgPhrase = MsgPhrase.get(UUID.fromString(msgPhraseStr));
                        } else {
                            msgPhrase = MsgPhrase.get(msgPhraseStr);
                        }
                        print.addMsgPhrase(msgPhrase);
                    }
                    print.execute();
                    String val = null;
                    if (select != null) {
                        val = print.getSelect(select);
                    } else if (phrase != null) {
                        val = print.getPhrase("ph");
                    } else if (msgPhrase != null) {
                        val = print.getMsgPhrase(msgPhrase);
                    }
                    if (val != null) {
                        if (ret.length() > 0) {
                            ret.append(", ");
                        }
                        ret.append(val);
                    }
                }
            }
        }
        return ret.toString();
    }

    /**
     * Basic filter class.
     */
    public abstract static class AbstractFilterValue<T>
        implements Serializable
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * The object for this filter.
         */
        private T object;

        /**
         * @param _parameter Parameter as passed by the eFaps API
         * @return the label for this filter
         * @throws EFapsException on error
         */
        public String getLabel(final Parameter _parameter)
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
         * @return return this for chaining
         */
        public AbstractFilterValue<T> setObject(final T _object)
        {
            this.object = _object;
            return this;
        }
    }

    public static class TypeFilterValue
        extends AbstractFilterValue<Set<Long>>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            final StringBuilder ret = new StringBuilder();
            final List<String> labels = new ArrayList<>();
            for (final Long val : getObject()) {
                labels.add(Type.get(val).getLabel());
            }
            Collections.sort(labels, new Comparator<String>()
            {

                @Override
                public int compare(final String _o1,
                                   final String _o2)
                {
                    return _o1.compareTo(_o2);
                }
            });
            for (final String label : labels) {
                if (ret.length() > 0) {
                    ret.append(", ");
                }
                ret.append(label);
            }
            return ret.toString();
        }
    }

    public static class StatusFilterValue
        extends AbstractFilterValue<Set<Long>>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            final StringBuilder ret = new StringBuilder();
            final List<String> labels = new ArrayList<>();
            for (final Long val : getObject()) {
                labels.add(Status.get(val).getLabel());
            }
            Collections.sort(labels, new Comparator<String>()
            {

                @Override
                public int compare(final String _o1,
                                   final String _o2)
                {
                    return _o1.compareTo(_o2);
                }
            });
            for (final String label : labels) {
                if (ret.length() > 0) {
                    ret.append(", ");
                }
                ret.append(label);
            }
            return ret.toString();
        }
    }

    /**
     * Filter that has a Instance as base.
     */
    public static class InstanceFilterValue
        extends AbstractFilterValue<Instance>
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            return getInstanceLabel(_parameter, getObject());
        }
    }

    public static class InstanceSetFilterValue
        extends AbstractFilterValue<Set<Instance>>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            return getInstanceLabel(_parameter, getObject().toArray(new Instance[getObject().size()]));
        }
    }

    /**
     * FilterClass.
     */
    public static class CurrencyFilterValue
        extends AbstractFilterValue<Instance>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            String ret;
            if ("BASE".equals(getObject().getKey())) {
                ret = DBProperties.getProperty(FilteredReport.class.getName() + ".BaseCurrency");
            } else if (getObject().isValid()) {
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

    /**
     * FilterClass.
     */
    public static class EnumFilterValue
        extends AbstractFilterValue<Enum<?>>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            return DBProperties.getProperty(getObject().getClass().getName() + "." + getObject().toString());
        }
    }

    public static class InstanceOption
        implements IOption
    {

        private static final long serialVersionUID = 1L;

        private final String label;
        private final String value;

        public InstanceOption(final String _value,
                              final String _label)
        {
            this.label = _label;
            this.value = _value;
        }

        @Override
        public String getLabel()
        {
            return this.label;
        }

        @Override
        public Object getValue()
        {
            return this.value;
        }

        @Override
        public boolean isSelected()
        {
            return false;
        }
    }

}
