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
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Context;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("e47df65d-4c5e-423f-b2cc-815c3007b19f")
@EFapsRevision("$Rev$")
public abstract class CommonDocument_Base
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
}
