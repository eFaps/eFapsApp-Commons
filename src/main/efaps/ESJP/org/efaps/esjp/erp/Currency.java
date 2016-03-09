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

import java.math.BigDecimal;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("00c044cb-9175-40a4-a611-8133c080785f")
@EFapsApplication("eFapsApp-Commons")
public class Currency
    extends Currency_Base
{

    /**
     * @return the base currency for eFaps
     * @throws EFapsException on error
     */
    public static Instance getBaseCurrency()
        throws EFapsException
    {
        return Currency_Base.getBaseCurrency();
    }

    /**
     * Convert to base.
     *
     * @param _parameter the _parameter
     * @param _current the _current
     * @param _rateInfo the _rate info
     * @param _key the _key
     * @return the big decimal
     * @throws EFapsException the eFaps exception
     */
    public static BigDecimal convertToBase(final Parameter _parameter,
                                           final BigDecimal _current,
                                           final RateInfo _rateInfo,
                                           final String _key)
        throws EFapsException
    {
        return Currency_Base.convertToBase(_parameter, _current, _rateInfo, _key);
    }

    /**
     * Convert to currency.
     *
     * @param _parameter the _parameter
     * @param _current the _current
     * @param _rateInfo the _rate info
     * @param _key the _key
     * @param _periodCurrenycInstance the _period currenyc instance
     * @return the big decimal
     * @throws EFapsException the e faps exception
     */
    public static BigDecimal convertToCurrency(final Parameter _parameter,
                                               final BigDecimal _current,
                                               final RateInfo _rateInfo,
                                               final String _key,
                                               final Instance _periodCurrenycInstance)
        throws EFapsException
    {
        return Currency_Base.convertToCurrency(_parameter, _current, _rateInfo, _key, _periodCurrenycInstance);
    }
}
