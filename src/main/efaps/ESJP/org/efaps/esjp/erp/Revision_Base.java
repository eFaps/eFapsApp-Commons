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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.OIDType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class to create Revisions for a Document.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("63f7a789-bd28-4e7f-bae2-89c2425df2b3")
@EFapsRevision("$Rev$")
public abstract class Revision_Base
    extends AbstractCommon
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Revision.class);

    /**
     * Method to execute the actual revision process.
     * @param _parameter    Parameter as passed from the eFaps API
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return revise(final Parameter _parameter)
        throws EFapsException
    {
        if (reviseable(_parameter.getInstance())) {
            final Instance newInst = copyDoc(_parameter);
            if (newInst != null && newInst.isValid()) {
                updateRevision(_parameter, newInst);
                copyRelations(_parameter, newInst);
                connectRevision(_parameter, newInst);
                setStati(_parameter, newInst);
            }
        }
        return new Return();
    }

    /**
     * Validate if the given Document can be revised.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @return Return with true if can be revised
     * @throws EFapsException on error
     */
    public Return validate(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        if (!reviseable(_parameter.getInstance())) {
            final StringBuilder html = new StringBuilder()
                .append(DBProperties.getProperty("org.efaps.esjp.erp.Revision.validate.revisable"));
            ret.put(ReturnValues.SNIPLETT, html.toString());
        } else {
            ret.put(ReturnValues.TRUE, true);
        }
        return ret;
    }

    /**
     * Check if the given instance can be revised by checking
     * if a Revise Relation already exists.
     *
     * @param _instance instance to be checked
     * @return true if already revised else false
     * @throws EFapsException on error
     */
    protected boolean reviseable(final Instance _instance)
        throws EFapsException
    {
        boolean ret = false;
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.Document2Revision);
        queryBldr.addWhereAttrEqValue(CIERP.Document2Revision.FromLink, _instance.getId());
        final InstanceQuery query = queryBldr.getQuery();
        ret = query.execute().isEmpty();
        return ret;
    }

    /**
     * Set the stati for the Instances.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     */
    protected void setStati(final Parameter _parameter,
                            final Instance _newDoc)
        throws EFapsException
    {
        final Map<?,?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (props.containsKey("Status")) {
            final String statusStr = (String) props.get("Status");
            updateStatus(_parameter.getInstance(), statusStr);
        }
        if (props.containsKey("RevisionStatus")) {
            final String statusStr = (String) props.get("RevisionStatus");
            updateStatus(_newDoc, statusStr);
        }
    }

    /**
     * Set the status for an instance.
     *
     * @param _instance     Instance to be updated
     * @param _statusStr    statsu to be set
     * @throws EFapsException   on error
     */
    protected void updateStatus(final Instance _instance,
                                final String _statusStr)
        throws EFapsException
    {
        final Type statusType = _instance.getType().getStatusAttribute().getLink();
        final Status status = Status.find(statusType.getUUID(), _statusStr);
        if (status != null) {
            final Update update = new Update(_instance);
            update.add(_instance.getType().getStatusAttribute(), status.getId());
            update.execute();
        }
    }

    /**
     * Connect the new Revision to its parent.
     * Setting property "RevisionConnect" = false deactivates this mechanism.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     */
    protected void connectRevision(final Parameter _parameter,
                                   final Instance _newDoc)
        throws EFapsException
    {
        final Map<?,?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (!"false".equalsIgnoreCase((String) props.get("RevisionConnect"))) {
            final Instance origInst = _parameter.getInstance();
            final Insert insert = new Insert(CIERP.Document2Revision);
            insert.add(CIERP.Document2Revision.FromLink, origInst.getId());
            insert.add(CIERP.Document2Revision.ToLink, _newDoc.getId());
            insert.execute();
        }
    }

    /**
     * Copy the Relations.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newInst      the newly created Document
     * @return Map of old instance to new instance
     * @throws EFapsException on error
     */
    protected Map<Instance, Instance> copyRelations(final Parameter _parameter,
                                                    final Instance _newInst)
        throws EFapsException
    {
        final Map<Instance, Instance> ret = new HashMap<Instance, Instance>();
        _parameter.get(ParameterValues.PROPERTIES);
        //TODO remove old api
        if (containsProperty(_parameter, "ReviseRelations")
                        && containsProperty(_parameter, "ReviseRelationsAttribute")) {
            LOG.warn("Deprecated api for ReviseRelationsAttribute");
            final String [] rels = getProperty(_parameter, "ReviseRelations").split(";");
            final String [] attrs = getProperty(_parameter, "ReviseRelationsAttribute").split(";");

            for (int i = 0; i < rels.length; i++) {
                ret.putAll(copyRelation(_parameter, _newInst, rels[i], attrs[i]));
            }
        }
        if (containsProperty(_parameter, "ReviseRelation")
                        && containsProperty(_parameter, "ReviseRelationAttribute")) {
            final Map<Integer, String> rel = analyseProperty(_parameter, "ReviseRelation");
            final Map<Integer, String> relattr = analyseProperty(_parameter, "ReviseRelationAttribute");
            for (final Entry<Integer, String> entry : rel.entrySet()) {
                ret.putAll(copyRelation(_parameter, _newInst, entry.getValue(), relattr.get(entry.getKey())));
            }
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _newInst      Instance of  the new doc
     * @param _typeName     name of the relation type
     * @param _linkAttrName name of the attribute that connects the relation
     *                      to the type
     * @return Map of oldInstance to new Instance
     * @throws EFapsException on error
     */
    protected Map<Instance, Instance> copyRelation(final Parameter _parameter,
                                                   final Instance _newInst,
                                                   final String _typeName,
                                                   final String _linkAttrName)
        throws EFapsException
    {
        final Type reltype = Type.get(_typeName);
        return copyRelation(_parameter, _newInst, reltype, reltype.getAttribute(_linkAttrName), null);
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _newInst      Instance of  the new doc
     * @param _typeName     name of the relation type
     * @param _linkAttrName name of the attribute that connects the relation
     *                      to the type
     * @return Map of oldInstance to new Instance
     * @throws EFapsException on error
     */
    protected Map<Instance, Instance> copyRelation(final Parameter _parameter,
                                                   final Instance _newInst,
                                                   final Type _reltype,
                                                   final Attribute _relAttr,
                                                   final Type _targetType)
        throws EFapsException
    {
        final Map<Instance, Instance> ret = new HashMap<Instance, Instance>();
        final QueryBuilder queryBldr = new QueryBuilder(_reltype);
        queryBldr.addWhereAttrEqValue(_relAttr, _parameter.getInstance().getId());
        final InstanceQuery query = queryBldr.getQuery();
        final List<Instance> instances = query.execute();
        for (final Instance instance : instances) {
            final Insert insert = new Insert(_targetType == null ? instance.getType() : _targetType);
            final Attribute attr = instance.getType().getAttribute(_relAttr.getName());
            insert.add(attr, _newInst.getId());
            final Set<String> added = new HashSet<String>();
            added.add(attr.getSqlColNames().toString());
            addAttributes(_parameter, instance, insert, added);
            insert.execute();
            ret.put(instance, insert.getInstance());
        }
        return ret;
    }

    /**
     * Update the Revision number for the Document.
     * Setting property "RevisionUpdate" = false deactivates this mechanism.
     *
     * @param _parameter    Parameter as passed from the eFaps API
     * @param _newDoc       the newly created Document
     * @throws EFapsException on error
     */
    protected void updateRevision(final Parameter _parameter,
                                  final Instance _newDoc)
        throws EFapsException
    {
        final Map<?,?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (!"false".equalsIgnoreCase((String) props.get("RevisionUpdate"))) {
            final Update update = new Update(_newDoc);
            update.add(getRevisionAttribute(_parameter, _newDoc), getNextRevision(_parameter, _newDoc));
            update.execute();
        }
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
        return getNextRevision(_parameter, _parameter.getInstance(), _newDoc);
    }

    public Object getNextRevision(final Parameter _parameter,
                                  final Instance _origDoc,
                                  final Instance _newDoc)
        throws EFapsException
    {
        int ret;
        final PrintQuery print = new PrintQuery(_origDoc);
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
    public void addAttributes(final Parameter _parameter,
                              final Instance _origInst,
                              final Update _update,
                              final Set<String> _added)
        throws EFapsException
    {
        final PrintQuery print = new PrintQuery(_origInst);
        for (final Attribute attr : _origInst.getType().getAttributes().values()) {
            print.addAttribute(attr.getName());
        }
        print.execute();

        for (final Attribute attr : _update.getInstance().getType().getAttributes().values()) {
            final boolean noAdd = attr.getAttributeType().isAlwaysUpdate()
                    || attr.getAttributeType().isCreateUpdate()
                    || attr.getParent().getTypeAttribute() != null
                            && attr.getParent().getTypeAttribute().getName().equals(attr.getName())
                    || attr.getAttributeType().getDbAttrType() instanceof OIDType
                    || _added.contains(attr.getSqlColNames().toString())
                    || attr.getParent().getMainTable().getSqlColId().equals(attr.getSqlColNames().get(0));
            if (!noAdd) {
                final Object object = print.getAttribute(attr);
                _update.add(attr.getName(), object);
                _added.add(attr.getSqlColNames().toString());
            }
        }
    }
}
