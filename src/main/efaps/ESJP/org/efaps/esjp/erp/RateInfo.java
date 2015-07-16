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
import org.efaps.util.EFapsException;

/**
 * Class is used as POJO and therefore is not thought to be used with standard
 * "_Base" approach.
 *
 * @author The eFaps Team
 */
@EFapsUUID("852cba11-ab22-4024-acc0-580f777c6750")
@EFapsApplication("eFapsApp-Commons")
public class RateInfo
    extends RateInfo_Base
{

    /**
     * {@inheritDoc}
     */
    public static RateInfo getDummyRateInfo()
        throws EFapsException
    {
        return RateInfo_Base.getDummyRateInfo();
    }

    /**
     * {@inheritDoc}
     */
    public static BigDecimal getRate(final Parameter _parameter,
                                        final RateInfo _rateInfo,
                                        final String _key)
        throws EFapsException
    {
        return RateInfo_Base.getRate(_parameter, _rateInfo, _key);
    }

    /**
     * {@inheritDoc}
     */
    public static String getRateFrmt(final Parameter _parameter,
                                        final RateInfo _rateInfo,
                                        final String _key)
        throws EFapsException
    {
        return RateInfo_Base.getRateUIFrmt(_parameter, _rateInfo, _key);
    }

    /**
     * {@inheritDoc}
     */
    public static BigDecimal getRateUI(final Parameter _parameter,
                                          final RateInfo _rateInfo,
                                          final String _key)
        throws EFapsException
    {
        return RateInfo_Base.getRateUI(_parameter, _rateInfo, _key);
    }

    /**
     * {@inheritDoc}
     */
    public static String getRateUIFrmt(final Parameter _parameter,
                                          final RateInfo _rateInfo,
                                          final String _key)
        throws EFapsException
    {
        return RateInfo_Base.getRateUIFrmt(_parameter, _rateInfo, _key);
    }

    /**
     * {@inheritDoc}
     */
    public static Object[] getRateObject(final Parameter _parameter,
                                            final RateInfo _rateInfo,
                                            final String _key)
        throws EFapsException
    {
        return RateInfo_Base.getRateObject(_parameter, _rateInfo, _key);
    }
}
