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
import java.text.DecimalFormat;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsListener;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * Class is used as POJO and therefore is not thought to be used with standard
 * "_Base" approach.
 *
 * @author The eFaps Team
 */
@EFapsUUID("999443c8-8f9f-47be-aad1-974a1b09421b")
@EFapsApplication("eFapsApp-Commons")
@EFapsListener
public final class NumberFormatter
    extends NumberFormatter_Base
{
    /**
     * Singelton constructor.
     */
    NumberFormatter()
    {
    }

    /**
     * Parses the.
     *
     * @param _strValue the str value
     * @return the big decimal
     * @throws EFapsException on error
     */
    public static BigDecimal parse(final String _strValue)
        throws EFapsException
    {
        return NumberFormatter_Base.parse(_strValue);
    }

    /**
     * Parses the.
     *
     * @param _strValue the str value
     * @param _format the format
     * @return the big decimal
     * @throws EFapsException on error
     */
    public static BigDecimal parse(final String _strValue,
                                   final DecimalFormat _format)
        throws EFapsException
    {
        return  NumberFormatter_Base.parse(_strValue, _format);
    }

    /**
     * @return static access
     */
    public static NumberFormatter get()
    {
        return NumberFormatter_Base.get();
    }
}
