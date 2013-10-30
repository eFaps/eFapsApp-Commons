/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.math.BigDecimal;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_base</code>"
 * class.
 * @deprecated replaced {@link Byte} RateInfo
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("5c118791-bf38-4c20-b606-b825058570a5")
@EFapsRevision("$Rev$")
@Deprecated
public class Rate
    extends Rate_Base
{


    /**
     * @param curInstance
     * @param one
     */
    public Rate(final CurrencyInst _curInstance,
                final BigDecimal _value,
                final BigDecimal _label)
    {
        super(_curInstance, _value, _label);
    }

    /**
     * @param curInstance
     * @param one
     */
    public Rate(final CurrencyInst _curInstance,
                final BigDecimal _value)
    {
        super(_curInstance, _value);
    }
}
