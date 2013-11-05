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
import java.text.Format;
import java.text.NumberFormat;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;


/**
 * Class is used as POJO and therefore is not thought to be used with standard
 * "_Base" approach.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("999443c8-8f9f-47be-aad1-974a1b09421b")
@EFapsRevision("$Rev$")
public class NumberFormatter
{
    private static NumberFormatter FORMATTER;

    /**
     * Simple basic formater mainly used to parse and create
     * accurate String values.
     */
    private DecimalFormat formater;

    /**
     * Method to get a formater.
     *
     * @return a formater
     * @throws EFapsException on error
     */
    public DecimalFormat getTwoDigitsFormatter()
        throws EFapsException
    {
        return getFormatter(2, 2);
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
        return getFormatter(0, 0);
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
        if (this.formater == null) {
            this.formater = getFormatter(null, null);
        }
        return this.formater;
    }


    public static NumberFormatter get()
    {
        if (NumberFormatter.FORMATTER == null) {
            NumberFormatter.FORMATTER = new NumberFormatter();
        }
        return NumberFormatter.FORMATTER;
    }


    /**
     * @return
     */
    public DecimalFormat getFrmt4UnitPrice(final String _type)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public DecimalFormat getFrmt4Quantity(final String _type)
    {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * @param _typeName
     * @return
     */
    public DecimalFormat getFrmt4Total(final String _typeName)
    {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * @param _typeName4SysConf
     * @return
     */
    public Format getFrmt4Discount(final String _typeName4SysConf)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
