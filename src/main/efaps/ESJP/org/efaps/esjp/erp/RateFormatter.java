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
import java.util.Properties;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;

/**
 * Class is used as POJO and therefore is not thought to be used with standard
 * "_Base" approach.
 *
 * @author The eFaps Team
 */
@EFapsUUID("32b49b2d-0cef-47e8-b0a8-ee48f06c223e")
@EFapsApplication("eFapsApp-Commons")
public class RateFormatter
{
    /**
     * Static access.
     */
    private static RateFormatter FORMATTER;

    /**
     * Format for Rate.
     */
    private DecimalFormat frmt4Rate;

    /**
     * Format for rateUI.
     */
    private DecimalFormat frmt4RateUI;

    /**
     * Format for SaleRate.
     */
    private DecimalFormat frmt4SaleRate;

    /**
     * Format for SaleRateUI.
     */
    private DecimalFormat frmt4SaleRateUI;

    /**
     * @return
     */
    public DecimalFormat getFrmt4Rate()
        throws EFapsException
    {
        if (this.frmt4Rate == null) {
            this.frmt4Rate = getDefaultFormat("Rate");
        }
        return this.frmt4Rate;
    }

    /**
     * @param _key key the default format is wanted for
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    protected DecimalFormat getDefaultFormat(final String _key)
        throws EFapsException
    {
        final DecimalFormat ret = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext().getLocale());
        ret.setParseBigDecimal(true);
        ret.setRoundingMode(RoundingMode.HALF_UP);
        final Properties props =  ERP.RATEFRMT.get();
        if (props.containsKey(_key)) {
            ret.applyPattern(props.getProperty(_key));
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #frmt4Rate}.
     *
     * @param _frmt4Rate value for instance variable {@link #frmt4Rate}
     */
    public void setFrmt4Rate(final DecimalFormat _frmt4Rate)
    {
        this.frmt4Rate = _frmt4Rate;
    }

    /**
     * Getter method for the instance variable {@link #frmt4RateUI}.
     *
     * @return value of instance variable {@link #frmt4RateUI}
     */
    public DecimalFormat getFrmt4RateUI()
        throws EFapsException
    {
        if (this.frmt4RateUI == null) {
            this.frmt4RateUI = getDefaultFormat("RateUI");
        }
        return this.frmt4RateUI;
    }

    /**
     * Setter method for instance variable {@link #frmt4RateUI}.
     *
     * @param _frmt4RateUI value for instance variable {@link #frmt4RateUI}
     */
    public void setFrmt4RateUI(final DecimalFormat _frmt4RateUI)
    {
        this.frmt4RateUI = _frmt4RateUI;
    }

    /**
     * Getter method for the instance variable {@link #frmt4SaleRate}.
     *
     * @return value of instance variable {@link #frmt4SaleRate}
     */
    public DecimalFormat getFrmt4SaleRate()
        throws EFapsException
    {
        if (this.frmt4SaleRate == null) {
            this.frmt4SaleRate = getDefaultFormat("SaleRate");
        }
        return this.frmt4SaleRate;
    }

    /**
     * Setter method for instance variable {@link #frmt4SaleRate}.
     *
     * @param _frmt4SaleRate value for instance variable {@link #frmt4SaleRate}
     */
    public void setFrmt4SaleRate(final DecimalFormat _frmt4SaleRate)
    {
        this.frmt4SaleRate = _frmt4SaleRate;
    }

    /**
     * Getter method for the instance variable {@link #frmt4SaleRateUI}.
     *
     * @return value of instance variable {@link #frmt4SaleRateUI}
     */
    public DecimalFormat getFrmt4SaleRateUI()
        throws EFapsException
    {
        if (this.frmt4SaleRateUI == null) {
            this.frmt4SaleRateUI = getDefaultFormat("SaleRateUI");
        }
        return this.frmt4SaleRateUI;
    }

    /**
     * Setter method for instance variable {@link #frmt4SaleRateUI}.
     *
     * @param _frmt4SaleRateUI value for instance variable
     *            {@link #frmt4SaleRateUI}
     */
    public void setFrmt4SaleRateUI(final DecimalFormat _frmt4SaleRateUI)
    {
        this.frmt4SaleRateUI = _frmt4SaleRateUI;
    }

    /**
     * Gets the.
     *
     * @return the rate formatter
     */
    public static RateFormatter get()
    {
        if (RateFormatter.FORMATTER == null) {
            RateFormatter.FORMATTER = new RateFormatter();
        }
        return RateFormatter.FORMATTER;
    }

}
