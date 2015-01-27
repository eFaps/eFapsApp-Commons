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
     * String with the name of the selected company.
     */
    String COMPANYNAME = BASE + "CompanyName";

    /**
     * String with the tax number of the selected company.
     */
    String COMPANYTAX = BASE + "CompanyTaxNumber";

    /**
     * String with the tax number of the selected company.
     */
    String COMPANYACTIVITY = BASE + "CompanyActivity";

    /**
     * String with the tax number of the selected company.
     */
    String COMPANYSTREET = BASE + "CompanyStreet";

    /**
     * String with the tax number of the selected company.
     */
    String COMPANYDISTRICT = BASE + "CompanyDistrict";

    /**
     * Properties. Can be concatenated.<br/>
     * TYPENAME.Status
     */
    String DOCSTATUSCREATE = BASE + "DocumentStatus4Create";

    /**
     * Properties.<br/>
     * Can be concatenated.<br/>
     * Set a JasperReport for a Type. Used for Common Documents.
     */
    String JASPERKEY = BASE + "JasperKey";

    /**
     * Properties.<br/>
     * Can be concatenated.<br/>
     * Set Formatting information for numbers.
     */
    String NUMBERFRMT = BASE + "NumberFormatter";

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
    String RATEFRMT = BASE + "RateFormatter";

    /**
     * Properties.<br/>
     * Set Info information for rates.
     */
    String RATEINFO = BASE + "RateInfo4Type";

    /**
     * Properties. Can be concatenated.
     * Key: Normally the type name
     * Values: One of EDIT, CREATE, NONE
     */
    String ACTIONDEF = BASE + "ActionDefinition";

    /**
     * Properties. Can be concatenated.
     * Key: Normally the type name
     */
    String WARNING = BASE + "Warning";

    /**
     * Link.<br/>
     * Base Currency for the System.
     */
    String CURRENCYBASE = BASE + "CurrencyBase";

    /**
     * Properties. Can be concatenated.
     * Key: Normally the type name
     */
    String CURRENCIES = BASE + "Currencies";
}
