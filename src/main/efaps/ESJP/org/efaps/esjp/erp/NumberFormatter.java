/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.esjp.erp.util.ERPSettings;
import org.efaps.util.EFapsException;

/**
 * Class is used as POJO and therefore is not thought to be used with standard
 * "_Base" approach.
 *
 * @author The eFaps Team
 * @version $Id: NumberFormatter.java 10761 2013-11-05 02:45:51Z jan@moxter.net
 *          $
 */
@EFapsUUID("999443c8-8f9f-47be-aad1-974a1b09421b")
@EFapsRevision("$Rev$")
public class NumberFormatter
{

    private static NumberFormatter FORMATTER;

    /**
     *  Key for basic formatter.
     */
    private static String FRMTKEY = NumberFormatter.class.getName() + ".Formatter";

    /**
     *  Key for basic formatter.
     */
    private static String TWOFRMTKEY = NumberFormatter.class.getName() + ".TwoDigitsFormatter";

    /**
     *  Key for basic formatter.
     */
    private static String ZEROFRMTKEY = NumberFormatter.class.getName() + ".ZeroDigitsFormatter";


    /**
     * Formatter mapping
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
        if (!this.key2formatter.containsKey(NumberFormatter.TWOFRMTKEY)) {
            this.key2formatter.put(NumberFormatter.TWOFRMTKEY, getFormatter(2, 2));
        }
        return this.key2formatter.get(NumberFormatter.TWOFRMTKEY);
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
        if (!this.key2formatter.containsKey(NumberFormatter.ZEROFRMTKEY)) {
            this.key2formatter.put(NumberFormatter.ZEROFRMTKEY, getFormatter(0, 0));
        }
        return this.key2formatter.get(NumberFormatter.ZEROFRMTKEY);
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
        if (!this.key2formatter.containsKey(NumberFormatter.FRMTKEY)) {
            this.key2formatter.put(NumberFormatter.FRMTKEY, getFormatter(null, null));
        }
        return this.key2formatter.get(NumberFormatter.FRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4UnitPrice(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4UnitPrice", NumberFormatter.TWOFRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Quantity(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4Quantity", NumberFormatter.ZEROFRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Total(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4Total", NumberFormatter.TWOFRMTKEY);
    }

    /**
     * @param _type TypeName
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4Discount(final String _type)
        throws EFapsException
    {
        return getFrmtFromSysConf(_type + ".Frmt4Discount", NumberFormatter.TWOFRMTKEY);
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
        if (!this.key2formatter.containsKey(_key)) {
            final Properties properties = ERP.getSysConfig().getAttributeValueAsProperties(ERPSettings.NUMBERFRMT);
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
            this.key2formatter.put(_key, frmt);
        }
        return this.key2formatter.get(_key);
    }

    /**
     * @return static access
     */
    public static NumberFormatter get()
    {
        if (NumberFormatter.FORMATTER == null) {
            NumberFormatter.FORMATTER = new NumberFormatter();
        }
        return NumberFormatter.FORMATTER;
    }
}