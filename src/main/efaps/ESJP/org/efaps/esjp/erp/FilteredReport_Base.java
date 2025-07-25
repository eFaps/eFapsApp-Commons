/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.erp;

import java.io.Serializable;
import java.math.BigDecimal;
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
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
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ui.IOption;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.datetime.DateAndTimeUtils;
import org.efaps.esjp.common.datetime.JodaTimeUtils;
import org.efaps.esjp.common.jasperreport.AbstractCachedReport;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.esjp.common.uiform.Field_Base.ListType;
import org.efaps.esjp.common.util.InterfaceUtils;
import org.efaps.esjp.common.util.InterfaceUtils_Base.DojoLibs;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.esjp.ui.html.Table;
import org.efaps.esjp.ui.rest.dto.OptionDto;
import org.efaps.esjp.ui.rest.dto.ValueDto;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.efaps.util.UUIDUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.VariableBuilder;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.builder.column.ComponentColumnBuilder;
import net.sf.dynamicreports.report.builder.column.ValueColumnBuilder;
import net.sf.dynamicreports.report.builder.component.GenericElementBuilder;
import net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression;
import net.sf.dynamicreports.report.builder.group.GroupBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.builder.subtotal.CustomSubtotalBuilder;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.definition.ReportParameters;

/**
 * @author The eFaps Team
 */
@EFapsUUID("fc64ff47-d1f6-4aed-8d7d-2a9128a51a19")
@EFapsApplication("eFapsApp-Commons")
public abstract class FilteredReport_Base
    extends AbstractCachedReport
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
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilteredReport.class);

    private Map<String, Object> filterMap;

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
            final Map<Integer, String> fields = getFields(_parameter);
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
            ret = JodaTimeUtils.toDateTime(DateAndTimeUtils.getDefaultValue(_parameter, props));
        } else if ("Type".equalsIgnoreCase(_type)) {
            final Set<Long> set = new HashSet<>();
            if ("ALL".equals(_default)) {
                final List<Type> types = getTypeList(_parameter, _field);
                for (final Type type : types) {
                    set.add(type.getId());
                }
            } else if ("NONE".equals(_default) || _default.isEmpty()) {
                set.add((long) 0);
            } else if (isUUID(_default)) {
                set.add(Type.get(UUID.fromString(_default)).getId());
            } else {
                set.add(Type.get(_default).getId());
            }
            ret = new TypeFilterValue().setObject(set);
        } else if ("Instance".equalsIgnoreCase(_type)) {
            ret = new InstanceFilterValue().setObject(Instance.get(_default));
        } else if ("InstanceSet".equalsIgnoreCase(_type)) {
            final Instance inst = Instance.get(_default);
            ret = new InstanceSetFilterValue().setObject(inst.isValid()
                            ? new HashSet<>(Arrays.asList(inst))
                            : new HashSet<Instance>());
        } else if ("Boolean".equalsIgnoreCase(_type)) {
            ret = BooleanUtils.toBoolean(_default);
        } else if ("Currency".equalsIgnoreCase(_type)) {
            final Instance inst;
            if ("BASECURRENCY".equalsIgnoreCase(_default)) {
                inst = Currency.getBaseCurrency();
            } else {
                inst = Instance.get(_default);
            }
            ret = new CurrencyFilterValue().setObject(inst);
        } else if ("Enum".equalsIgnoreCase(_type)) {
            try {
                final Class<?> clazz = Class.forName(_default);
                if (clazz.isEnum()) {
                    final Object[] consts = clazz.getEnumConstants();
                    ret = new EnumFilterValue().setObject((Enum<?>) consts[0]);
                }
            } catch (final ClassNotFoundException e) {
                FilteredReport_Base.LOG.error("Could not find enum class {}", e);
            }
        } else if ("AttributeDefinition".equalsIgnoreCase(_type)) {
            final Type type;
            if (isUUID(_default)) {
                type = Type.get(UUID.fromString(_default));
            } else {
                type = Type.get(_default);
            }
            final QueryBuilder queryBldr = new QueryBuilder(type);
            queryBldr.addWhereAttrEqValue(CIERP.AttributeDefinitionAbstract.StatusAbstract,
                            Status.find(CIERP.AttributeDefinitionStatus.Active));
            final InstanceQuery query = queryBldr.getQuery();
            ret = new AttrDefFilterValue().setObject(new HashSet<>(query.execute()));
        } else if ("FilterValue".equalsIgnoreCase(_type)) {
            ret = getFilterValue(_parameter, _default);
        } else if ("GroupBy".equalsIgnoreCase(_type)) {
            ret = getGroupByFilterValue(_parameter, _default);
        }
        return ret;
    }

    /**
     * Gets the filter value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _className the class name
     * @return the filter value
     * @throws EFapsException on error
     */
    protected IFilterValue getFilterValue(final Parameter _parameter,
                                          final String _className)
        throws EFapsException
    {
        IFilterValue ret = null;
        try {
            final Class<?> clazz = Class.forName(_className);
            ret = (IFilterValue) clazz.newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            FilteredReport_Base.LOG.error("Could not find IFilterValue class {}", e);
        }
        return ret;
    }

    /**
     * Gets the group by filter value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _className the class name
     * @return the group by filter value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected GroupByFilterValue getGroupByFilterValue(final Parameter _parameter,
                                                       final String _className)
    {
        final GroupByFilterValue ret = new GroupByFilterValue().setClassName(_className);
        ret.setObject(new ArrayList());
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
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
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
        final IUIValue value = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String fieldName = value.getField().getName();
        if (filterMap.containsKey(fieldName)) {
            final CurrencyFilterValue filter = (CurrencyFilterValue) filterMap.get(fieldName);
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
        Collections.sort(values, (_o1, _o2) -> String.valueOf(_o1.getOrderValue()).compareTo(
                        String.valueOf(_o2.getOrderValue())));
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
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

        final List<IOption> tokens = new ArrayList<>();
        if (map.containsKey(key)) {
            final Object obj = map.get(key);
            if (obj instanceof InstanceSetFilterValue) {
                for (final Instance instance : ((InstanceSetFilterValue) obj).getObject()) {
                    if (instance.isValid()) {
                        tokens.add(new InstanceOption(instance.getOid(),
                                        FilteredReport_Base.getInstanceLabel(_parameter, instance)));
                    }
                }
            }
        } else {
            map.put(key, "");
        }
        Collections.sort(tokens, Comparator.comparing(IOption::getLabel));
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
     * Gets the option list field value.
     *
     * @param _parameter the _parameter
     * @return the option list field value
     * @throws EFapsException the e faps exception
     */
    public Return getOptionListFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final IUIValue value = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = value.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        String val = "";
        if (map.containsKey(key)) {
            final Object obj = map.get(key);
            if (obj instanceof InstanceFilterValue) {
                val = ((InstanceFilterValue) obj).getObject().getOid();
            } else {
                val = obj.toString();
            }
            _parameter.put(ParameterValues.UIOBJECT, UIValue.get(value.getField(), null, val));
        } else {
            map.put(key, "");
        }

        return new org.efaps.esjp.common.uiform.Field().getOptionListFieldValue(_parameter);
    }

    /**
     * Gets the attr def field value.
     *
     * @param _parameter the _parameter
     * @return the attr def field value
     * @throws EFapsException on error
     */
    public Return getAttrDefFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final List<DropDownPosition> values = new ArrayList<>();
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = uiValue.getField().getName();
        final Map<String, Object> map = getFilterMap(_parameter);
        if (map.containsKey(key)) {
            final Object obj = map.get(key);
            final Set<Instance> val;
            if (obj instanceof AttrDefFilterValue) {
                val = ((AttrDefFilterValue) obj).getObject();
            } else {
                val = new HashSet<>();
            }
            final QueryBuilder queryBldr = getQueryBldrFromProperties(_parameter);
            queryBldr.addWhereAttrEqValue(CIERP.AttributeDefinitionAbstract.StatusAbstract,
                            Status.find(CIERP.AttributeDefinitionStatus.Active));
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIERP.AttributeDefinitionAbstract.Value, CIERP.AttributeDefinitionAbstract.Description);
            multi.execute();
            while (multi.next()) {
                final String value = multi.getAttribute(CIERP.AttributeDefinitionAbstract.Value);
                final String description = multi.getAttribute(CIERP.AttributeDefinitionAbstract.Description);
                final DropDownPosition pos = new DropDownPosition(multi.getCurrentInstance().getOid(),
                                value + (description != null && !description.isEmpty() ? " - " + description : ""));
                pos.setSelected(val.contains(multi.getCurrentInstance()));
                values.add(pos);
            }
        } else {
            map.put(key, "");
        }
        ret.put(ReturnValues.VALUES, values);
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
        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
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
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String key = uiValue.getField().getName();

        final Map<String, Object> map = getFilterMap(_parameter);
        if (!map.containsKey(key)) {
            map.put(key, StringUtils.endsWith(key, "_negate") ? Boolean.valueOf(false) : Boolean.valueOf(true));
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
        if (uiObject instanceof final UIValue uiValue) {
            key = uiValue.getField().getName();
        } else {
            final IUIValue fieldValue = (IUIValue) uiObject;
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
        ret.put(ReturnValues.VALUES, values);
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
        final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        if (filter.containsKey(uiValue.getField().getName())) {
            final TypeFilterValue filters = (TypeFilterValue) filter.get(uiValue.getField().getName());
            selected.addAll(filters.getObject());
        }
        final List<Type> types = getTypeList(_parameter, uiValue.getField().getName());
        for (final Type type : types) {
            final DropDownPosition dropdown = new DropDownPosition(type.getId(), type.getLabel());
            dropdown.setSelected(selected.contains(type.getId()));
            values.add(dropdown);
        }
        Collections.sort(values, Comparator.comparing(DropDownPosition::getLabel));
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
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
        return getTypeList(_parameter, (String) null);
    }

    /**
     * Gets the type list.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _fieldName the _field name
     * @return list of types
     * @throws EFapsException on error
     */
    protected List<Type> getTypeList(final Parameter _parameter,
                                     final String _fieldName)
        throws EFapsException
    {
        final List<Type> ret = new ArrayList<>();
        final Properties props = getProperties4TypeList(_parameter, _fieldName);

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
     * Gets the properties4 type list.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _fieldName the _field name
     * @return Properties
     * @throws EFapsException on error
     */
    protected Properties getProperties4TypeList(final Parameter _parameter,
                                                final String _fieldName)
        throws EFapsException
    {
        Properties ret = new Properties();
        final Command cmd = (Command) _parameter.get(ParameterValues.CALL_CMD);
        if (cmd != null && cmd.getProperty("FilterTargetForm") != null) {
            final String formStr = cmd.getProperty("FilterTargetForm");
            final String fieldStr = cmd.getProperty("FilterTargetField");

            final Form form = isUUID(formStr) ? Form.get(UUID.fromString(formStr)) : Form.get(formStr);
            final Field field = form.getField(fieldStr);
            final EventDefinition event = field.getEvents(EventType.UI_FIELD_VALUE).get(0);
            ret = MapUtils.toProperties(event.getPropertyMap());
        } else if (containsProperty(_parameter, "QueryBldrConfig")) {
            final String config = getProperty(_parameter, "QueryBldrConfig");
            final SystemConfiguration sysConf;
            if (isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            if (sysConf != null) {
                ret = sysConf.getAttributeValueAsProperties(getProperty(_parameter, "QueryBldrConfigAttribute"));
            }
        } else {
            ret = MapUtils.toProperties((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES));
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
        if (uiObject instanceof final UIValue uiValue) {
            key = uiValue.getField().getName();
        } else {
            final IUIValue fieldValue = (IUIValue) uiObject;
            key = fieldValue.getField().getName();
        }

        final Map<String, Object> map = getFilterMap(_parameter);
        final StatusFilterValue value = (StatusFilterValue) map.get(key);

        final List<DropDownPosition> values = new ArrayList<>();
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
     * Gets the group by field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the group by field value
     * @throws EFapsException on error
     */
    public Return getGroupByFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        final IUIValue fieldValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        final String fieldName = fieldValue.getField().getName();

        final Map<String, Object> map = getFilterMap(_parameter);
        final GroupByFilterValue filterValue = (GroupByFilterValue) map.get(fieldName);

        final String divId = RandomUtil.randomAlphabetic(8);

        final StringBuilder html = new StringBuilder()
                        .append("<div class=\"groupByFilter\" id=\"").append(divId).append("\">")
                        .append("<div class=\"groupByContainer\">\n")
                        .append("<h3>")
                        .append(DBProperties.getProperty(FilteredReport.class.getName() + ".GroupByActive"))
                        .append("</h3>")
                        .append("<ol id=\"groupByNodeActive\" class=\"container\">\n")
                        .append("</ol>\n")
                        .append("</div>\n")
                        .append("<div class=\"groupByContainer\">\n")
                        .append("<h3>")
                        .append(DBProperties.getProperty(FilteredReport.class.getName() + ".GroupByInactive"))
                        .append("</h3>")
                        .append("<ol id=\"groupByNodeInactive\" class=\"container\">\n")
                        .append("</ol>\n")
                        .append("</div>\n")
                        .append("</div>\n");

        final String key = RandomUtil.randomAlphabetic(8);

        final List<Enum<?>> active = filterValue.getObject();
        final List<Enum<?>> inactive = filterValue.getInactive();

        final StringBuilder js = new StringBuilder()
                        .append("var acList = new Source(\"groupByNodeActive\", ")
                        .append("{ accept: [ \"").append(key).append("\"] });\n")
                        .append("var inacList = new Source(\"groupByNodeInactive\",")
                        .append("{ accept: [ \"").append(key).append("\"] });\n")
                        .append("var data =  [\n");

        for (final Enum<?> val : inactive) {
            js.append("{ data: \"").append(DBProperties.getProperty(val.getClass().getName() + "." + val.toString()))
                            .append("\",")
                            .append(" key: \"").append(val.toString()).append("\",\n")
                            .append(" type: [ \"").append(key).append("\" ] },\n");
        }
        js.append("];\n")
                        .append("var acData =  [\n");

        for (final Enum<?> val : active) {
            js.append("{ data: \"").append(DBProperties.getProperty(val.getClass().getName() + "." + val.toString()))
                            .append("\",")
                            .append(" key: \"").append(val.toString()).append("\",\n")
                            .append(" type: [ \"").append(key).append("\" ] },\n");
        }
        js.append("];\n")
                        .append(" for(i = 0; i < data.length; ++i){\n")
                        .append("t = inacList._normalizedCreator(data[i]);\n")
                        .append("data[i].id = t.node.id;")
                        .append(" inacList.setItem(t.node.id, {data: t.data, type: t.type});\n")
                        .append(" inacList.parent.appendChild(t.node);\n")
                        .append(" }\n")

                        .append("for(i = 0; i < acData.length; ++i){\n")
                        .append(" domConstruct.place(\"<input type='hidden' name='").append(fieldName)
                        .append("' value='\" + acData[i].key + \"'/>\", \"").append(divId).append("\");\n")
                        .append("t = acList._normalizedCreator(acData[i]);\n")
                        .append("acData[i].id = t.node.id;")
                        .append(" acList.setItem(t.node.id, {data: t.data, type: t.type});\n")
                        .append(" acList.parent.appendChild(t.node);\n")
                        .append(" }\n")
                        .append("var all = data.concat(acData);")
                        .append("var sv = function() {\n")
                        .append("query(\"[name=")
                        .append(fieldName)
                        .append("]\").forEach(domConstruct.destroy);\n")
                        .append("query(\"[name=")
                        .append(fieldName)
                        .append("_inactive]\").forEach(domConstruct.destroy);\n")
                        .append("inacList.getAllNodes().forEach(function(_node) {\n  ")
                        .append("var sel;")
                        .append("array.some(all, function(item){\n")
                        .append("if (item.id === _node.id) {\n")
                        .append("sel = item;")
                        .append("return true;\n")
                        .append("}\n")
                        .append("return false;\n")
                        .append(" });\n")
                        .append(" domConstruct.place(\"<input type='hidden' name='")
                        .append(fieldName)
                        .append("_inactive' value='\" + sel.key + \"'/>\", \"")
                        .append(divId)
                        .append("\");\n")
                        .append("});\n")
                        .append("acList.getAllNodes().forEach(function(_node) {\n  ")
                        .append("var sel;")
                        .append("array.some(all, function(item){\n")
                        .append("if (item.id === _node.id) {\n")
                        .append("sel = item;")
                        .append("return true;\n")
                        .append("}\n")
                        .append("return false;\n")
                        .append(" });\n")
                        .append(" domConstruct.place(\"<input type='hidden' name='")
                        .append(fieldName)
                        .append("' value='\" + sel.key + \"'/>\", \"")
                        .append(divId)
                        .append("\");\n")
                        .append("});\n")
                        .append("}\n")
                        .append("aspect.after(acList, \"onDrop\", sv);\n")
                        .append("aspect.after(inacList, \"onDrop\", sv);\n");

        html.append(InterfaceUtils.wrappInScriptTag(_parameter,
                        InterfaceUtils.wrapInDojoRequire(_parameter, js, DojoLibs.DNDSOURCE, DojoLibs.QUERY,
                                        DojoLibs.DOMCONSTRUCT, DojoLibs.ASPECT, DojoLibs.ARRAY),
                        true, 0));

        ret.put(ReturnValues.SNIPLETT, html);
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
        Map<String, Object> ret;
        if (this.filterMap == null) {
            final Map<String, Map<String, Object>> map = getCtxMap(_parameter);
            final String filterKey = getFilterKey(_parameter);
            ret = map.get(filterKey);
            if (ret == null) {
                ret = new HashMap<>();
                map.put(filterKey, ret);
            }
        } else {
            ret = this.filterMap;
        }
        return ret;
    }

    public void setFilterMap(Map<String, Object> filterMap)
    {
        this.filterMap = filterMap;
    }

    public Map<String, Object> getFilterMap()
    {
        return this.filterMap;
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
        final Map<String, Map<String, Object>> map;
        if (Context.getThreadContext().containsSessionAttribute(FilteredReport_Base.KEY4SESSION)) {
            map = (Map<String, Map<String, Object>>) Context.getThreadContext().getSessionAttribute(
                            FilteredReport_Base.KEY4SESSION);
        } else {
            map = new HashMap<>();
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
            if (callCmd instanceof final AbstractCommand cmd) {
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
        final Map<String, Object> newFilter = new HashMap<>();

        final AbstractCommand cmd = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Form form = cmd.getTargetForm();
        for (final Field field : form.getFields()) {
            newFilter.put(field.getName(), getFilterValue(_parameter, field, oldFilter));
        }
        oldFilter.clear();
        oldFilter.putAll(newFilter);
        clearCache(_parameter);
        return new Return();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _field field the value is wanted for
     * @param _oldFilter old filter
     * @return object
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    protected Object getFilterValue(final Parameter _parameter,
                                    final Field _field,
                                    final Map<String, Object> _oldFilter)
        throws EFapsException
    {
        final Object obj;
        final String val = _parameter.getParameterValue(_field.getName());
        final String[] values = _parameter.getParameterValues(_field.getName());
        final IUIProvider uiProvider = _field.getUIProvider();
        if (uiProvider instanceof DateTimeUI) {
            obj = new DateTime(val);
        } else if (uiProvider instanceof DateUI) {
            obj = JodaTimeUtils.toDateTime(DateAndTimeUtils.getDateForQuery(val));
        } else if (uiProvider instanceof BooleanUI) {
            obj = BooleanUtils.toBoolean(val);
        } else if (_oldFilter.containsKey(_field.getName())) {
            final Object oldObj = _oldFilter.get(_field.getName());
            if (oldObj instanceof CurrencyFilterValue) {
                if ("BASE".equals(val)) {
                    obj = new CurrencyFilterValue().setObject(Instance.get("", "", "BASE"));
                } else {
                    obj = new CurrencyFilterValue().setObject(Instance.get(val));
                }
            } else if (oldObj instanceof EnumFilterValue) {
                @SuppressWarnings("rawtypes") final Class clazz = ((EnumFilterValue) oldObj).getObject()
                                .getDeclaringClass();
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
                        final Instance inst = Instance.get(value);
                        if (inst.isValid()) {
                            set.add(inst);
                        }
                    }
                }
                obj = new InstanceSetFilterValue().setObject(set);
            } else if (oldObj instanceof AttrDefFilterValue) {
                final Set<Instance> set = new HashSet<>();
                if (values != null) {
                    for (final String value : values) {
                        set.add(Instance.get(value));
                    }
                }
                obj = new AttrDefFilterValue().setObject(set);
            } else if (oldObj instanceof TypeFilterValue) {
                final Set<Long> typeIds = new HashSet<>();
                if (values != null) {
                    for (final String value : values) {
                        typeIds.add(Long.valueOf(value));
                    }
                }
                obj = new TypeFilterValue().setObject(typeIds);
            } else if (oldObj instanceof IFilterValue) {
                obj = ((IFilterValue) oldObj).parseObject(values);
            } else {
                obj = val;
            }
        } else {
            obj = val;
        }
        if (obj != null && obj instanceof AbstractFilterValue) {
            final String negate = _parameter.getParameterValue(_field.getName() + "_negate");
            if (StringUtils.isNotEmpty(negate)) {
                ((AbstractFilterValue<?>) obj).setNegate(BooleanUtils.toBoolean(negate));
            }
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
            final DateTime time = getCachedTime(_parameter);
            if (time != null) {
                html.append(DBProperties.getFormatedDBProperty(FilteredReport.class.getName() + ".CacheTime",
                                time.toDate())).append("<br/>");
            }
            final Map<Integer, String> fields = getFields(_parameter);
            boolean first = true;
            if (fields.isEmpty()) {
                for (final Entry<String, Object> entry : map.get(filterKey).entrySet()) {
                    if (StringUtils.isNoneEmpty(entry.getKey()) && entry.getValue() != null
                                    && !entry.getValue().toString().isEmpty()) {
                        if (first) {
                            first = false;
                        } else {
                            html.append("<br/>");
                        }
                        html.append("<span style=\"font-weight: bold;\">")
                                        .append(entry.getKey())
                                        .append(": ").append("</span>").append(entry.getValue());
                    }
                }
            } else {
                final Table table = new Table();
                final Map<String, Object> filters = map.get(filterKey);
                final Map<Integer, String> dBProperties = analyseProperty(_parameter, "DBProperty");
                int i = 2;
                for (final Entry<Integer, String> entry : fields.entrySet()) {
                    if (i % 2 == 0 || fields.size() < 8) {
                        table.addRow();
                    }
                    String value = "-";
                    final Object valueTmp = filters.get(entry.getValue());
                    boolean negate = false;
                    if (valueTmp != null) {
                        if (valueTmp instanceof DateTime) {
                            value = ((DateTime) valueTmp).toString(DateTimeFormat.mediumDate().withLocale(
                                            Context.getThreadContext().getLocale()));
                        } else if (valueTmp instanceof Boolean) {
                            value = DBProperties.getProperty(dBProperties.get(entry.getKey()) + "." + valueTmp);
                        } else if (valueTmp instanceof IFilterValue) {
                            value = ((IFilterValue) valueTmp).getLabel(_parameter);
                            negate = ((IFilterValue) valueTmp).isNegate();
                        } else {
                            value = valueTmp.toString();
                        }
                    }
                    final StringBuilder inner = new StringBuilder().append("<span style=\"font-weight: bold;");
                    if (negate) {
                        inner.append("color:red;");
                    }
                    inner.append("\">").append(DBProperties.getProperty(dBProperties.get(entry.getKey()))).append(" ")
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

    /**
     * Gets the fields.
     *
     * @param _parameter the _parameter
     * @return the fields
     * @throws EFapsException the e faps exception
     */
    protected Map<Integer, String> getFields(final Parameter _parameter)
        throws EFapsException
    {
        final Map<Integer, String> ret = analyseProperty(_parameter, "Field");
        if (containsProperty(_parameter, "SystemConfig")) {
            final String configStr = getProperty(_parameter, "SystemConfig");
            final SystemConfiguration config;
            if (isUUID(configStr)) {
                config = SystemConfiguration.get(UUID.fromString(configStr));
            } else {
                config = SystemConfiguration.get(configStr);
            }
            for (final Entry<Integer, String> entry : analyseProperty(_parameter, "Field").entrySet()) {
                final Integer idx = entry.getKey();
                String formatStr = "%02d";
                if (idx > 99) {
                    formatStr = "%03d";
                }
                final String accessAttribute = "AccessAttribute" + String.format(formatStr, idx);
                final String accessAttributeExists = "AccessAttributeExists" + String.format(formatStr, idx);
                if (containsProperty(_parameter, accessAttribute)) {
                    final String value = getProperty(_parameter, accessAttribute);
                    final boolean inverse = value.startsWith("!");
                    final String key = inverse ? value.substring(1) : value;
                    final boolean access = inverse ? !config.getAttributeValueAsBoolean(key)
                                    : config.getAttributeValueAsBoolean(key);
                    if (!access) {
                        ret.remove(entry.getKey());
                    }
                }
                if (containsProperty(_parameter, accessAttributeExists)) {
                    final String value = getProperty(_parameter, accessAttributeExists);
                    final boolean inverse = value.startsWith("!");
                    final String key = inverse ? value.substring(1) : value;
                    final boolean access = inverse ? !config.containsAttributeValue(key)
                                    : config.containsAttributeValue(key);
                    if (!access) {
                        ret.remove(entry.getKey());
                    }
                }
            }
        }
        return ret;
    }

    public List<ValueDto> getFilters() {
        return null;
    }

    protected List<OptionDto> getOptions(final Class<? extends Enum<?>> optionEnum)
    {
        final List<OptionDto> ret = new ArrayList<>();
        if (optionEnum.isEnum()) {
            final var clazzName = optionEnum.getName();
            for (final var enumConstant : optionEnum.getEnumConstants()) {
                final var name = enumConstant.name();
                ret.add(OptionDto.builder()
                                .withLabel(DBProperties.getProperty(clazzName + "." + name))
                                .withValue(name)
                                .build());
            }
        }
        return ret;
    }

    protected List<OptionDto> getOptions4Boolean(final String key)
    {
        final List<OptionDto> ret = new ArrayList<>();
        ret.add(OptionDto.builder()
                        .withLabel(DBProperties.getProperty(key + ".false"))
                        .withValue(false)
                        .build());
        ret.add(OptionDto.builder()
                        .withLabel(DBProperties.getProperty(key + ".true"))
                        .withValue(true)
                        .build());
        return ret;
    }

    @Override
    protected Long getLifespan(final Parameter _parameter)
        throws EFapsException
    {
        return Long.parseLong(ERP.FILTERREPORTCONFIG.get().getProperty(getClass().getName() + " .LifeSpan", "5"));
    }

    /**
     * Gets the enum value.
     *
     * @param <S> the generic type
     * @param _object the _object
     * @return the enum value
     */
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
     * @param _instances Instances the label is wanted for
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
                    @SuppressWarnings("unchecked") final Map<String, String> props = (Map<String, String>) _parameter
                                    .get(ParameterValues.PROPERTIES);
                    boolean search = true;
                    String select = null;
                    String phrase = null;
                    String msgPhraseStr = null;
                    MsgPhrase msgPhrase = null;
                    Type type = instance.getType();
                    while (search) {
                        String key = type.getName() + "_Select";
                        if (props.containsKey(key)) {
                            select = props.get(key);
                            search = false;
                        } else {
                            key = type.getName() + "_Phrase";
                            if (props.containsKey(key)) {
                                phrase = props.get(key);
                                search = false;
                            } else {
                                key = type.getName() + "_MsgPhrase";
                                msgPhraseStr = props.get(key);
                                search = !props.containsKey(key);
                            }
                        }
                        if (search) {
                            type = type.getParentType();
                            search = type != null;
                        }
                    }
                    final PrintQuery print = new PrintQuery(instance);
                    if (select != null) {
                        print.addSelect(select);
                    } else if (phrase != null) {
                        print.addPhrase("ph", phrase);
                    } else if (msgPhraseStr != null) {
                        if (UUIDUtil.isUUID(msgPhraseStr)) {
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
     * Gets the link column.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _field the field
     * @return the link column
     */
    protected static ComponentColumnBuilder getLinkColumn(final Parameter _parameter,
                                                          final String _field)
    {
        final GenericElementBuilder linkElement = DynamicReports.cmp.genericElement(
                        "http://www.efaps.org", "efapslink")
                        .addParameter(EmbeddedLink.JASPER_PARAMETERKEY, new LinkExpression(_field))
                        .setHeight(12).setWidth(25);
        final ComponentColumnBuilder ret = DynamicReports.col.componentColumn(linkElement).setTitle("");
        return ret;
    }

    /**
     * Gets the custom text subtotal builder.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _field the field
     * @param _showInColumn the show in column
     * @return the custom text subtotal builder
     */
    protected static CustomTextSubtotalBuilder getCustomTextSubtotalBuilder(final Parameter _parameter,
                                                                            final String _field,
                                                                            final ColumnBuilder<?, ?> _showInColumn)
    {
        return new CustomTextSubtotalBuilder(_field, _showInColumn);
    }

    /**
     * Gets the uo M subtotal builder.
     *
     * @param _parameter the parameter
     * @param _builder the builder
     * @param _uoMColumn the uo M column
     * @param _quantityColumn the quantity column
     * @param _resetGroup the reset group
     * @return the uo M subtotal builder
     */
    protected static UoMSubtotalBuilder getUoMSubtotalBuilder(final Parameter _parameter,
                                                              final JasperReportBuilder _builder,
                                                              final ValueColumnBuilder<?, ?> _uoMColumn,
                                                              final ValueColumnBuilder<?, ?> _quantityColumn,
                                                              final GroupBuilder<?> _resetGroup)
    {
        final String uoM = "uom_" + RandomUtil.randomAlphabetic(4);
        final String quantity = "qauntity_" + RandomUtil.randomAlphabetic(4);

        final VariableBuilder<String> uoMVar = DynamicReports.variable(uoM, _uoMColumn, Calculation.DISTINCT_COUNT);
        final VariableBuilder<BigDecimal> quantityVar = DynamicReports.variable(quantity, _quantityColumn,
                        Calculation.SUM);
        if (_resetGroup != null) {
            uoMVar.setResetGroup(_resetGroup);
            quantityVar.setResetGroup(_resetGroup);
        }
        _builder.addVariable(uoMVar, quantityVar);
        return new UoMSubtotalBuilder(uoM, quantity, _quantityColumn);
    }

    /**
     * Expression used to render a link for the UserInterface.
     */
    public static class LinkExpression
        extends AbstractComplexExpression<EmbeddedLink>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Costructor.
         *
         * @param _field the field
         */
        public LinkExpression(final String _field)
        {
            addExpression(DynamicReports.field(_field, String.class));
        }

        @Override
        public EmbeddedLink evaluate(final List<?> _values,
                                     final ReportParameters _reportParameters)
        {
            final String oid = (String) _values.get(0);
            return EmbeddedLink.getJasperLink(oid);
        }
    }

    /**
     * The Class CustomTextSubtotalBuilder.
     *
     * @author The eFaps Team
     */
    public static class CustomTextSubtotalBuilder
        extends AggregationSubtotalBuilder<String>
    {

        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new custom text subtotal builder.
         *
         * @param _fieldName the field name
         * @param _showInColumn the show in column
         */
        public CustomTextSubtotalBuilder(final String _fieldName,
                                         final ColumnBuilder<?, ?> _showInColumn)
        {
            super(new AbstractSimpleExpression<String>()
            {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override

                public String evaluate(final ReportParameters _reportParameters)
                {
                    final Object value = _reportParameters.getFieldValue(_fieldName);
                    return String.format("Total '%s':", value);
                }
            }, _showInColumn, Calculation.NOTHING);
        }
    }

    /**
     * The Class UoMSubtotalBuilder.
     */
    public static class UoMSubtotalBuilder
        extends CustomSubtotalBuilder<BigDecimal>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new uo M subtotal builder.
         *
         * @param _uoMKey the uo M key
         * @param _qauntityKey the qauntity key
         * @param _showInColumn the show in column
         */
        protected UoMSubtotalBuilder(final String _uoMKey,
                                     final String _qauntityKey,
                                     final ColumnBuilder<?, ?> _showInColumn)
        {
            super(new AbstractSimpleExpression<BigDecimal>()
            {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                public BigDecimal evaluate(final ReportParameters _reportParameters)
                {
                    final Long distinctCount = _reportParameters.getVariableValue(_uoMKey);
                    final BigDecimal quantity = _reportParameters.getVariableValue(_qauntityKey);
                    return distinctCount > 1 ? null : quantity;
                }
            }, _showInColumn);
            setDataType(DynamicReports.type.bigDecimalType());
        }
    }

    /**
     * The Interface IFilterValue.
     */
    public interface IFilterValue
        extends Serializable
    {

        /**
         * Gets the label.
         *
         * @param _parameter Parameter as passed by the eFaps API
         * @return the label
         * @throws EFapsException on error
         */
        String getLabel(Parameter _parameter)
            throws EFapsException;

        /**
         * Parses the object.
         *
         * @param _values the values
         * @return the filter value
         */
        IFilterValue parseObject(String[] _values);

        /**
         * Negate the current filter elements.
         *
         * @return true, if successful
         */
        boolean isNegate();
    }

    /**
     * Basic filter class.
     *
     * @author The eFaps Team
     * @param <T> the generic type
     */
    public abstract static class AbstractFilterValue<T>
        implements IFilterValue
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * The object for this filter.
         */
        private T object;

        /** The negate. */
        private boolean negate;

        /**
         * @param _parameter Parameter as passed by the eFaps API
         * @return the label for this filter
         * @throws EFapsException on error
         */
        @Override
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

        /**
         * Parses the object.
         *
         * @param _values the values
         * @return the abstract filter value< t>
         */
        @Override
        public AbstractFilterValue<T> parseObject(final String[] _values)
        {
            return this;
        }

        @Override
        public boolean isNegate()
        {
            return this.negate;
        }

        /**
         * Setter method for instance variable {@link #negate}.
         *
         * @param _negate value for instance variable {@link #negate}
         */
        public void setNegate(final boolean _negate)
        {
            this.negate = _negate;
        }
    }

    /**
     * The Class TypeFilterValue.
     */
    public static class TypeFilterValue
        extends AbstractFilterValue<Set<Long>>
    {

        /** */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            final StringBuilder ret = new StringBuilder();
            final List<String> labels = new ArrayList<>();
            for (final Long val : getObject()) {
                if (val > 0) {
                    labels.add(Type.get(val).getLabel());
                }
            }
            Collections.sort(labels, String::compareTo);
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
     * The Class StatusFilterValue.
     */
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
            Collections.sort(labels, String::compareTo);
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
            return FilteredReport_Base.getInstanceLabel(_parameter, getObject());
        }
    }

    /**
     * The Class InstanceSetFilterValue.
     *
     */
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
            return FilteredReport_Base.getInstanceLabel(_parameter,
                            getObject().toArray(new Instance[getObject().size()]));
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
            final String ret;
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

        /** */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            return DBProperties.getProperty(getObject().getClass().getName() + "." + getObject().toString());
        }
    }

    /**
     * FilterClass.
     */
    public static class GroupByFilterValue
        extends AbstractFilterValue<List<Enum<?>>>
    {

        /** */
        private static final long serialVersionUID = 1L;

        /** The class name. */
        private String className;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public AbstractFilterValue<List<Enum<?>>> parseObject(final String[] _values)
        {
            if (ArrayUtils.isNotEmpty(_values)) {
                try {
                    final Class<? extends Enum> clazz = (Class<? extends Enum>) Class.forName(getClassName());
                    final List<Enum<?>> values = new ArrayList<>();
                    setObject(values);
                    for (final String value : _values) {
                        values.add(Enum.valueOf(clazz, value));
                    }
                } catch (final ClassNotFoundException e) {
                    FilteredReport_Base.LOG.error("Could not find enum class {}", e);
                }
            } else {
                setObject(new ArrayList());
            }
            return this;
        }

        /**
         * Gets the inactive.
         *
         * @return the inactive
         */
        public List<Enum<?>> getInactive()
        {
            final List<Enum<?>> ret = new ArrayList<>();
            try {
                final Class<?> clazz = Class.forName(getClassName());
                if (clazz.isEnum()) {
                    final Object[] consts = clazz.getEnumConstants();
                    for (final Object obj : consts) {
                        if (!getObject().contains(obj)) {
                            ret.add((Enum<?>) obj);
                        }
                    }
                }
            } catch (final ClassNotFoundException e) {
                FilteredReport_Base.LOG.error("Could not find enum class {}", e);
            }
            return ret;
        }

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            final StringBuilder ret = new StringBuilder();
            if (CollectionUtils.isNotEmpty(getObject())) {
                for (final Enum<?> val : getObject()) {
                    if (ret.length() > 0) {
                        ret.append(", ");
                    }
                    ret.append(DBProperties.getProperty(val.getClass().getName() + "." + val.toString()));
                }
            }
            return ret.toString();
        }

        /**
         * Gets the class name.
         *
         * @return the class name
         */
        public String getClassName()
        {
            return className;
        }

        /**
         * Sets the class name.
         *
         * @param _className the new class name
         * @return this
         */
        public GroupByFilterValue setClassName(final String _className)
        {
            className = _className;
            return this;
        }
    }

    /**
     * FilterClass.
     */
    public static class AttrDefFilterValue
        extends AbstractFilterValue<Set<Instance>>
    {

        /** */
        private static final long serialVersionUID = 1L;

        @Override
        public String getLabel(final Parameter _parameter)
            throws EFapsException
        {
            final StringBuilder ret = new StringBuilder();
            final List<String> labels = new ArrayList<>();
            final MultiPrintQuery multi = new MultiPrintQuery(new ArrayList<>(getObject()));
            multi.addAttribute(CIERP.AttributeDefinitionAbstract.Value, CIERP.AttributeDefinitionAbstract.Description);
            multi.execute();
            while (multi.next()) {
                final String value = multi.getAttribute(CIERP.AttributeDefinitionAbstract.Value);
                final String description = multi.getAttribute(CIERP.AttributeDefinitionAbstract.Description);
                labels.add(value + (description != null && !description.isEmpty() ? " - " + description : ""));
            }

            Collections.sort(labels, String::compareTo);
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
     * The Class InstanceOption.
     *
     */
    public static class InstanceOption
        implements IOption
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The label. */
        private final String label;

        /** The value. */
        private final String value;

        /**
         * Instantiates a new instance option.
         *
         * @param _value the _value
         * @param _label the _label
         */
        public InstanceOption(final String _value,
                              final String _label)
        {
            label = _label;
            value = _value;
        }

        @Override
        public String getLabel()
        {
            return label;
        }

        @Override
        public Object getValue()
        {
            return value;
        }

        @Override
        public boolean isSelected()
        {
            return false;
        }
    }

}
