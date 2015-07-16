/*
 * Copyright 2003 - 2015 The eFaps Team
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
 */

package org.efaps.esjp.erp;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.esjp.admin.common.IReloadCacheListener;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;

/**
 * The Class NumberFormatter_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("6fb387cd-a4dc-4638-a659-0d1ccf8ca5f9")
@EFapsApplication("eFapsApp-Commons")
public abstract class NumberFormatter_Base
    implements IReloadCacheListener
{
    /**
     * The singelton instance.
     */
    private static NumberFormatter_Base FORMATTER;

    /**
     * Key for basic formatter.
     */
    private static String FRMTKEY = NumberFormatter.class.getName() + ".Formatter";

    /**
     * Key for basic formatter.
     */
    private static String TWOFRMTKEY = NumberFormatter.class.getName() + ".TwoDigitsFormatter";

    /**
     * Key for basic formatter.
     */
    private static String ZEROFRMTKEY = NumberFormatter.class.getName() + ".ZeroDigitsFormatter";

    /**
     * Formatter mapping.
     */
    private final Map<String, DecimalFormat> key2formatter = new HashMap<String, DecimalFormat>();

    /**
     * Method to get a formater.
     *
     * @return a formater
     * @throws EFapsException on error
     */
    public DecimalFormat getTwoDigitsFormatter()
        throws EFapsException
    {
        if (!this.key2formatter.containsKey(NumberFormatter_Base.TWOFRMTKEY)) {
            this.key2formatter.put(NumberFormatter_Base.TWOFRMTKEY, getFormatter(2, 2));
        }
        return this.key2formatter.get(NumberFormatter_Base.TWOFRMTKEY);
    }

    /**
     * Method to get a formater.
     *
     * @return a formater
     * @throws EFapsException on error
     */
    public DecimalFormat getZeroDigitsFormatter()
        throws EFapsException
    {
        if (!this.key2formatter.containsKey(NumberFormatter_Base.ZEROFRMTKEY)) {
            this.key2formatter.put(NumberFormatter_Base.ZEROFRMTKEY, getFormatter(0, 0));
        }
        return this.key2formatter.get(NumberFormatter_Base.ZEROFRMTKEY);
    }

    /**
     * @return a format used to format BigDecimal for the user interface
     * @param _maxFrac maximum Faction, null to deactivate
     * @param _minFrac minimum Faction, null to activate
     * @throws EFapsException on error
     */
    public DecimalFormat getFormatter(final Integer _minFrac,
                                      final Integer _maxFrac)
                                          throws EFapsException
    {
        final DecimalFormat formater = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext().getLocale());
        if (_maxFrac != null) {
            formater.setMaximumFractionDigits(_maxFrac);
        }
        if (_minFrac != null) {
            formater.setMinimumFractionDigits(_minFrac);
        }
        formater.setMinimumIntegerDigits(1);
        formater.setRoundingMode(RoundingMode.HALF_UP);
        formater.setParseBigDecimal(true);
        return formater;
    }

    /**
     * Method to get a <code>DecimalFormat</code> instance with the
     * <code>Locale</code> from the <code>Context</code>.
     *
     * @return DecimalFormat
     * @throws EFapsException on erro
     */
    public DecimalFormat getFormatter()
        throws EFapsException
    {
        if (!this.key2formatter.containsKey(NumberFormatter_Base.FRMTKEY)) {
            this.key2formatter.put(NumberFormatter_Base.FRMTKEY, getFormatter(null, null));
        }
        return this.key2formatter.get(NumberFormatter_Base.FRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4UnitPrice(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4UnitPrice", NumberFormatter_Base.TWOFRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Quantity(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4Quantity", NumberFormatter_Base.ZEROFRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Total(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4Total", NumberFormatter_Base.TWOFRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Discount(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4Discount", NumberFormatter_Base.TWOFRMTKEY);
    }

    /**
     * Gets the frmt4 key.
     *
     * @param _type TypeName
     * @param _key the _key
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Key(final String _type,
                                     final String _key)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + "." + _key, NumberFormatter_Base.TWOFRMTKEY);
    }

    /**
     * @param _key key to the Formatter
     * @param _default default key for a formatter
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    protected DecimalFormat getFrmtFromSysConf(final String _key,
                                               final String _default)
        throws EFapsException
    {
        final String storeKey;

        final Company company = Context.getThreadContext().getCompany();
        if (company == null) {
            storeKey = _key;
        } else {
            storeKey = company.getId() + _key;
        }

        if (!this.key2formatter.containsKey(storeKey)) {
            final Properties properties = ERP.NUMBERFRMT.get();
            DecimalFormat frmt;
            if (properties.containsKey(_key)) {
                frmt = getFormatter(null, null);
                frmt.applyPattern(properties.getProperty(_key));
            } else {
                // init
                getTwoDigitsFormatter();
                getZeroDigitsFormatter();
                getFormatter();
                frmt = this.key2formatter.get(_default);
            }
            this.key2formatter.put(storeKey, frmt);
        }
        return this.key2formatter.get(storeKey);
    }

    /**
     * @return static access
     */
    protected static NumberFormatter get()
    {
        if (NumberFormatter_Base.FORMATTER == null) {
            NumberFormatter_Base.FORMATTER = new NumberFormatter();
        }
        return (NumberFormatter) NumberFormatter_Base.FORMATTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReloadSystemConfig(final Parameter _parameter)
    {
        NumberFormatter_Base.FORMATTER.key2formatter.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReloadCache(final Parameter _parameter)
    {
        NumberFormatter_Base.FORMATTER.key2formatter.clear();
    }

    @Override
    public int getWeight()
    {
        return 0;
    }
}
