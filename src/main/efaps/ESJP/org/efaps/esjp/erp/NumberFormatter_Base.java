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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.admin.common.IReloadCacheListener;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;
import org.jfree.util.Log;

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
    public static String FRMTKEY = NumberFormatter.class.getName() + ".Formatter";

    /**
     * Key for basic formatter.
     */
    public static String TWOFRMTKEY = NumberFormatter.class.getName() + ".TwoDigitsFormatter";

    /**
     * Key for basic formatter.
     */
    public static String ZEROFRMTKEY = NumberFormatter.class.getName() + ".ZeroDigitsFormatter";

    /**
     * Formatter mapping.
     */
    private final Map<FormatterKey, DecimalFormat> key2formatter = new HashMap<>();

    /**
     * Method to get a formater.
     *
     * @return a formater
     * @throws EFapsException on error
     */
    public DecimalFormat getTwoDigitsFormatter()
        throws EFapsException
    {
        final FormatterKey key = getKey(NumberFormatter_Base.TWOFRMTKEY);
        if (!key2formatter.containsKey(key)) {
            key2formatter.put(key, getFormatter(2, 2));
        }
        return key2formatter.get(key);
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
        final FormatterKey key = getKey(NumberFormatter_Base.ZEROFRMTKEY);
        if (!key2formatter.containsKey(key)) {
            key2formatter.put(key, getFormatter(0, 0));
        }
        return key2formatter.get(key);
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
        final FormatterKey key = getKey(NumberFormatter_Base.FRMTKEY);
        if (!key2formatter.containsKey(key)) {
            key2formatter.put(key, getFormatter(null, null));
        }
        return key2formatter.get(key);
    }

    public DecimalFormat getFrmt4UnitPrice(final Type _type)
        throws EFapsException
    {
        return evalFrmt(_type, FrmtKey.UNITPRICE);
    }

    public DecimalFormat getFrmt4Quantity(final Type _type)
        throws EFapsException
    {
        return evalFrmt(_type, FrmtKey.QUANTITY);
    }

    public DecimalFormat getFrmt4Tax(final Type _type)
        throws EFapsException
    {
        return evalFrmt(_type, FrmtKey.TAX);
    }

    public DecimalFormat getFrmt4Total(final Type _type)
        throws EFapsException
    {
        return evalFrmt(_type, FrmtKey.TOTAL);
    }

    public DecimalFormat getFrmt4Discount(final Type _type)
        throws EFapsException
    {
        return evalFrmt(_type, FrmtKey.DISCOUNT);
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

    public DecimalFormat getFrmt(final String _prefix,
                                 final FrmtKey _frmtKey)
        throws EFapsException
    {
        return getFrmtFromSysConf(_prefix + "." + _frmtKey.key, _frmtKey.defaultKey);
    }

    /**
     * Checks if the given value is parseable.
     *
     * @param _strValue the str value
     * @return true, if is parses the able
     */
    public boolean isParseAble(final String _strValue)
    {
        boolean ret = true;
        try {
            getFormatter().parse(_strValue);
        } catch (EFapsException | ParseException e) {
            ret = false;
        }
        return ret;
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
        final FormatterKey key = getKey(_key);
        if (!key2formatter.containsKey(key)) {
            getFrmtFromProperties(key, getKey(_default), ERP.NUMBERFRMT.get());
        }
        return key2formatter.get(key);
    }

    /**
     * Gets the frmt from properties.
     *
     * @param _key key to the Formatter
     * @param _default default key for a formatter
     * @param _properties the properties
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    protected DecimalFormat getFrmtFromProperties(final FormatterKey _key,
                                                  final FormatterKey _default,
                                                  final Properties _properties)
        throws EFapsException
    {
        if (!key2formatter.containsKey(_key)) {
            final DecimalFormat frmt;
            if (_properties.containsKey(_key.baseKey)) {
                frmt = getFormatter(null, null);
                frmt.applyPattern(_properties.getProperty(_key.baseKey));
            } else if (_default != null && _properties.containsKey(_default.baseKey)) {
                frmt = getFormatter(null, null);
                frmt.applyPattern(_properties.getProperty(_default.baseKey));
            } else {
                // init
                getTwoDigitsFormatter();
                getZeroDigitsFormatter();
                getFormatter();
                if (_default == null) {
                    frmt = getFormatter();
                } else {
                    frmt = key2formatter.get(_default);
                }
            }
            key2formatter.put(_key, frmt);
        }
        return key2formatter.get(_key);
    }

    protected boolean hasFrmt(final FormatterKey _key)
        throws EFapsException
    {
        return key2formatter.containsKey(_key) || ERP.NUMBERFRMT.get().containsKey(_key.baseKey);
    }

    protected DecimalFormat evalFrmt(final Type _type,
                                     final FrmtKey _frmtKey)
        throws EFapsException
    {
        DecimalFormat ret = null;
        Type currentType = _type;
        while (ret == null && currentType != null) {
            final String currentKey = currentType.getName() + "." + _frmtKey.key;
            if (hasFrmt(getKey(currentKey))) {
                ret = getFrmtFromSysConf(currentKey, _frmtKey.defaultKey);
            } else {
                currentType = currentType.getParentType();
            }
        }
        if (ret == null) {
            ret = getFrmtFromSysConf(_type.getName() + "." + _frmtKey.key, _frmtKey.defaultKey);
        }
        return ret;
    }

    /**
     * Gets the key.
     *
     * @param _baseKey the _base key
     * @return the key
     * @throws EFapsException on error
     */
    protected FormatterKey getKey(final String _baseKey)
        throws EFapsException
    {
        return new FormatterKey(_baseKey, Context.getThreadContext().getLocale().toString(),
                        Context.getThreadContext().getCompany() == null ? "0"
                                        : String.valueOf(Context.getThreadContext().getCompany().getId()));
    }

    /**
     * Parses the.
     *
     * @param _strValue the str value
     * @return the big decimal
     * @throws EFapsException on error
     */
    protected static BigDecimal parse(final String _strValue)
        throws EFapsException
    {
        return NumberFormatter.parse(_strValue, NumberFormatter_Base.get().getFormatter());
    }

    /**
     * Parses the.
     *
     * @param _strValue the str value
     * @param _format the format
     * @return the big decimal
     * @throws EFapsException on error
     */
    protected static BigDecimal parse(final String _strValue,
                                      final DecimalFormat _format)
        throws EFapsException
    {
        BigDecimal ret;
        try {
            ret = (BigDecimal) _format.parse(_strValue);
        } catch (final ParseException e) {
            Log.warn("Catched parsing exception", e);
            ret = BigDecimal.ZERO;
        }
        return ret;
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

    /**
     * The Class FormatterKey used to identify a formatter.
     */
    public static class FormatterKey
    {

        /** The base key. */
        private final String baseKey;

        /** The locale. */
        private final String locale;

        /** The company. */
        private final String company;

        /**
         * Instantiates a new formatter key.
         *
         * @param _baseKey the _base key
         * @param _locale the _locale
         * @param _company the _company
         */
        public FormatterKey(final String _baseKey,
                            final String _locale,
                            final String _company)
        {
            baseKey = _baseKey;
            locale = _locale;
            company = _company;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof FormatterKey) {
                ret = baseKey.equals(((FormatterKey) _obj).baseKey)
                                && locale.equals(((FormatterKey) _obj).locale)
                                && company.equals(((FormatterKey) _obj).company);
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return baseKey.hashCode() + locale.hashCode() + company.hashCode();
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    public enum FrmtKey
    {
        UNITPRICE("Frmt4UnitPrice", NumberFormatter_Base.TWOFRMTKEY),
        TOTAL("Frmt4Total", NumberFormatter_Base.TWOFRMTKEY),
        QUANTITY("Frmt4Quantity", NumberFormatter_Base.ZEROFRMTKEY),
        TAX("Frmt4Tax", NumberFormatter_Base.TWOFRMTKEY),
        DISCOUNT("Frmt4Discount", NumberFormatter_Base.TWOFRMTKEY);

        public String key;
        public String defaultKey;

        FrmtKey(final String _key,
                final String _defaultKey)
        {
            key = _key;
            defaultKey = _defaultKey;
        }
    }
}
