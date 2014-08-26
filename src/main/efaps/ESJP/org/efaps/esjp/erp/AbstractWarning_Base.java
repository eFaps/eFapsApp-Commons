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
import java.util.List;
import java.util.Properties;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.esjp.erp.util.ERPSettings;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("93fe4fd1-4d03-471b-bf88-323db2965a27")
@EFapsRevision("$Rev$")
public abstract class AbstractWarning_Base
    implements IWarning
{
    /**
     * LIst of object for the DBProperty.
     */
    private final List<Object> objects = new ArrayList<Object>();

    /**
     * Format string.
     */
    private String format;

    /**
     * Key for the DBProperty.
     */
    private String key;

    /**
     * Is error or not.
     */
    private boolean error = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage()
        throws EFapsException
    {
        return String.format(Context.getThreadContext().getLocale(), getFormat(), getObjects().toArray());
    }

    /**
     * Getter method for the instance variable {@link #format}.
     *
     * @return value of instance variable {@link #format}
     */
    public String getFormat()
    {
        if (this.format == null) {
            this.format = DBProperties.getProperty(getKey());
        }
        return this.format;
    }

    /**
     * Setter method for instance variable {@link #format}.
     *
     * @param _format value for instance variable {@link #format}
     * @return this for chaining
     */
    public IWarning setFormat(final String _format)
    {
        this.format = _format;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #objects}.
     *
     * @return value of instance variable {@link #objects}
     */
    public List<Object> getObjects()
    {
        return this.objects;
    }

    /**
     * Getter method for the instance variable {@link #objects}.
     * @param _objects objects to be added
     * @return this for chaining
     */
    public IWarning addObject(final Object... _objects)
    {
        for (final Object object : _objects) {
            this.objects.add(object);
        }
        return this;
    }

    /**
     * Getter method for the instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        if (this.key == null) {
            this.key = "org.efaps.warning." + this.getClass().getSimpleName();
        }
        return this.key;
    }

    /**
     * Setter method for instance variable {@link #key}.
     *
     * @param _key value for instance variable {@link #key}
     * @return this for chaining
     */
    public IWarning setKey(final String _key)
    {
        this.key = _key;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #error}.
     *
     * @return value of instance variable {@link #error}
     */
    @Override
    public boolean isError()
        throws EFapsException
    {
        boolean ret = this.error;
        final Properties props = ERP.getSysConfig().getAttributeValueAsProperties(ERPSettings.WARNING);
        final String keyTmp = this.getClass().getSimpleName() + ".Level";
        if (props.containsKey(keyTmp)) {
            switch (props.getProperty(keyTmp).toUpperCase()) {
                case "ERROR":
                    ret = true;
                    break;
                case "WARN":
                    ret = false;
                default:
                    break;
            }
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #error}.
     *
     * @param _error value for instance variable {@link #error}
     * @return this for chaining
     */
    public IWarning setError(final boolean _error)
    {
        this.error = _error;
        return this;
    }
}
