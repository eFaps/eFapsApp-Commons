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


package org.efaps.esjp.erp.util;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3e81d806-3b66-45d4-a5d8-19a6fc3408a1")
@EFapsRevision("$Rev$")
public interface ERPSettings
{
    /**
     * Base key.
     */
    String BASE = "org.efaps.commons.";

    /**
     * Properties. Can be concatenated.<br/>
     * TYPENAME.Status
     */
    String DOCSTATUSCREATE = ERPSettings.BASE + "DocumentStatus4Create";

    /**
     * Properties.<br/>
     * Can be concatenated.<br/>
     * Set a JasperReport for a Type. Used for Common Documents.
     */
    String JASPERKEY = ERPSettings.BASE + "JasperKey";

    /**
     * Properties.<br/>
     * Can be concatenated.<br/>
     * Set Formatting information for numbers.
     */
    String NUMBERFRMT = ERPSettings.BASE + "NumberFormatter";

    /**
     * Properties.<br/>
     * Can be concatenated.<br/>
     * Set a NumberGenerator for a Type. Used for Common Documents.
     */
    String NUMBERGENERATOR = "org.efaps.erp.NumberGenerators";

    /**
     * Properties.<br/>
     * Set Formatting information for rates.
     */
    String RATEFRMT = ERPSettings.BASE + "RateFormatter";

    /**
     * Properties.<br/>
     * Set Info information for rates.
     */
    String RATEINFO = ERPSettings.BASE + "RateInfo4Type";

    /**
     * Properties. Can be concatenated.
     * Key: Normally the type name
     * Values: One of EDIT, CREATE, NONE
     */
    String ACTIONDEF = ERPSettings.BASE + "ActionDefinition";

    /**
     * Properties. Can be concatenated.
     * Key: Normally the type name
     */
    String WARNING = ERPSettings.BASE + "Warning";

    /**
     * Link.<br/>
     * Base Currency for the System.
     */
    String CURRENCYBASE = ERPSettings.BASE + "CurrencyBase";

    /**
     * Properties. Can be concatenated.
     * Key: Normally the type name
     */
    String CURRENCIES = ERPSettings.BASE + "Currencies";
}
