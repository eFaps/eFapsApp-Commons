/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.OIDType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.ci.CIERP;
import org.efaps.util.EFapsException;


/**
 * The Class to create Revisions for a Document.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("63f7a789-bd28-4e7f-bae2-89c2425df2b3")
@EFapsRevision("$Rev$")
public abstract class Revision_Base
{

    /**
     * Method to execute the actual revision process.
     * @param _parameter    Parameter as passed from the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return revise(final Parameter _parameter)
        throws EFapsException
    {
        final Instance newInst = copyDoc(_parameter);
        updateRevision(_parameter, newInst);
        copyRelations(_parameter, newInst);
        return new Return();
    }

    /**
     * Copy the Relations.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     */
    protected void copyRelations(final Parameter _parameter,
                                 final Instance _newInst)
        throws EFapsException
    {
        final Map<?,?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (props.containsKey("ReviseRelations") && props.containsKey("ReviseRelationsAttribute")) {
            final String [] rels = ((String) props.get("ReviseRelations")).split(";");
            final String [] attrs = ((String) props.get("ReviseRelationsAttribute")).split(";");

            for (int i = 0; i < rels.length; i++) {
                final Type reltype = Type.get(rels[i]);
                final QueryBuilder queryBldr = new QueryBuilder(reltype);
                queryBldr.addWhereAttrEqValue(reltype.getAttribute(attrs[i]), _parameter.getInstance().getId());
                final InstanceQuery query = queryBldr.getQuery();
                final List<Instance> instances = query.execute();
                for (final Instance instance : instances) {
                    final Insert insert = new Insert(instance.getType());
                    final Attribute attr = instance.getType().getAttribute(attrs[i]);
                    insert.add(attr, _newInst.getId());
                    final Set<String> added = new HashSet<String>();
                    added.add(attr.getSqlColNames().toString());
                    addAttributes(_parameter, instance, insert, added);
                    insert.execute();
                }
            }
        }
    }

    /**
     * Update the Revision number for the Document.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     */
    protected void updateRevision(final Parameter _parameter,
                                  final Instance _newDoc)
        throws EFapsException
    {
        final Update update = new Update(_newDoc);
        update.add(getRevisionAttribute(_parameter, _newDoc), getNextRevision(_parameter, _newDoc));
        update.execute();
    }

    /**
     * Get the Name of the Attribute that contains the Revsion.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     */
    protected String getRevisionAttribute(final Parameter _parameter,
                                          final Instance _newDoc)
        throws EFapsException
    {
        final Map<?,?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String attrName;
        if (props.containsKey("RevisionAttribute")) {
            attrName = (String) props.get("RevisionAttribute");
        } else {
            attrName = CIERP.DocumentAbstract.Revision.name;
        }
        return attrName;
    }

    /**
     * Get the next Revision Number.
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     * @return next Revision Number
     */
    protected Object getNextRevision(final Parameter _parameter,
                                   final Instance _newDoc)
        throws EFapsException
    {
        int ret;
        final Instance origInst = _parameter.getInstance();
        final PrintQuery print = new PrintQuery(origInst);
        print.addAttribute(getRevisionAttribute(_parameter, _newDoc));
        if (print.execute()) {
            final String value = print.<String>getAttribute(getRevisionAttribute(_parameter, _newDoc));
            if (value != null && !value.isEmpty()) {
                ret = Integer.parseInt(value);
            } else {
                ret = 0;
            }
            ret++;
        } else {
            ret = 0;
        }
        return ret;
    }

    /**
     * Copy the main Document and return the Instance of the newly created
     * Document.
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Instance of the newly created Document
     * @throws EFapsException on error
     */
    protected Instance copyDoc(final Parameter _parameter)
        throws EFapsException
    {
        final Instance origInst = _parameter.getInstance();
        final Insert insert = new Insert(origInst.getType());
        final Set<String> added = new HashSet<String>();
        addAttributes(_parameter, origInst, insert, added);
        insert.execute();
        return insert.getInstance();
    }

    /**
     * Add Attributes to an Update.
     *
     * @param _parameter     Parameter as passed from the eFaps API
     * @param _origInst     Instance to be copied
     * @param _update       update the attributes must be added to
     * @param _added        already added attributes
     * @throws EFapsException on error
     */
    protected void addAttributes(final Parameter _parameter,
                                 final Instance _origInst,
                                 final Update _update,
                                 final Set<String> _added)
        throws EFapsException
    {
        final PrintQuery print = new PrintQuery(_origInst);
        for (final Attribute attr : _origInst.getType().getAttributes().values()) {
            print.addAttribute(attr);
        }
        print.execute();

        for (final Attribute attr : _origInst.getType().getAttributes().values()) {
            final Attribute typeAttr = attr.getParent().getTypeAttribute();
            final boolean noAdd = attr.getAttributeType().isAlwaysUpdate() || attr.getAttributeType().isCreateUpdate()
                || typeAttr.getName().equals(attr.getName())
                || attr.getAttributeType().getDbAttrType() instanceof OIDType
                || _added.contains(attr.getSqlColNames().toString())
                || attr.getParent().getMainTable().getSqlColId().equals(attr.getSqlColNames().get(0));
            if (!noAdd) {
                final Object object = print.getAttribute(attr);
                _update.add(attr, object);
                _added.add(attr.getSqlColNames().toString());
            }
        }
    }
}
