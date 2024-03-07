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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.CachedInstanceQuery;
import org.efaps.db.CachedPrintQuery;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.QueryCache;
import org.efaps.esjp.ci.CIERP;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("a848745e-417f-4148-9f24-7429cb445572")
@EFapsApplication("eFapsApp-Commons")
public abstract class CurrencyInst_Base
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyInst.class);

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
     * ISONumber of this Currency.
     */
    private String isoNumber;

    /**
     * Constructor when used as instance object. to access parameters from a
     * currency.
     *
     * @param _instance instance of the currency
     */
    public CurrencyInst_Base(final Instance _instance)
    {
        instance = _instance;
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
     * Getter method for the instance variable {@link #symbol}.
     *
     * @return value of instance variable {@link #symbol}
     * @throws EFapsException on error
     */
    public String getSymbol()
        throws EFapsException
    {
        initialize();
        return symbol;
    }

    /**
     * Method used to read the related data from the database.
     *
     * @throws EFapsException on error
     */
    private void initialize()
        throws EFapsException
    {
        if (!initialized) {
            final PrintQuery print = new CachedPrintQuery(instance).setLifespan(1).setLifespanUnit(TimeUnit.HOURS);
            print.addAttribute(CIERP.Currency.Symbol, CIERP.Currency.Name, CIERP.Currency.Invert, CIERP.Currency.UUID,
                            CIERP.Currency.ISOCode, CIERP.Currency.ISONumber);
            print.execute();
            symbol = print.<String>getAttribute(CIERP.Currency.Symbol);
            name = print.<String>getAttribute(CIERP.Currency.Name);
            invert = print.<Boolean>getAttribute(CIERP.Currency.Invert);
            uuid = UUID.fromString(print.<String>getAttribute(CIERP.Currency.UUID));
            isoCode =  print.<String>getAttribute(CIERP.Currency.ISOCode);
            isoNumber =  print.<String>getAttribute(CIERP.Currency.ISONumber);
            initialized = true;
        }
    }

    /**
     * Setter method for instance variable {@link #symbol}.
     *
     * @param _symbol value for instance variable {@link #symbol}
     */

    public void setSymbol(final String _symbol)
    {
        symbol = _symbol;
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
        return name;
    }

    /**
     * Setter method for instance variable {@link #name}.
     *
     * @param _name value for instance variable {@link #name}
     */

    public void setName(final String _name)
    {
        name = _name;
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
        return invert;
    }

    /**
     * Setter method for instance variable {@link #invert}.
     *
     * @param _invert value for instance variable {@link #invert}
     */

    public void setInvert(final boolean _invert)
    {
        invert = _invert;
    }

    /**
     * Getter method for the instance variable {@link #initialized}.
     *
     * @return value of instance variable {@link #initialized}
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Setter method for instance variable {@link #initialized}.
     *
     * @param _initialized value for instance variable {@link #initialized}
     */
    public void setInitialized(final boolean _initialized)
    {
        initialized = _initialized;
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
        return uuid;
    }

    /**
     * Setter method for instance variable {@link #uuid}.
     *
     * @param _uuid value for instance variable {@link #uuid}
     */
    public void setUUID(final UUID _uuid)
    {
        uuid = _uuid;
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
        return isoCode;
    }

    /**
     * Setter method for instance variable {@link #iSOCode}.
     *
     * @param _iSOCode value for instance variable {@link #iSOCode}
     */
    public void setISOCode(final String _iSOCode)
    {
        isoCode = _iSOCode;
    }

    public String getISONumber()
        throws EFapsException
    {
        initialize();
        return isoNumber;
    }

    public void setISONumber(final String _isoNumber)
    {
        isoNumber = _isoNumber;
    }

    /**
     * Gets the latest valid from.
     *
     * @return the latest valid from
     * @throws EFapsException the e faps exception
     */
    public DateTime getLatestValidFrom()
        throws EFapsException
    {
        DateTime ret = null;
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.CurrencyRateAbstract);
        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, getInstance());
        queryBldr.addOrderByAttributeDesc(CIERP.CurrencyRateAbstract.ValidFrom);
        queryBldr.setLimit(1);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIERP.CurrencyRateAbstract.ValidFrom);
        multi.execute();
        if (multi.next()) {
            ret = multi.getAttribute(CIERP.CurrencyRateAbstract.ValidFrom);
        }
        return ret;
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

    /**
     * @param _object object the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    protected static CurrencyInst get(final Object _object)
        throws EFapsException
    {
        CurrencyInst ret = null;
        if (_object instanceof Long) {
            ret = CurrencyInst_Base.get((Long) _object);
        } else if (_object instanceof Instance) {
            ret = CurrencyInst_Base.get((Instance) _object);
        } else if (_object instanceof UUID) {
            ret = CurrencyInst_Base.get((UUID) _object);
        }
        return ret;
    }

    /**
     * @return Set of available CurrencyInst
     * @throws EFapsException on error
     */
    protected static Collection<CurrencyInst> getAvailable()
        throws EFapsException
    {
        final Set<CurrencyInst> ret = new LinkedHashSet<>();
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.Currency);
        queryBldr.addWhereAttrEqValue(CIERP.Currency.Status, Status.find(CIERP.CurrencyStatus.Active));
        queryBldr.addOrderByAttributeAsc(CIERP.Currency.ISOCode);
        final CachedInstanceQuery query = queryBldr.getCachedQuery(QueryCache.DEFAULTKEY)
                        .setLifespan(1).setLifespanUnit(TimeUnit.HOURS);
        query.executeWithoutAccessCheck();
        while (query.next()) {
            ret.add(new CurrencyInst(query.getCurrentValue()));
        }
        return ret;
    }

    protected static Optional<CurrencyInst> find(final String _codeOrNumber)
        throws EFapsException
    {
        return CurrencyInst.getAvailable().stream().filter(ci -> {
            try {
                return _codeOrNumber.equals(ci.getISOCode()) || _codeOrNumber.equals(ci.getISONumber());
            } catch (final EFapsException e) {
                LOG.error("Catched", e);
            }
            return false;
        }).findFirst();
    }
}
