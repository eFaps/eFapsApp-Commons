/*
 * Copyright 2003 - 2011 The eFaps Team
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.common.NumberGenerator;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.esjp.erp.util.ERPSettings;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("e47df65d-4c5e-423f-b2cc-815c3007b19f")
@EFapsRevision("$Rev$")
public abstract class CommonDocument_Base
    extends AbstractCommon
{
    /**
     * @param _parameter    Parameter as passed by the eFasp API
     * @return Sales Person Field Value
     * @throws EFapsException on error
     */
    public Return getSalesPersonFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final org.efaps.esjp.common.uiform.Field field = new org.efaps.esjp.common.uiform.Field() {

            @Override
            protected DropDownPosition getDropDownPosition(final Parameter _parameter,
                                                           final Object _value,
                                                           final Object _option)
                throws EFapsException
            {
                final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
                final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
                DropDownPosition pos;
                if (TargetMode.EDIT.equals(fieldValue.getTargetMode())) {
                    pos = new DropDownPosition(_value, _option) {
                        @Override
                        public boolean isSelected()
                        {
                            boolean ret = false;
                            final Long persId = (Long) fieldValue.getValue();
                            ret = getValue().equals(persId);
                            return ret;
                        }
                    };
                } else {
                    if ("true".equalsIgnoreCase((String) props.get("SelectCurrent"))) {
                        pos = new DropDownPosition(_value, _option) {

                            @Override
                            public boolean isSelected()
                            {
                                boolean ret = false;
                                long persId = 0;
                                try {
                                    persId = Context.getThreadContext().getPerson().getId();
                                } catch (final EFapsException e) {
                                    // nothing must be done at all
                                    e.printStackTrace();
                                }
                                ret = new Long(persId).equals(getValue());
                                return ret;
                            }
                        };
                    } else {
                        pos = super.getDropDownPosition(_parameter, _value, _option);
                    }
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
                    final List<Long> roleIds = new ArrayList<Long>();
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

                    final List<Long> groupIds = new ArrayList<Long>();
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
        return field.dropDownFieldValue(_parameter);
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
            .append("  tableName = \"").append(_tableName).append("\";\n")
            .append("  for (i = 100;i < 10000; i = i + 100) {\n")
            .append("    if( lang.exists(\"eFapsTable\" + i) ){\n")
            .append("      var tO = window[\"eFapsTable\" + i];\n")
            .append("      if (tO.tableName == tableName) {\n")
            .append("        tableBody = dom.byId(tO.bodyID);\n")
            .append("        query(\".eFapsTableRemoveRowCell\", tableBody).parent().forEach(domConstruct.destroy);\n");

        if (_makeUneditable) {
            ret.append("        query(\"div\", tableBody).style(\"display\", \"none\");");
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
     *
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
                                                     final Collection<Map<String, String>> _values,
                                                     final StringBuilder _onComplete)
    {
        return getTableAddNewRowsScript(_parameter, _tableName, _values, _onComplete, false, false, null);
    }

    /**
     * A Script adds new Rows to the given table and fills it with values.
     * @param _parameter Parameter as passed by the eFaps API
     * @param _tableName name of the table to be removed
     * @param _values values to be used
     * @param _onComplete script to be added on complete
     * @param _onDomReady add onDomReady script part
     * @param _wrapInTags wrap in script tags
     * @param _nonEscapeFields set of fields for which the escape must not apply
     * @return StringBuilder containing the javascript
     */
    protected StringBuilder getTableAddNewRowsScript(final Parameter _parameter,
                                                     final String _tableName,
                                                     final Collection<Map<String, String>> _values,
                                                     final StringBuilder _onComplete,
                                                     final boolean _onDomReady,
                                                     final boolean _wrapInTags,
                                                     final Set<String> _nonEscapeFields)
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
        ret.append("\n}, null);\n");
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
                                                    final Collection<Map<String, String>> _values,
                                                    final Set<String> _nonEscapeFields)
    {
        final StringBuilder ret = new StringBuilder();
        int i = 0;
        for (final Map<String, String> values : _values) {
            for (final Entry<String, String> entry : values.entrySet()) {
                ret.append(getSetFieldValue(i, entry.getKey(), entry.getValue(), _nonEscapeFields == null ? true
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
        return getSetFieldValue(_idx, _fieldName, _value, true);
    }

    /**
     * Get a "eFapsSetFieldValue" Javascript line.
     * @param _idx          index of the field
     * @param _fieldName    name of the field
     * @param _value        value
     * @param _escape       must the value be escaped
     * @return StringBuilder
     */
    protected StringBuilder getSetFieldValue(final int _idx,
                                             final String _fieldName,
                                             final String _value,
                                             final boolean _escape)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("eFapsSetFieldValue(").append(_idx).append(",'").append(_fieldName).append("',");
        if (_escape) {
            ret.append("'").append(StringEscapeUtils.escapeEcmaScript(_value)).append("'");
        } else {
            ret.append(_value);
        }
        ret.append(");");
        return ret;
    }

    /**
     * JavaScript Snipplet that removes from all SELECT with
     * <code>fieldname</code> all other options except the one
     * identified by the given <code>_idvalue</code>
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _idvalue      value that will be set for the dropdown
     * @param _field        fieldname
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
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _idvalue      value that will be set for the dropdown
     * @param _field        fieldname
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
            .append("var nl = query(\" select[name='").append(_field).append("']\")");

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
        final StringBuilder js = new StringBuilder()
            .append("require([\"dojo/query\", \"dojo/NodeList-dom\"], function(query) {\n");

        for (final String field : _field) {
            js.append("var nl = query(\" input[name='").append(field).append("'], textarea[name='")
                .append(field).append("']\");");
            if (_idx == null) {
                js.append("nl.forEach(\"item.readOnly = true;\");\n");
            } else {
                js.append("if (nl[").append(_idx).append("]!=undefined) {")
                    .append("nl[").append(_idx).append("].readOnly = true;")
                    .append("}\n");
            }
        }
        js.append("query(\" input[readonly=''], textarea[readonly='']\").addClass(\"eFapsReadOnly\")")
            .append("});");
        return js;
    }


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

    protected String getUoMFieldStr(final long _dimId)
        throws CacheReloadException
    {
        final Dimension dim = Dimension.get(_dimId);
        return getUoMFieldStr(dim.getBaseUoM().getId(), _dimId);
    }

    /**
     * @param _uoMId id of the UoM
     * @return Field String
     */
    protected String getUoMFieldStrByUoM(final long _uoMId)
        throws CacheReloadException
    {
        return getUoMFieldStr(_uoMId, Dimension.getUoM(_uoMId).getDimId());
    }

    /**
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
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        Status status = null;
        if (props.containsKey("StatusGroup")) {
            status = Status.find((String) props.get("StatusGroup"), (String) props.get("Status"));
        }

        if (status != null) {
            _insert.add(getType4DocCreate(_parameter).getStatusAttribute(), status.getId());
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
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final boolean useNumGen = "true".equalsIgnoreCase((String) properties.get("UseNumberGenerator4Name"));
        String ret;
        if (useNumGen) {
            final Type type = getType4DocCreate(_parameter);
            final Properties props = ERP.getSysConfig()
                            .getAttributeValueAsProperties(ERPSettings.NUMBERGENERATOR, true);
            final String uuid = props.getProperty(type.getName());
            final NumberGenerator numGen = NumberGenerator.get(UUID.fromString(uuid));
            ret = numGen.getNextVal();
        } else {
            ret = _parameter.getParameterValue(getFieldName4Attribute(_parameter, CIERP.DocumentAbstract.Name.name));
        }
        return ret;
    }


    public void executeProcess(final Parameter _parameter,
                               final CreatedDoc _createdDoc) throws EFapsException
    {
        final Create create = new Create() {
            @Override
            protected void add2ProcessMap(final Parameter _parameter,
                                          final Instance _instance,
                                          final Map<String, Object> _params)
                throws EFapsException
            {
                CommonDocument_Base.this.add2ProcessMap(_parameter, _createdDoc, _params);
            }
        };
        create.executeProcess(_parameter, _createdDoc.getInstance());
    }


    /**
     * Add additional values to the map passed to the process prior to
     * execution.
     *
     * @param _parameter Parameter as passed by the eFasp API
     * @param _instance Insert the values can be added to
     * @param _params Map passed to the Process
     * @throws EFapsException on error
     */
    protected void add2ProcessMap(final Parameter _parameter,
                                  final CreatedDoc _createdDoc,
                                  final Map<String, Object> _params)
        throws EFapsException
    {

    }


    /**
     * Class is used as the return value for the internal Create methods.
     */
    public class CreatedDoc
    {
        /**
         * Instance of the newly created doc.
         */
        private Instance instance;

        /**
         * Positions of the created Document.
         */
        private final List<Instance> positions = new ArrayList<Instance>();

        /**
         * Map can be used to pass values from one method to another.
         */
        private final Map<String, Object> values = new HashMap<String, Object>();

        /**
         *
         */
        public CreatedDoc()
        {
        }


        /**
         * @param _instance Instance of the Document
         */
        public CreatedDoc(final Instance _instance)
        {
            this.instance = _instance;
        }

        /**
         * Getter method for the instance variable {@link #values}.
         *
         * @return value of instance variable {@link #values}
         */
        public Map<String, Object> getValues()
        {
            return this.values;
        }

        /**
         * @param _key key
         * @return value
         */
        public Object getValue(final String _key)
        {
            return this.values.get(_key);
        }

        /**
         * @param _key  key
         * @param _value value
         */
        public void addValue(final String _key,
                             final Object _value)
        {
            this.values.put(_key, _value);
        }

        /**
         * Getter method for the instance variable {@link #instance}.
         *
         * @return value of instance variable {@link #instance}
         */
        public Instance getInstance()
        {
            return this.instance;
        }

        /**
         * Setter method for instance variable {@link #instance}.
         *
         * @param _instance value for instance variable {@link #instance}
         */

        public void setInstance(final Instance _instance)
        {
            this.instance = _instance;
        }

        /**
         * Getter method for the instance variable {@link #positions}.
         *
         * @return value of instance variable {@link #positions}
         */
        public List<Instance> getPositions()
        {
            return this.positions;
        }
        /**
         * @param _instance Instance to add
         */
        public void addPosition(final Instance _instance)
        {
            this.positions.add(_instance);
        }
    }
}
