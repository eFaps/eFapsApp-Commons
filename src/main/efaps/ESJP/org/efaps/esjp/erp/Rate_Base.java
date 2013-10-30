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
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 * @deprecated replaced {@link Byte} RateInfo
 */
@EFapsUUID("23338189-9272-4d3e-997e-ac41215d9487")
@EFapsRevision("$Rev$")
@Deprecated
public abstract class Rate_Base
{
    private final BigDecimal value;
    private final BigDecimal label;
    private final CurrencyInst curInstance;


    /**
     * @param curInstance
     * @param one
     */
    public Rate_Base(final CurrencyInst _curInstance,
                     final BigDecimal _value,
                     final BigDecimal _label)
    {
        this.curInstance = _curInstance;
        this.value = _value;
        this.label = _label;
    }


    /**
     * @param _curInstance
     * @param one
     */
    public Rate_Base(final CurrencyInst _curInstance,
                     final BigDecimal _value)
    {
        this(_curInstance, _value, _value);
    }


    /**
     * @return
     */
    public BigDecimal getValue()
    {
        return this.value;
    }

    /**
     * Getter method for the instance variable {@link #curInstance}.
     *
     * @return value of instance variable {@link #curInstance}
     */
    public CurrencyInst getCurInstance()
    {
        return this.curInstance;
    }


    /**
     * Getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public BigDecimal getLabel()
    {
        return this.label;
    }
}
