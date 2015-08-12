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

import java.text.DecimalFormat;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;

/**
 * The Class RateFormatter_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("885cceb1-8100-479f-974d-b32ce948665c")
@EFapsApplication("eFapsApp-Commons")
public abstract class RateFormatter_Base
    extends AbstractCommon
{

    /**
     * Static access.
     */
    private static RateFormatter FORMATTER;

    /**
     * Gets the formatter for rate.
     *
     * @return the frmt4 rate
     * @throws EFapsException the eFaps exception
     */
    public DecimalFormat getFrmt4Rate()
        throws EFapsException
    {
        return getFormat("Rate");
    }

    /**
     * Getter method for the instance variable {@link #frmt4RateUI}.
     *
     * @return value of instance variable {@link #frmt4RateUI}
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4RateUI()
        throws EFapsException
    {
        return getFormat("RateUI");
    }

    /**
     * Getter method for the instance variable {@link #frmt4SaleRate}.
     *
     * @return value of instance variable {@link #frmt4SaleRate}
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4SaleRate()
        throws EFapsException
    {
        return getFormat("SaleRate");
    }

    /**
     * Getter method for the instance variable {@link #frmt4SaleRateUI}.
     *
     * @return value of instance variable {@link #frmt4SaleRateUI}
     * @throws EFapsException on error
     */
    public DecimalFormat getFrmt4SaleRateUI()
        throws EFapsException
    {
        return getFormat("SaleRateUI");
    }

    /**
     * @param _key key the default format is wanted for
     * @return DecimalFormat
     * @throws EFapsException on error
     */
    protected DecimalFormat getFormat(final String _key)
        throws EFapsException
    {
        return NumberFormatter.get().getFrmtFromProperties(NumberFormatter.get().getKey(_key), null,
                        ERP.RATEFRMT.get());
    }

    /**
     * Gets the.
     *
     * @return the rate formatter
     */
    protected static RateFormatter get()
    {
        if (RateFormatter_Base.FORMATTER == null) {
            RateFormatter_Base.FORMATTER = new RateFormatter();
        }
        return RateFormatter_Base.FORMATTER;
    }
}
