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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUser;
import org.efaps.ci.CIType;
import org.efaps.db.AttributeQuery;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.admin.access.AccessCheck4UI;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.esjp.common.jasperreport.StandartReport;
import org.efaps.esjp.common.listener.ITypedClass;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.common.uiform.Edit;
import org.efaps.esjp.common.uiform.Field_Base;
import org.efaps.esjp.common.util.InterfaceUtils;
import org.efaps.esjp.common.util.InterfaceUtils_Base.DojoLibs;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.esjp.erp.listener.IOnAction;
import org.efaps.esjp.erp.listener.IOnCreateDocument;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.jfree.util.Log;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!.
 *
 * @author The eFaps Team
 */
@EFapsUUID("e47df65d-4c5e-423f-b2cc-815c3007b19f")
@EFapsApplication("eFapsApp-Commons")
public abstract class CommonDocument_Base
    extends AbstractCommon
    implements ITypedClass
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CommonDocument.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public CIType getCIType()
        throws EFapsException
    {
        return null;
    }

    /**
     * Assign action.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return assignAction(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);

        final Set<Instance> instances = new HashSet<>();
        if (InstanceUtils.isValid(_parameter.getInstance()) && _parameter.getInstance().getType().isKindOf(command
                        .getTargetConnectAttribute().getLink())) {
            instances.add(_parameter.getInstance());
        } else {
            instances.addAll(getSelectedInstances(_parameter));
        }
        for (final Instance inst : instances) {
            Instance instance = null;
            if (containsProperty(_parameter, "Select4Instance")) {
                final String select4Instance = getProperty(_parameter, "Select4Instance");
                final PrintQuery print = new CachedPrintQuery(inst, getRequestKey())
                                .setLifespanUnit(TimeUnit.SECONDS).setLifespan(30);
                print.addSelect(select4Instance);
                print.executeWithoutAccessCheck();
                final Object obj = print.getSelect(select4Instance);
                if (obj instanceof Instance) {
                    instance = (Instance) obj;
                }
            } else {
                instance = inst;
            }

            final QueryBuilder queryBldr = new QueryBuilder(command.getTargetCreateType());
            queryBldr.addWhereAttrEqValue(command.getTargetConnectAttribute(), instance);
            for (final Instance relInst : queryBldr.getQuery().executeWithoutAccessCheck()) {
                final PrintQuery print = new PrintQuery(relInst);
                print.addAttribute(CIERP.ActionDefinition2DocumentAbstract.FromLinkAbstract,
                                CIERP.ActionDefinition2DocumentAbstract.ToLinkAbstract,
                                CIERP.ActionDefinition2DocumentAbstract.Date);
                print.executeWithoutAccessCheck();
                final Insert insert = new Insert(CIERP.ActionDefinition2DocumentHistorical);
                insert.add(CIERP.ActionDefinition2DocumentHistorical.FromLinkAbstract, print.<Long>getAttribute(
                                CIERP.ActionDefinition2DocumentAbstract.FromLinkAbstract));
                insert.add(CIERP.ActionDefinition2DocumentHistorical.ToLinkAbstract, print.<Long>getAttribute(
                                CIERP.ActionDefinition2DocumentAbstract.ToLinkAbstract));
                insert.add(CIERP.ActionDefinition2DocumentHistorical.Date, print.<DateTime>getAttribute(
                                CIERP.ActionDefinition2DocumentAbstract.Date));
                insert.executeWithoutAccessCheck();
                new Delete(relInst).execute();
            }

            if (InstanceUtils.isValid(Instance.get(_parameter.getParameterValue("action")))) {
                final Parameter parameter = ParameterUtil.clone(_parameter,
                                Parameter.ParameterValues.INSTANCE, instance);
                final Create create = new Create()
                {
                    @Override
                    protected void add2basicInsert(final Parameter _parameter,
                                                   final Insert _insert)
                        throws EFapsException
                    {
                        super.add2basicInsert(_parameter, _insert);
                        final Instance actionInst = Instance.get(_parameter.getParameterValue("action"));
                        if (actionInst.isValid()) {
                            _insert.add(CIERP.ActionDefinition2ObjectAbstract.FromLinkAbstract, actionInst);
                        }
                    }
                };
                final Instance actionInst = create.basicInsert(parameter);
                for (final IOnAction listener : Listener.get().<IOnAction>invoke(IOnAction.class)) {
                    listener.afterAssign(this, parameter, actionInst);
                }

                final Map<Status, Status> mapping = getStatusMapping(parameter);

                if (!mapping.isEmpty()) {
                    final PrintQuery print = new PrintQuery(instance);
                    print.addAttribute(CIERP.DocumentAbstract.StatusAbstract);
                    print.execute();
                    final Status status = Status.get(print.<Long>getAttribute(CIERP.DocumentAbstract.StatusAbstract));
                    if (mapping.containsKey(status)) {
                        final Update update = new Update(instance);
                        update.add(CIERP.DocumentAbstract.StatusAbstract, mapping.get(status));
                        update.execute();
                    }
                }

            }
        }
        return ret;
    }

    /**
     * Validate assign different action.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return validateAssignDifferentAction(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance actionInst = Instance.get(_parameter.getParameterValue("action"));
        if (InstanceUtils.isValid(actionInst)) {
            final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
            final Set<Instance> instances = new HashSet<>();
            if (InstanceUtils.isValid(_parameter.getInstance()) && _parameter.getInstance().getType().isKindOf(command
                            .getTargetConnectAttribute().getLink())) {
                instances.add(_parameter.getInstance());
            } else {
                instances.addAll(getSelectedInstances(_parameter));
            }
            for (final Instance inst : instances) {
                Instance instance = null;
                if (containsProperty(_parameter, "Select4Instance")) {
                    final String select4Instance = getProperty(_parameter, "Select4Instance");
                    final PrintQuery print = new CachedPrintQuery(inst, getRequestKey()).setLifespanUnit(
                                    TimeUnit.SECONDS).setLifespan(30);
                    print.addSelect(select4Instance);
                    print.executeWithoutAccessCheck();
                    final Object obj = print.getSelect(select4Instance);
                    if (obj instanceof Instance) {
                        instance = (Instance) obj;
                    }
                } else {
                    instance = inst;
                }
                final QueryBuilder queryBldr = new QueryBuilder(command.getTargetCreateType());
                queryBldr.addWhereAttrEqValue(command.getTargetConnectAttribute(), instance);
                for (final Instance relInst : queryBldr.getQuery().executeWithoutAccessCheck()) {
                    final PrintQuery print = new PrintQuery(relInst);
                    final SelectBuilder selInst = SelectBuilder.get()
                                    .linkto(CIERP.ActionDefinition2DocumentAbstract.FromLinkAbstract).instance();
                    print.addSelect(selInst);
                    print.executeWithoutAccessCheck();
                    final Instance existingActionInst = print.getSelect(selInst);
                    if (InstanceUtils.isValid(existingActionInst) && existingActionInst.equals(actionInst)) {
                        final List<IWarning> warnings = new ArrayList<>();
                        warnings.add(new AssignDifferentActionWarning());
                        ret.put(ReturnValues.SNIPLETT, WarningUtil.getHtml4Warning(warnings).toString());
                        break;
                    }
                }
            }
        }
        if (!ret.contains(ReturnValues.SNIPLETT)) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Validate selected for assign action.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return validateSelected4AssignAction(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new AccessCheck4UI().check4SelectedOnStatus(_parameter);
        if (ret.isEmpty()) {
            final List<IWarning> warnings = new ArrayList<>();
            warnings.add(new Selected4AssignActionInvalidWarning());
            ret.put(ReturnValues.SNIPLETT, WarningUtil.getHtml4Warning(warnings).toString());
        }
        return ret;
    }

    /**
     * Call action.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return callAction(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        for (final IOnAction listener : Listener.get().<IOnAction>invoke(IOnAction.class)) {
            listener.onDocumentUpdate(_parameter, _parameter.getInstance());
        }
        return ret;
    }

    /**
     * Access check for action.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @return Return containing access
     * @throws EFapsException on error
     */
    public Return accessCheck4Action(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Properties properties = ERP.ACTIONDEF.get();
        final String key;
        if (containsProperty(_parameter, "Key")) {
            key = getProperty(_parameter, "Key");
        } else {
            final Type type = getType4SysConf(_parameter);
            if (type != null) {
                key = type.getName();
            } else {
                key = "Default";
            }
        }
        final String accessDef = properties.getProperty(key);
        if (accessDef != null) {
            String access = getProperty(_parameter, "Access", "NA");
            if (access.startsWith("!")) {
                access = access.substring(1);
                if (!accessDef.equalsIgnoreCase(access)) {
                    ret.put(ReturnValues.TRUE, true);
                }
            } else if (accessDef.equalsIgnoreCase(access)) {
                ret.put(ReturnValues.TRUE, true);
            }
        }
        return ret;
    }

    /**
     * Access check for action relation.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @return Return containing access
     * @throws EFapsException on error
     */
    public Return accessCheck4ActionRelation(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.ActionDefinition2DocumentAbstract);
        queryBldr.addWhereAttrEqValue(CIERP.ActionDefinition2DocumentAbstract.ToLinkAbstract, _parameter.getInstance());
        final InstanceQuery query = queryBldr.getQuery();
        if (query.executeWithoutAccessCheck().isEmpty()) {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Sets the scale for read value.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @return scale corrected BigDecimal
     * @throws EFapsException on error
     */
    public Return setScale4ReadValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Object object = _parameter.get(ParameterValues.OTHERS);
        if (object != null && object instanceof BigDecimal) {
            int scale = 0;
            final Object attr = _parameter.get(ParameterValues.CLASS);
            if (attr instanceof Attribute) {
                final Type type = ((Attribute) attr).getParent();
                final String frmtKey = getProperty(_parameter, "Formatter");
                final DecimalFormat formatter;
                if ("total".equalsIgnoreCase(frmtKey)) {
                    formatter = NumberFormatter.get().getFrmt4Total(type);
                } else if ("discount".equalsIgnoreCase(frmtKey)) {
                    formatter = NumberFormatter.get().getFrmt4Discount(type);
                } else if ("quantity".equalsIgnoreCase(frmtKey)) {
                    formatter = NumberFormatter.get().getFrmt4Quantity(type);
                } else if ("unit".equalsIgnoreCase(frmtKey)) {
                    formatter = NumberFormatter.get().getFrmt4UnitPrice(type);
                } else if (frmtKey != null) {
                    formatter = NumberFormatter.get().getFrmt4Key(type.getName(), frmtKey);
                } else {
                    formatter = NumberFormatter.get().getFormatter();
                }
                if (formatter != null) {
                    final int scaleTmp = ((BigDecimal) object).scale();
                    if (scaleTmp > formatter.getMaximumFractionDigits()) {
                        final BigDecimal decTmp = ((BigDecimal) object).stripTrailingZeros();
                        if (decTmp.scale() <  formatter.getMinimumFractionDigits()) {
                            scale = formatter.getMinimumFractionDigits();
                        } else if (decTmp.scale() > formatter.getMaximumFractionDigits()) {
                            scale = formatter.getMaximumFractionDigits();
                        } else {
                            scale = decTmp.scale();
                        }
                    } else if (scaleTmp < formatter.getMinimumFractionDigits()) {
                        scale = formatter.getMinimumFractionDigits();
                    } else {
                        scale = scaleTmp;
                    }
                }
            }
            ret.put(ReturnValues.VALUES, ((BigDecimal) object).setScale(scale, RoundingMode.HALF_UP));
        }
        return ret;
    }

    /**
     * Gets the sales person field value.
     *
     * @param _parameter    Parameter as passed by the eFasp API
     * @return Sales Person Field Value
     * @throws EFapsException on error
     */
    public Return getSalesPersonFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final org.efaps.esjp.common.uiform.Field field = new org.efaps.esjp.common.uiform.Field() {

            @Override
            public DropDownPosition getDropDownPosition(final Parameter _parameter,
                                                           final Object _value,
                                                           final Object _option)
                throws EFapsException
            {
                final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
                final IUIValue uiValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
                final DropDownPosition pos;
                if (TargetMode.EDIT.equals(_parameter.get(ParameterValues.ACCESSMODE))) {
                    final Long persId = (Long) uiValue.getObject();
                    pos = new DropDownPosition(_value, _option).setSelected(_value.equals(persId));
                } else if ("true".equalsIgnoreCase((String) props.get("SelectCurrent"))) {
                    long persId = 0;
                    try {
                        persId = Context.getThreadContext().getPerson().getId();
                    } catch (final EFapsException e) {
                        // nothing must be done at all
                        Field_Base.LOG.error("Catched error", e);
                    }
                    pos = new DropDownPosition(_value, _option).setSelected(Long.valueOf(persId).equals(_value));
                } else {
                    pos = super.getDropDownPosition(_parameter, _value, _option);
                }
                return pos;
            }

            @Override
            protected void add2QueryBuilder4List(final Parameter _parameter,
                                                 final QueryBuilder _queryBldr)
                throws EFapsException
            {
                final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
                final String rolesStr = (String) props.get("Roles");
                final String groupsStr = (String) props.get("Groups");
                if (rolesStr != null && !rolesStr.isEmpty()) {
                    final String[] roles = rolesStr.split(";");
                    final List<Long> roleIds = new ArrayList<>();
                    for (final String role : roles) {
                        final Role aRole = Role.get(role);
                        if (aRole != null) {
                            roleIds.add(aRole.getId());
                        }
                    }
                    if (!roleIds.isEmpty()) {
                        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person2Role);
                        queryBldr.addWhereAttrEqValue(CIAdminUser.Person2Role.UserToLink, roleIds.toArray());

                        _queryBldr.addWhereAttrInQuery(CIAdminUser.Abstract.ID,
                                        queryBldr.getAttributeQuery(CIAdminUser.Person2Role.UserFromLink));
                    }
                }
                if (groupsStr != null && !groupsStr.isEmpty()) {
                    final String[] groups;
                    boolean and = true;
                    if (groupsStr.contains("|")) {
                        groups = groupsStr.split("\\|");
                    } else {
                        groups = groupsStr.split(";");
                        and = false;
                    }

                    final List<Long> groupIds = new ArrayList<>();
                    for (final String group : groups) {
                        final Group aGroup = Group.get(group);
                        if (aGroup != null) {
                            groupIds.add(aGroup.getId());
                        }
                    }
                    if (!groupIds.isEmpty()) {
                        if (and) {
                            for (final Long group : groupIds) {
                                final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person2Group);
                                queryBldr.addWhereAttrEqValue(CIAdminUser.Person2Group.UserToLink, group);
                                final AttributeQuery attribute = queryBldr
                                                        .getAttributeQuery(CIAdminUser.Person2Group.UserFromLink);
                                _queryBldr.addWhereAttrInQuery(CIAdminUser.Abstract.ID, attribute);
                            }
                        } else {
                            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person2Group);
                            queryBldr.addWhereAttrEqValue(CIAdminUser.Person2Group.UserToLink, groupIds.toArray());
                            final AttributeQuery attribute = queryBldr
                                                        .getAttributeQuery(CIAdminUser.Person2Group.UserFromLink);
                            _queryBldr.addWhereAttrInQuery(CIAdminUser.Abstract.ID, attribute);
                        }

                    }
                }
                super.add2QueryBuilder4List(_parameter, _queryBldr);
            }
        };
        return field.getOptionListFieldValue(_parameter);
    }

    /**
     * A Script that removes the table rows,
     * but lives if functional for editing via script etc.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableRemoveScript(final Parameter _parameter,
                                                 final String _tableName)
    {
        return getTableRemoveScript(_parameter, _tableName, false, false);
    }

    /**
     * A Script that removes the table rows,
     * but lives if functional for editing via script etc.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @param _onDomReady add onDomReady script part
     * @param _wrapInTags wrap in script tags
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableRemoveScript(final Parameter _parameter,
                                                 final String _tableName,
                                                 final boolean _onDomReady,
                                                 final boolean _wrapInTags)
    {
        return getTableRemoveScript(_parameter, _tableName, _onDomReady, _wrapInTags, false);
    }


    /**
     * A Script that removes the table rows,
     * but lives if functional for editing via script etc.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @param _onDomReady add onDomReady script part
     * @param _wrapInTags wrap in script tags
     * @param _makeUneditable make the table uneditable
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableRemoveScript(final Parameter _parameter,
                                                 final String _tableName,
                                                 final boolean _onDomReady,
                                                 final boolean _wrapInTags,
                                                 final boolean _makeUneditable)
    {
        final StringBuilder ret = new StringBuilder();
        if (_wrapInTags) {
            ret.append("<script type=\"text/javascript\">\n");
        }
        if (_onDomReady) {
            ret.append("require([\"dojo/domReady!\"], function(){\n");
        }
        ret.append("require([\"dojo/_base/lang\",\"dojo/dom\", \"dojo/dom-construct\",")
                .append("\"dojo/query\",\"dojo/NodeList-traverse\", \"dojo/NodeList-dom\"],")
                .append(" function(lang, dom, domConstruct, query){\n")
            .append("  var tN = \"").append(_tableName).append("\";\n")
            .append("  for (i = 100;i < 10000; i = i + 100) {\n")
            .append("    if( lang.exists(\"eFapsTable\" + i) ){\n")
            .append("      var tO = window[\"eFapsTable\" + i];\n")
            .append("      if (tO.tableName == tN) {\n")
            .append("         var tB = dom.byId(tO.bodyID);\n")
            .append("        query(\".eFapsTableRemoveRowCell\", tB).parent().forEach(domConstruct.destroy);\n");

        if (_makeUneditable) {
            ret.append("        query(\"div\", tB).style(\"display\", \"none\");");
        }

        ret.append("      }\n")
            .append("    } else {\n")
            .append("      break;\n")
            .append("    }\n")
            .append("  }\n")
            .append("});");

        if (_onDomReady) {
            ret.append("});\n");
        }

        if (_wrapInTags) {
            ret.append("</script>\n");
        }
        return ret;
    }

    /**
     * A Script that deactivates the table by hiding the add and remove buttons,
     * but lives if functional for editing via script etc.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableDeactivateScript(final Parameter _parameter,
                                                     final String _tableName)
    {
        return getTableDeactivateScript(_parameter, _tableName, false, false);
    }

    /**
     * A Script that deactivates the table by hiding the add and remove buttons,
     * but lives if functional for editing via script etc.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @param _onDomReady add onDomReady script part
     * @param _wrapInTags wrap in script tags
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableDeactivateScript(final Parameter _parameter,
                                                     final String _tableName,
                                                     final boolean _onDomReady,
                                                     final boolean _wrapInTags)
    {
        final StringBuilder ret = new StringBuilder();
        if (_wrapInTags) {
            ret.append("<script type=\"text/javascript\">\n");
        }
        if (_onDomReady) {
            ret.append("require([\"dojo/domReady!\"], function(){\n");
        }
        ret.append("require([\"dojo/_base/lang\",\"dojo/dom\", \"dojo/query\",\"dojo/NodeList-traverse\", ")
                .append("\"dojo/NodeList-dom\"], function(lang, dom, query){\n")
            .append("  tableName = \"").append(_tableName).append("\";\n")
            .append("  for (i=100;i<10000;i=i+100) {\n")
            .append("    if( lang.exists(\"eFapsTable\" + i) ){\n")
            .append("      var tO = window[\"eFapsTable\" + i];\n")
            .append("      if (tO.tableName == tableName) {\n")
            .append("        tableBody = dom.byId(tO.bodyID);\n")
            .append("        query(\".eFapsTableRemoveRowCell > *\", tableBody).style({ display:\"none\" });\n")
            .append("          query(\"div\", tableBody).at(-1).style(\"display\",\"none\");\n")
            .append("        }\n")
            .append("      } else {\n")
            .append("        break;\n")
            .append("      }\n")
            .append("  }\n")
            .append("});");

        if (_onDomReady) {
            ret.append("});\n");
        }

        if (_wrapInTags) {
            ret.append("</script>\n");
        }
        return ret;
    }

    /**
     * A Script adds one new empty Row to the given table.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table the row will be added to
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableAddNewRowScript(final Parameter _parameter,
                                                     final String _tableName)
    {
        return getTableAddNewRowsScript(_parameter, _tableName, null, null);
    }

    /**
     * A Script adds new Rows to the given table and fills it with values.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @param _values values to be used
     * @param _onComplete script to be added on complete
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableAddNewRowsScript(final Parameter _parameter,
                                                     final String _tableName,
                                                     final Collection<Map<String, Object>> _values,
                                                     final StringBuilder _onComplete)
    {
        return getTableAddNewRowsScript(_parameter, _tableName, _values, _onComplete, false, false, null);
    }

    /**
     * Gets the table add new rows script.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName the table name
     * @param _values the values
     * @param _onComplete the on complete
     * @param _onDomReady the on dom ready
     * @param _wrapInTags the wrap in tags
     * @param _nonEscapeFields the non escape fields
     * @return the table add new rows script
     */
    protected StringBuilder getTableAddNewRowsScript(final Parameter _parameter,
                                                     final String _tableName,
                                                     final Collection<Map<String, Object>> _values,
                                                     final StringBuilder _onComplete,
                                                     final boolean _onDomReady,
                                                     final boolean _wrapInTags,
                                                     final Set<String> _nonEscapeFields)
    {
        return getTableAddNewRowsScript(_parameter, _tableName, _values, _onComplete, _onDomReady, _wrapInTags,
                        _nonEscapeFields, null);
    }


    /**
     * A Script adds new Rows to the given table and fills it with values.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @param _values values to be used
     * @param _onComplete script to be added on complete
     * @param _onDomReady add onDomReady script part
     * @param _wrapInTags wrap in script tags
     * @param _nonEscapeFields set of fields for which the escape must not apply
     * @param _extraParameter the extra parameter
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableAddNewRowsScript(final Parameter _parameter,
                                                     final String _tableName,
                                                     final Collection<Map<String, Object>> _values,
                                                     final StringBuilder _onComplete,
                                                     final boolean _onDomReady,
                                                     final boolean _wrapInTags,
                                                     final Set<String> _nonEscapeFields,
                                                     final String _extraParameter)
    {
        final StringBuilder ret = new StringBuilder();
        if (_wrapInTags) {
            ret.append("<script type=\"text/javascript\">\n");
        }
        if (_onDomReady) {
            ret.append("require([\"dojo/domReady!\"], function(){\n");
        }
        if (_values == null) {
            ret.append(" addNewRows_").append(_tableName).append("(").append(1).append(",function(){\n");
        } else {
            ret.append(" addNewRows_").append(_tableName).append("(").append(_values.size()).append(",function(){\n")
                .append(getSetFieldValuesScript(_parameter, _values, _nonEscapeFields));
        }
        if (_onComplete != null) {
            ret.append(_onComplete);
        }
        ret.append("\n}, null");

        if (_extraParameter != null) {
            ret.append(",").append(_extraParameter);
        }
        ret.append(" );\n");

        if (_onDomReady) {
            ret.append("\n});\n");
        }
        if (_wrapInTags) {
            ret.append("</script>\n");
        }
        return ret;
    }

    /**
     * A Script that sets values.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _values values to be used
     * @param _nonEscapeFields set of fields for which the escape must not apply
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getSetFieldValuesScript(final Parameter _parameter,
                                                    final Collection<Map<String, Object>> _values,
                                                    final Set<String> _nonEscapeFields)
    {
        final StringBuilder ret = new StringBuilder();
        int i = 0;
        for (final Map<String, Object> values : _values) {
            for (final Entry<String, Object> entry : values.entrySet()) {
                final String value;
                final String label;
                if (entry.getValue() instanceof String[] && ((String[]) entry.getValue()).length == 2) {
                    value = ((String[]) entry.getValue())[0];
                    label = ((String[]) entry.getValue())[1];
                } else {
                    value = String.valueOf(entry.getValue());
                    label = null;
                }

                ret.append(getSetFieldValue(i, entry.getKey(), value, label, _nonEscapeFields == null ? true
                                : !_nonEscapeFields.contains(entry.getKey()))).append("\n");
            }
            i++;
        }
        return ret;
    }

    /**
     * Get a "eFapsSetFieldValue" Javascript line.
     * @param _idx          index of the field
     * @param _fieldName    name of the field
     * @param _value        value
     * @return StringBuilder
     */
    protected StringBuilder getSetFieldValue(final int _idx,
                                             final String _fieldName,
                                             final String _value)
    {
        return getSetFieldValue(_idx, _fieldName, _value, null, true);
    }


    /**
     * Get a "eFapsSetFieldValue" Javascript line.
     * @param _idx          index of the field
     * @param _fieldName    name of the field
     * @param _value        value of the field
     * @param _label        visible value (label) of the field
     * @return StringBuilder
     */
    protected StringBuilder getSetFieldValue(final int _idx,
                                             final String _fieldName,
                                             final String _value,
                                             final String _label)
    {
        return getSetFieldValue(_idx, _fieldName, _value, _label, true);
    }

    /**
     * Get a "eFapsSetFieldValue" Javascript line.
     * @param _idx          index of the field
     * @param _fieldName    name of the field
     * @param _value        value
     * @param _label        shown value
     * @param _escape       must the value be escaped
     * @return StringBuilder
     */
    protected StringBuilder getSetFieldValue(final int _idx,
                                             final String _fieldName,
                                             final String _value,
                                             final String _label,
                                             final boolean _escape)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("eFapsSetFieldValue(").append(_idx).append(",'").append(_fieldName).append("',");
        if (_escape) {
            ret.append("'").append(StringEscapeUtils.escapeEcmaScript(_value)).append("'");
        } else {
            ret.append(_value);
        }
        if (_label != null) {
            if (_escape) {
                ret.append(",'").append(StringEscapeUtils.escapeEcmaScript(_label)).append("'");
            } else {
                ret.append(",").append(_label);
            }
        }
        ret.append(");");
        return ret;
    }

    protected StringBuilder getSetFieldValue(final String fieldName,
                                             final boolean escape,
                                             final String selectedValue,
                                             final String... valueLabels)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("eFapsSetFieldValue(").append(0).append(",'").append(fieldName)
                        .append("', new Array('").append(selectedValue == null ? valueLabels[0] : selectedValue)
                        .append("'");
        for (int i = 0; i < valueLabels.length; i = i + 2) {
            ret.append(",");
            if (escape) {
                ret.append("'").append(StringEscapeUtils.escapeEcmaScript(valueLabels[i])).append("'");
            } else {
                ret.append(valueLabels[i]);
            }

            if (escape) {
                ret.append(",'").append(StringEscapeUtils.escapeEcmaScript(valueLabels[i + 1])).append("'");
            } else {
                ret.append(",").append(valueLabels[i + 1]);
            }
        }
        ret.append("));");
        return ret;
    }

    /**
     * JavaScript Snipplet that removes from all SELECT with
     * <code>fieldname</code> all other options except the one
     * identified by the given <code>_idvalue</code>.
     *
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _field        fieldname
     * @param _idvalue      value that will be set for the dropdown
     * @return JavaScript
     * @throws EFapsException on error
     */
    public StringBuilder getSetDropDownScript(final Parameter _parameter,
                                              final String _field,
                                              final String _idvalue)
        throws EFapsException
    {
        return getSetDropDownScript(_parameter, _field, _idvalue, null);
    }

    /**
     * JavaScript Snipplet that removes from the  SELECT with
     * <code>fieldname</code> and index <code>_idx</code> (if null from all)
     * the other options except the one identified by the given
     * <code>_idvalue</code>.
     *
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _field        fieldname
     * @param _idvalue      value that will be set for the dropdown
     * @param _idx          index
     * @return JavaScript
     * @throws EFapsException on error
     */
    public StringBuilder getSetDropDownScript(final Parameter _parameter,
                                              final String _field,
                                              final String _idvalue,
                                              final Integer _idx)
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder()
            .append("require([\"dojo/query\", \"dojo/dom-construct\",\"dojo/dom-class\"], ")
                .append("function(query, domConstruct, domClass) {")
            .append("var nl = query(\" select[name='").append(_field).append("']\");");

        if (_idx == null) {
            js.append("nl.addClass(\"eFapsReadOnly\");")
                .append("query(\" select[name='").append(_field).append("'] *\").forEach(function(node){")
                .append("if (node.value!='").append(_idvalue).append("') {")
                .append("domConstruct.destroy(node);")
                .append("}")
                .append("});");
        } else {
            js.append("if (nl[").append(_idx).append("]!=undefined) {")
                .append("domClass.add(nl[").append(_idx).append("], \"eFapsReadOnly\");")
                .append("query(\"*\", nl[").append(_idx).append("]).forEach(function(node){")
                .append("if (node.value!='").append(_idvalue).append("') {")
                .append("domConstruct.destroy(node);")
                .append("}")
                .append("});")
                .append("}");
        }
        js.append("});");
        return js;
    }

    /**
     * JavaScript Snipplet that sets all INPUTS or TEXTARES
     * with name <code>fieldname</code> to readOnly and assigns class
     * eFapsReadOnly.
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _field        fieldnames
     * @return JavaScript
     * @throws EFapsException on error
     */
    public StringBuilder getSetFieldReadOnlyScript(final Parameter _parameter,
                                                   final String... _field)
        throws EFapsException
    {
        return getSetFieldReadOnlyScript(_parameter, null, _field);
    }

    /**
     * JavaScript Snipplet that sets INPUTS or TEXTARES
     * with name <code>fieldname</code> and Index <code>_idx</code>
     * to readOnly and assigns class eFapsReadOnly. If <code>_idx</code> all
     * are are set.
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _idx          index of the field to be set to readonly
     * @param _field        fieldnames
     * @return JavaScript
     * @throws EFapsException on error
     */
    public StringBuilder getSetFieldReadOnlyScript(final Parameter _parameter,
                                                   final Integer _idx,
                                                   final String... _field)
        throws EFapsException
    {
        return getSetFieldReadOnlyScript(_parameter, _idx, true, _field);
    }

    /**
     * JavaScript Snipplet that sets INPUTS or TEXTARES
     * with name <code>fieldname</code> and Index <code>_idx</code>
     * to readOnly and assigns class eFapsReadOnly. If <code>_idx</code> all
     * are are set.
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _idx          index of the field to be set to readonly
     * @param _readOnly     readonly or not
     * @param _field        fieldnames
     * @return JavaScript
     * @throws EFapsException on error
     */
    public StringBuilder getSetFieldReadOnlyScript(final Parameter _parameter,
                                                   final Integer _idx,
                                                   final boolean _readOnly,
                                                   final String... _field)
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder();

        for (final String field : _field) {
            js.append("var nl = query(\" input[name='").append(field).append("'], textarea[name='")
                .append(field).append("']\");");
            if (_idx == null) {
                js.append("nl.forEach(function(node){")
                    .append("if (node.type===\"hidden\") {")
                    .append("var pW = registry.getEnclosingWidget(node);")
                    .append("if (typeof(pW) !== \"undefined\") {")
                    .append("if (pW.isInstanceOf(AutoComplete) || pW.isInstanceOf(AutoSuggestion)) {")
                    .append("pW.set('readOnly', ").append(_readOnly).append(");")
                    .append("}")
                    .append("}")
                    .append("} else {")
                    .append("node.readOnly =").append(_readOnly).append(";")
                    .append("}")
                    .append("});\n");
            } else {
                js.append("if (nl[").append(_idx).append("]!=undefined) {")
                    .append("nl[").append(_idx).append("].readOnly =").append(_readOnly).append(";")
                    .append("}\n");
            }
        }
        if (_readOnly) {
            js.append("query(\" input[readonly=''], textarea[readonly='']\").addClass(\"eFapsReadOnly\");");
        } else {
            js.append("query(\".eFapsReadOnly\").forEach(function(_node) {")
                .append("if (!_node.readOnly) {")
                .append("domClass.remove(_node, \"eFapsReadOnly\");")
                .append("}")
                .append("});");
        }
        return InterfaceUtils.wrapInDojoRequire(_parameter, js, DojoLibs.QUERY, DojoLibs.REGISTRY,
                        DojoLibs.AUTOCOMP, DojoLibs.AUTOSUGG, DojoLibs.DOMCLASS, DojoLibs.NLDOM);
    }


    /**
     * Get a String Array for UoM Field.
     * @param _selected selected id
     * @param _dimId    id of the Dimension
     * @return String for UoM DropDown
     * @throws CacheReloadException on error
     */
    protected String getUoMFieldStr(final long _selected,
                                    final long _dimId)
        throws CacheReloadException
    {
        final Dimension dim = Dimension.get(_dimId);
        final StringBuilder js = new StringBuilder();

        js.append("new Array('").append(_selected).append("'");

        for (final UoM uom : dim.getUoMs()) {
            js.append(",'").append(uom.getId()).append("','").append(uom.getName()).append("'");
        }
        js.append(")");
        return js.toString();
    }

    /**
     * Get a String Array for UoM Field.
     * @param _dimId    id of the Dimension
     * @return String for UoM DropDown
     * @throws CacheReloadException on error
     */
    protected String getUoMFieldStr(final long _dimId)
        throws CacheReloadException
    {
        final Dimension dim = Dimension.get(_dimId);
        return getUoMFieldStr(dim.getBaseUoM().getId(), _dimId);
    }

    /**
     * Gets the uo M field str by uo M.
     *
     * @param _uoMId id of the UoM
     * @return Field String
     * @throws CacheReloadException on error
     */
    protected String getUoMFieldStrByUoM(final long _uoMId)
        throws CacheReloadException
    {
        return getUoMFieldStr(_uoMId, Dimension.getUoM(_uoMId).getDimId());
    }

    /**
     * Adds the status two doc create.
     *
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _insert       insert to add to
     * @param _createdDoc   document created
     * @throws EFapsException on error
     */
    protected void addStatus2DocCreate(final Parameter _parameter,
                                       final Insert _insert,
                                       final CreatedDoc _createdDoc)
        throws EFapsException
    {
        Status status = null;
        //  first check if set via SystemConfiguration if not set lookup the properties
        final Properties properties = ERP.DOCSTATUSCREATE.get();
        final Type type = getType4DocCreate(_parameter);
        if (type != null) {
            final String key = properties.getProperty(type.getName() + ".Status");
            if (key != null) {
                status = Status.find(type.getStatusAttribute().getLink().getUUID(), key);
            }
        }
        if (status == null) {
            final List<Status> statusList = getStatusListFromProperties(_parameter);
            if (!statusList.isEmpty()) {
                status = statusList.get(0);
            }
        }
        if (status != null) {
            _insert.add(getType4DocCreate(_parameter).getStatusAttribute(), status);
        }
    }

    /**
     * Adds the status two doc edit.
     *
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _update       insert to add to
     * @param _editDoc      document edited
     * @throws EFapsException on error
     */
    protected void addStatus2DocEdit(final Parameter _parameter,
                                     final Update _update,
                                     final EditedDoc _editDoc)
        throws EFapsException
    {
        final Type type = _update.getInstance().getType();
        if (type.isCheckStatus()) {
            final String attrName = type.getStatusAttribute().getName();
            final String fieldName = getFieldName4Attribute(_parameter, attrName);
            final String statusTmp = _parameter.getParameterValue(fieldName);
            final Instance inst = Instance.get(statusTmp);
            Status status = null;
            if (inst.isValid()) {
                status = Status.get(inst.getId());
            } else if (statusTmp != null && !statusTmp.isEmpty()) {
                try {
                    final Long statusId = Long.valueOf(statusTmp);
                    status = Status.get(statusId);
                } catch (final NumberFormatException e) {
                    Log.warn("Catched NumberFormatException");
                }
            }
            if (status != null) {
                _update.add(type.getStatusAttribute(), status);
            }
        }
    }

    /**
     * Get the type used to create the new Payment Document.
     * @param _parameter Parameter as passed by the eFaps API
     * @return Type use for the insert
     * @throws EFapsException on error
     */
    protected Type getType4DocCreate(final Parameter _parameter)
        throws EFapsException
    {
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        return command.getTargetCreateType();
    }

    /**
     * Creates the report.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _createdDoc   document created
     * @return the created file
     * @throws EFapsException on error
     */
    protected File createReport(final Parameter _parameter,
                                final CreatedDoc _createdDoc)
        throws EFapsException
    {
        File ret = null;
        if ((containsProperty(_parameter, "JasperReport") || containsProperty(_parameter, "JasperKey")
                        || containsProperty(_parameter, "JasperConfig"))
                        && _createdDoc.getInstance() != null && _createdDoc.getInstance().isValid()) {
            try {
                final StandartReport report = new StandartReport();
                _parameter.put(ParameterValues.INSTANCE, _createdDoc.getInstance());
                final PrintQuery print = new PrintQuery(_createdDoc.getInstance());
                print.addAttribute(CIERP.DocumentAbstract.Name);
                print.executeWithoutAccessCheck();
                final var name = print.<String>getAttribute(CIERP.DocumentAbstract.Name);
                final String fileName = DBProperties.getProperty(_createdDoc.getInstance().getType().getLabelKey(),
                                "es") + "_" + name;
                report.setFileName(fileName);
                add2Report(_parameter, _createdDoc, report);
                ret = report.getFile(_parameter);

                ret = new FileUtil().convertPdf(_parameter, ret, fileName);
                if (ret != null) {
                    final InputStream input = new FileInputStream(ret);
                    final Checkin checkin = new Checkin(_createdDoc.getInstance());
                    checkin.execute(fileName + "." + report.getMime(_parameter).getExtension(), input,
                                    ((Long) ret.length()).intValue());
                }
            } catch (final FileNotFoundException e) {
                CommonDocument_Base.LOG.error("Catched FileNotFoundException", e);
            }
        }
        return ret;
    }

    /**
     * Creates the report.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the created file
     * @throws EFapsException on error
     */
    public Return createReport(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StandartReport report = new StandartReport();

        if (InstanceUtils.isKindOf(_parameter.getInstance(), CIERP.DocumentAbstract)) {
            final PrintQuery print = new PrintQuery(_parameter.getInstance());
            print.addAttribute(CIERP.DocumentAbstract.Name);
            print.execute();
            final String name = print.<String>getAttribute(CIERP.DocumentAbstract.Name);
            final String fileName = DBProperties.getProperty(_parameter.getInstance().getType().getLabelKey(), "es")
                            + "_" + name;
            report.setFileName(fileName);
        }
        add2Report(_parameter, null, report);
        File file = report.getFile(_parameter);

        if (BooleanUtils.toBoolean(getProperty(_parameter, "Checkin"))) {
            try {
                file = new FileUtil().convertPdf(_parameter, file, report.getFileName());
                if (file != null) {
                    final InputStream input = new FileInputStream(file);
                    final Checkin checkin = new Checkin(_parameter.getInstance());
                    checkin.execute(report.getFileName() + "." + report.getMime(_parameter).getExtension(), input,
                                    ((Long) file.length()).intValue());
                }
            } catch (final FileNotFoundException e) {
                CommonDocument_Base.LOG.error("Catched FileNotFoundException", e);
            }
        }
        ret.put(ReturnValues.VALUES, file);
        ret.put(ReturnValues.TRUE, true);
        return ret;
    }

    /**
     * Adds the two report.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _createdDoc   document created
     * @param _report report
     * @throws EFapsException on error
     */
    protected void add2Report(final Parameter _parameter,
                              final CreatedDoc _createdDoc,
                              final StandartReport _report)
        throws EFapsException
    {
        final String companyName = ERP.COMPANY_NAME.get();
        if (companyName != null && !companyName.isEmpty()) {
            _report.getJrParameters().put("CompanyName", companyName);
        }
        final String companyTaxNum = ERP.COMPANY_TAX.get();
        if (companyTaxNum != null && !companyTaxNum.isEmpty()) {
            _report.getJrParameters().put("CompanyTaxNum", companyTaxNum);
        }
        final String companyActivity = ERP.COMPANY_ACTIVITY.get();
        if (companyActivity != null && !companyActivity.isEmpty()) {
            _report.getJrParameters().put("CompanyActivity", companyActivity);
        }
        final String companyStreet = ERP.COMPANY_STREET.get();
        if (companyStreet != null && !companyStreet.isEmpty()) {
            _report.getJrParameters().put("CompanyStreet", companyStreet);
        }
        final String companyRegion = ERP.COMPANY_REGION.get();
        if (companyRegion != null && !companyRegion.isEmpty()) {
            _report.getJrParameters().put("CompanyRegion", companyRegion);
        }
        final String companyCity = ERP.COMPANY_CITY.get();
        if (companyCity != null && !companyCity.isEmpty()) {
            _report.getJrParameters().put("CompanyCity", companyCity);
        }
        final String companyDistrict = ERP.COMPANY_DISTRICT.get();
        if (companyDistrict != null && !companyDistrict.isEmpty()) {
            _report.getJrParameters().put("CompanyDistrict", companyDistrict);
        }
        final String companyLogo = ERP.COMPANY_JASPERLOGO.get();
        if (companyLogo != null && !companyLogo.isEmpty()) {
            _report.getJrParameters().put("CompanyLogo", companyLogo);
        }
    }

    /**
     * Method is called in the process of creation of a Document.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _insert   insert to add to
     * @param _createdDoc   document created
     * @throws EFapsException on error
     */
    protected void add2DocCreate(final Parameter _parameter,
                                 final Insert _insert,
                                 final CreatedDoc _createdDoc)
        throws EFapsException
    {
        // used by implementation
    }


    /**
     * Method is called in the process of edit of a Document.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _update   update to add to
     * @param _editDoc   document edited
     * @throws EFapsException on error
     */
    protected void add2DocEdit(final Parameter _parameter,
                               final Update _update,
                               final EditedDoc _editDoc)
        throws EFapsException
    {
        // used by implementation
    }

    /**
     * Gets the field name for attribute.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _attributeName attributerName the FieldName is wanted for
     * @return fieldname
     * @throws EFapsException on error
     */
    protected String getFieldName4Attribute(final Parameter _parameter,
                                            final String _attributeName)
        throws EFapsException
    {
        return StringUtils.uncapitalize(_attributeName);
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return new Name
     * @throws EFapsException on error
     */
    protected String getDocName4Create(final Parameter _parameter)
        throws EFapsException
    {
        //first priority are values from the UserInterface
        final String fieldName = getFieldName4Attribute(_parameter, CIERP.DocumentAbstract.Name.name);
        String ret = _parameter.getParameterValue(fieldName);
        if (StringUtils.isEmpty(ret)) {
            ret = _parameter.getParameterValue(fieldName + "4Create");
        }
        if (StringUtils.isEmpty(ret)) {
            final Type type = getType4DocCreate(_parameter);
            ret = new Naming().fromNumberGenerator(_parameter, type.getName());
        }
        return ret;
    }

    /**
     * Gets the type for sys conf.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return type
     * @throws EFapsException on error
     */
    protected Type getType4SysConf(final Parameter _parameter)
        throws EFapsException
    {
        return CIERP.DocumentAbstract.getType();
    }

    /**
     * Gets the rate.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo  rateinfo
     * @return rate value
     * @throws EFapsException on error
     */
    protected BigDecimal getRate(final Parameter _parameter,
                                 final RateInfo _rateInfo)
        throws EFapsException
    {
        final Type type = getType4SysConf(_parameter);
        return RateInfo.getRate(_parameter, _rateInfo, type.getName());
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo  rateinfo
     * @return formated rate
     * @throws EFapsException on error
     */
    protected String getRateFrmt(final Parameter _parameter,
                                 final RateInfo _rateInfo)
        throws EFapsException
    {
        final Type type = getType4SysConf(_parameter);
        return RateInfo.getRateFrmt(_parameter, _rateInfo, type.getName());
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo  rateinfo
     * @return rate value for UserInterface
     * @throws EFapsException on error
     */
    protected BigDecimal getRateUI(final Parameter _parameter,
                                   final RateInfo _rateInfo)
        throws EFapsException
    {
        final Type type = getType4SysConf(_parameter);
        return RateInfo.getRateUI(_parameter, _rateInfo, type.getName());
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo  rateinfo
     * @return fromatted rate string for UserInterface
     * @throws EFapsException on error
     */
    protected String getRateUIFrmt(final Parameter _parameter,
                                   final RateInfo _rateInfo)
        throws EFapsException
    {
        final Type type = getType4SysConf(_parameter);
        return RateInfo.getRateUIFrmt(_parameter, _rateInfo, type.getName());
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo  rateinfo
     * @return formated rate
     * @throws EFapsException on error
     */
    protected Object[] getRateObject(final Parameter _parameter,
                                     final RateInfo _rateInfo)
        throws EFapsException
    {
        final Type type = getType4SysConf(_parameter);
        return RateInfo.getRateObject(_parameter, _rateInfo, type.getName());
    }

    /**
     * Gets the java script for edit massive table.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return new Name
     * @throws EFapsException on error
     */
    public Return getJavaScript4EditMassiveTable(final Parameter _parameter)
        throws EFapsException
    {
        final Return retVal = new Return();
        retVal.put(ReturnValues.SNIPLETT,
                        getTableDeactivateScript(_parameter, "inventoryTable", true, true).toString());
        return retVal;
    }

    /**
     * Add additional values to the map passed to the process prior to
     * execution.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @param _createdDoc CreatedDoc the process must be executed for
     * @throws EFapsException on error
     */
    public void connect2Object(final Parameter _parameter,
                               final CreatedDoc _createdDoc)
        throws EFapsException
    {
        final Map<Integer, String> connectTypes = analyseProperty(_parameter, "ConnectType");
        if (!connectTypes.isEmpty()) {
            final Map<Integer, String> currentLinks = analyseProperty(_parameter, "ConnectCurrentLink");
            final Map<Integer, String> foreignLinks = analyseProperty(_parameter, "ConnectForeignLink");
            final Map<Integer, String> foreignFields = analyseProperty(_parameter, "ConnectForeignField");
            // all must be of the same size
            if (connectTypes.size() == currentLinks.size() && foreignLinks.size() == foreignFields.size()
                            && connectTypes.size() == foreignLinks.size()) {
                for (final Entry<Integer, String> entry: connectTypes.entrySet()) {
                    final String[] foreigns;
                    final String foreignField =  foreignFields.get(entry.getKey());
                    if ("CALLINSTANCE".equals(foreignField)) {
                        foreigns = new String[] { _parameter.getCallInstance().getOid() };
                    } else {
                        foreigns = _parameter.getParameterValues(foreignFields.get(entry.getKey()));
                    }
                    if (foreigns != null) {
                        for (final String foreign : foreigns) {
                            if (!foreign.isEmpty()) {
                                final String typeStr = entry.getValue();
                                final Insert insert;
                                if (isUUID(typeStr)) {
                                    insert = new Insert(UUID.fromString(typeStr));
                                } else {
                                    insert = new Insert(typeStr);
                                }
                                insert.add(currentLinks.get(entry.getKey()), _createdDoc.getInstance());

                                final Instance inst = Instance.get(foreign);
                                if (inst.isValid()) {
                                    insert.add(foreignLinks.get(entry.getKey()), inst);
                                } else {
                                    insert.add(foreignLinks.get(entry.getKey()), foreign);
                                }
                                add2connect2Object(_parameter, _createdDoc, insert);
                                insert.execute();
                            }
                        }
                    }
                }
            } else {
                CommonDocument_Base.LOG.error("The properties must be of the same size!");
            }
        }
    }


    /**
     * Add additional values to the Update generated by connect definition.
     *
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _createdDoc   CreatedDoc the process must be executed for
     * @param _update       update 2 add to
     * @throws EFapsException on error
     */
    protected void add2connect2Object(final Parameter _parameter,
                                      final CreatedDoc _createdDoc,
                                      final Update _update)
        throws EFapsException
    {

    }

    /**
     * Update connection 2 object.
     *
     * @param _parameter the _parameter
     * @param _editedDoc the _edited doc
     * @throws EFapsException on error
     */
    public void updateConnection2Object(final Parameter _parameter,
                                        final EditedDoc _editedDoc)
        throws EFapsException
    {
        final Edit edit = new Edit() {
            @Override
            protected void add2updateConnection2Object(final Parameter _parameter,
                                                       final Update _update)
                throws EFapsException
            {
                super.add2updateConnection2Object(_parameter, _update);
                CommonDocument_Base.this.add2updateConnection2Object(_parameter, _editedDoc, _update);
            }
        };
        edit.updateConnection2Object(_parameter, _editedDoc.getInstance());
    }

    /**
     * Add2update connection2 object.
     *
     * @param _parameter the _parameter
     * @param _editedDoc the _edited doc
     * @param _update the _update
     * @throws EFapsException on error
     */
    protected void add2updateConnection2Object(final Parameter _parameter,
                                             final EditedDoc _editedDoc,
                                             final Update _update)
        throws EFapsException
    {

    }

    /**
     * Method to evaluate the selected row.
     *
     * @param _parameter paaremter
     * @return number of selected row.
     */
    public int getSelectedRow(final Parameter _parameter)
    {
        return InterfaceUtils.getSelectedRow(_parameter);
    }

    /**
     * Gets the java script for doc.
     *
     * @param _parameter    Parameter as passed by the eFasp API
     * @return JavaScript to be used for UI
     * @throws EFapsException on error
     */
    protected CharSequence getJavaScript4Doc(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        for (final IOnCreateDocument listener : Listener.get().<IOnCreateDocument>invoke(IOnCreateDocument.class)) {
            ret.append(listener.getJavaScript4Doc(this, _parameter));
        }
        return ret;
    }

    /**
     * Class is used as the return value for the internal Create methods.
     */
    public static class CreatedDoc
    {
        /**
         * Instance of the newly created doc.
         */
        private Instance instance;

        /**
         * Positions of the created Document.
         */
        private final List<Instance> positions = new ArrayList<>();

        /**
         * Map can be used to pass values from one method to another.
         */
        private final Map<String, Object> values = new HashMap<>();

        /**
         * Instantiates a new created doc.
         */
        public CreatedDoc()
        {
        }


        /**
         * Instantiates a new created doc.
         *
         * @param _instance Instance of the Document
         */
        public CreatedDoc(final Instance _instance)
        {
            instance = _instance;
        }

        /**
         * Getter method for the instance variable {@link #values}.
         *
         * @return value of instance variable {@link #values}
         */
        public Map<String, Object> getValues()
        {
            return values;
        }

        /**
         * Gets the value.
         *
         * @param _key key
         * @return value
         */
        public Object getValue(final String _key)
        {
            return values.get(_key);
        }

        /**
         * Adds the value.
         *
         * @param _key  key
         * @param _value value
         */
        public void addValue(final String _key,
                             final Object _value)
        {
            values.put(_key, _value);
        }

        /**
         * Getter method for the instance variable {@link #instance}.
         *
         * @return value of instance variable {@link #instance}
         */
        public Instance getInstance()
        {
            return instance;
        }

        /**
         * Setter method for instance variable {@link #instance}.
         *
         * @param _instance value for instance variable {@link #instance}
         */

        public void setInstance(final Instance _instance)
        {
            instance = _instance;
        }

        /**
         * Getter method for the instance variable {@link #positions}.
         *
         * @return value of instance variable {@link #positions}
         */
        public List<Instance> getPositions()
        {
            return positions;
        }

        /**
         * Adds the position.
         *
         * @param _instance Instance to add
         */
        public void addPosition(final Instance _instance)
        {
            positions.add(_instance);
        }
    }

    /**
     * Class is used as the return value for the internal Edit methods.
     */
    public static class EditedDoc
        extends CreatedDoc
    {

        /**
         * Instantiates a new edited doc.
         *
         * @param _instance Instance the document belongs to
         */
        public EditedDoc(final Instance _instance)
        {
            super(_instance);
        }
    }

    /**
     * Warning for not enough Stock.
     */
    public static class Selected4AssignActionInvalidWarning
        extends AbstractWarning
    {
        /**
         * Constructor.
         */
        public Selected4AssignActionInvalidWarning()
        {
            setError(true);
        }
    }

    /**
     * The Class AssignDifferentActionWarning.
     */
    public static class AssignDifferentActionWarning
        extends AbstractWarning
    {
        /**
         * Constructor.
         */
        public AssignDifferentActionWarning()
        {
            setError(true);
        }
    }
}
