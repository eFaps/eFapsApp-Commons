/*
 * Copyright 2003 - 2016 The eFaps Team
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

import java.util.Set;
import java.util.UUID;

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
@EFapsUUID("1961a24b-40d7-45a0-b3d8-82578022ee08")
@EFapsApplication("eFapsApp-Commons")
public class CurrencyInst
    extends CurrencyInst_Base
{

    /**
     * Constructor when used as instance object. to access parameters from a
     * currency.
     *
     * @param _instance instance of the currency
     */
    public CurrencyInst(final Instance _instance)
    {
        super(_instance);
    }

    /**
     * @param _instance instance the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    public static CurrencyInst get(final Instance _instance)
        throws EFapsException
    {
        return CurrencyInst_Base.get(_instance);
    }

    /**
     * @param _currencyId id the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    public static CurrencyInst get(final Long _currencyId)
        throws EFapsException
    {
        return CurrencyInst_Base.get(_currencyId);
    }

    /**
     * @param _currencyUUID uuid the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    public static CurrencyInst get(final UUID _currencyUUID)
        throws EFapsException
    {
        return CurrencyInst_Base.get(_currencyUUID);
    }

    /**
     * @param _object object the CurrencyInst is wanted for
     * @return new CurrencyInst
     * @throws EFapsException on error
     */
    public static CurrencyInst get(final Object _object)
        throws EFapsException
    {
        return CurrencyInst_Base.get(_object);
    }

    /**
     * @return Set of available CurrencyInst
     * @throws EFapsException on error
     */
    public static Set<CurrencyInst> getAvailable()
        throws EFapsException
    {
        return CurrencyInst_Base.getAvailable();
    }
}
