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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.CachedInstanceQuery;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.QueryCache;
import org.efaps.esjp.ci.CIERP;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: CurrencyInst_Base.java 10707 2013-10-30 01:00:13Z
 *          jan@moxter.net $
 */
@EFapsUUID("a848745e-417f-4148-9f24-7429cb445572")
@EFapsRevision("$Rev$")
public class CurrencyInst_Base
{

    /**
     * Instance for this Currency.
     */
    private final Instance instance;

    /**
     * The symbol for this currency.
     */
    private String symbol;

    /**
     * The name of this currency.
     */
    private String name;

    /**
     * Are the rates of this Currency inverted.
     */
    private boolean invert;

    /**
     * Was the instance initialized. (Data read from the database)
     */
    private boolean initialized;

    /**
     * UUID of this Currency.
     */
    private UUID uuid;

    /**
     * ISOCode of this Currency.
     */
    private String isoCode;

    /**
     * Constructor when used as instance object. to access parameters from a
     * currency.
     *
     * @param _instance instance of the currency
     */
    public CurrencyInst_Base(final Instance _instance)
    {
        this.instance = _instance;
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
     * Getter method for the instance variable {@link #symbol}.
     *
     * @return value of instance variable {@link #symbol}
     * @throws EFapsException on error
     */
    public String getSymbol()
        throws EFapsException
    {
        initialize();
        return this.symbol;
    }

    /**
     * Method used to read the related data from the database.
     *
     * @throws EFapsException on error
     */
    private void initialize()
        throws EFapsException
    {
        if (!this.initialized) {
            final PrintQuery print = new CachedPrintQuery(this.instance).setLifespan(1).setLifespanUnit(TimeUnit.HOURS);
            print.addAttribute(CIERP.Currency.Symbol, CIERP.Currency.Name, CIERP.Currency.Invert, CIERP.Currency.UUID,
                            CIERP.Currency.ISOCode);
            print.execute();
            this.symbol = print.<String>getAttribute(CIERP.Currency.Symbol);
            this.name = print.<String>getAttribute(CIERP.Currency.Name);
            this.invert = print.<Boolean>getAttribute(CIERP.Currency.Invert);
            this.uuid = UUID.fromString(print.<String>getAttribute(CIERP.Currency.UUID));
            this.isoCode =  print.<String>getAttribute(CIERP.Currency.ISOCode);
            this.initialized = true;
        }
    }

    /**
     * Setter method for instance variable {@link #symbol}.
     *
     * @param _symbol value for instance variable {@link #symbol}
     */

    public void setSymbol(final String _symbol)
    {
        this.symbol = _symbol;
    }

    /**
     * Getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     * @throws EFapsException on error
     */
    public String getName()
        throws EFapsException
    {
        initialize();
        return this.name;
    }

    /**
     * Setter method for instance variable {@link #name}.
     *
     * @param _name value for instance variable {@link #name}
     */

    public void setName(final String _name)
    {
        this.name = _name;
    }

    /**
     * Getter method for the instance variable {@link #invert}.
     *
     * @return value of instance variable {@link #invert}
     * @throws EFapsException on error
     */
    public boolean isInvert()
        throws EFapsException
    {
        initialize();
        return this.invert;
    }

    /**
     * Setter method for instance variable {@link #invert}.
     *
     * @param _invert value for instance variable {@link #invert}
     */

    public void setInvert(final boolean _invert)
    {
        this.invert = _invert;
    }

    /**
     * Getter method for the instance variable {@link #initialized}.
     *
     * @return value of instance variable {@link #initialized}
     */
    public boolean isInitialized()
    {
        return this.initialized;
    }

    /**
     * Setter method for instance variable {@link #initialized}.
     *
     * @param _initialized value for instance variable {@link #initialized}
     */
    public void setInitialized(final boolean _initialized)
    {
        this.initialized = _initialized;
    }

    /**
     * Getter method for the instance variable {@link #uuid}.
     *
     * @return value of instance variable {@link #uuid}
     * @throws EFapsException on error
     */
    public UUID getUUID()
        throws EFapsException
    {
        initialize();
        return this.uuid;
    }

    /**
     * Setter method for instance variable {@link #uuid}.
     *
     * @param _uuid value for instance variable {@link #uuid}
     */
    public void setUUID(final UUID _uuid)
    {
        this.uuid = _uuid;
    }

    /**
     * Getter method for the instance variable {@link #iSOCode}.
     *
     * @return value of instance variable {@link #iSOCode}
     *  @throws EFapsException on error
     */
    public String getISOCode()
        throws EFapsException
    {
        initialize();
        return this.isoCode;
    }

    /**
     * Setter method for instance variable {@link #iSOCode}.
     *
     * @param _iSOCode value for instance variable {@link #iSOCode}
     */
    public void setISOCode(final String _iSOCode)
    {
        this.isoCode = _iSOCode;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @param _instance instance the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    protected static CurrencyInst get(final Instance _instance)
        throws EFapsException
    {
        return new CurrencyInst(_instance);
    }

    /**
     * @param _currencyId id the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    protected static CurrencyInst get(final Long _currencyId)
        throws EFapsException
    {
        return new CurrencyInst(Instance.get(CIERP.Currency.getType(), _currencyId));
    }

    /**
     * @param _currencyUUID uuid the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    protected static CurrencyInst get(final UUID _currencyUUID)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.Currency);
        queryBldr.addWhereAttrEqValue(CIERP.Currency.UUID, _currencyUUID.toString());
        final CachedInstanceQuery query = queryBldr.getCachedQuery(QueryCache.DEFAULTKEY)
                        .setLifespan(1).setLifespanUnit(TimeUnit.HOURS);
        query.executeWithoutAccessCheck();
        CurrencyInst ret = null;
        if (query.next()) {
            ret = new CurrencyInst(query.getCurrentValue());
        }
        return ret;
    }

}
