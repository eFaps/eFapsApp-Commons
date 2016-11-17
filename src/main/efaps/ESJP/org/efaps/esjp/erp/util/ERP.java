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

package org.efaps.esjp.erp.util;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.IBitEnum;
import org.efaps.admin.datamodel.attributetype.BitEnumType;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.annotation.EFapsSysConfAttribute;
import org.efaps.api.annotation.EFapsSysConfLink;
import org.efaps.api.annotation.EFapsSystemConfiguration;
import org.efaps.esjp.admin.common.systemconfiguration.BooleanSysConfAttribute;
import org.efaps.esjp.admin.common.systemconfiguration.PropertiesSysConfAttribute;
import org.efaps.esjp.admin.common.systemconfiguration.StringSysConfAttribute;
import org.efaps.esjp.admin.common.systemconfiguration.SysConfLink;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("2806f80c-8395-41ce-9410-0c6bbb4614b7")
@EFapsApplication("eFapsApp-Commons")
@EFapsSystemConfiguration("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3")
public final class ERP
{

    /** The base. */
    public static final String BASE = "org.efaps.commons.";

    /** Commons-Configuration. */
    public static final UUID SYSCONFUUID = UUID.fromString("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYNAME = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyName")
                    .description("Name of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYTAX = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyTaxNumber")
                    .description("Tax number of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYACTIVITY = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyActivity")
                    .description("Activity of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYSTREET = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyStreet")
                    .description("Street of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYREGION = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyRegion")
                    .description("Region of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYCITY = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyCity")
                    .description("City of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYDISTRICT = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyDistrict")
                    .description("District of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute COMPANYCOUNTRY = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CompanyCountry")
                    .defaultValue("PERU")
                    .description("Country of the selected company.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute FILTERREPORTCONFIG = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "Config4FilteredReport")
                    .description("Basic Configurations for Filtered Reports.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute DOCSTATUSCREATE = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "DocumentStatus4Create")
                    .description("TYPENAME.Status.")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute RELATIONSHIPS = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "Relationships")
                    .description("Properties like TYPENAME.Relationship, TYPENAME.FromAttribute and "
                                    + "TYPENAME.ToAttribute. Optional TYPENAME.Unique")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute JASPERKEY = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "JasperKey")
                    .description("Set a JasperReport for a Type. Used for Common Documents.")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute NUMBERFRMT = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "NumberFormatter")
                    .description("Set Formatting information for numbers..")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute NUMBERGENERATOR = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "NumberGenerators")
                    .description("Set a NumberGenerator for a Type. Used for Common Documents")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute RATEFRMT = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "RateFormatter")
                    .description(" Set Formatting information for rates")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute RATEINFO = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "RateInfo4Key")
                    .description("Set Info information for rates.")
                    .addDefaultValue("DOCDATEPURCHASE", "purchase")
                    .addDefaultValue("DOCDATESALE", "sale")
                    .addDefaultValue("TRANSDATEPURCHASE", "purchase")
                    .addDefaultValue("TRANSDATESALE", "sale")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute ACTIONDEF = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "ActionDefinition")
                    .description(" Key: Normally the type name.Values: One of EDIT, CREATE, NONE\n"
                                    + "To be able to set the date:\n"
                                    + "KEY.Date=true")
                    .concatenate(true);

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute WARNING = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "Warning")
                    .description(" Key: Normally the type name");

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute CURRENCIES = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "Currencies")
                    .description("Configuration related to Currencies.\n"
                                    + "CURRENCY: UUID or ISOCODE of Currency\n"
                                    + "CURRENCY.ForceDailyRate=true\n"
                                    + "CURRENCY.Force4Role01=ROLE\n"
                                    + "CURRENCY.Warn4Role01=ROLE\n");

    /** See description. */
    @EFapsSysConfLink
    public static final SysConfLink CURRENCYBASE = new SysConfLink()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "CurrencyBase")
                    .description("Base Currency.");

    /** See description. */
    @EFapsSysConfAttribute
    public static final BooleanSysConfAttribute BIN_ACTIVATE = new BooleanSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "BinUser.Activate")
                    .description("Activate the mechanism for BinUser");

    /**
     * Enum used for a multistate for Activation in ERP_DocumentType.
     */
    public enum DocTypeActivation
        implements IBitEnum
    {
        /** NONE. */
        NONE, /** Docu. */
        TAX, /** Outgoing. */
        NOTAX;

        /**
         * {@inheritDoc}
         */
        @Override
        public int getInt()
        {
            return BitEnumType.getInt4Index(ordinal());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getBitIndex()
        {
            return ordinal();
        }
    }

    /**
     * Enum used for a multistate for Configuration in ERP_DocumentType.
     */
    public enum DocTypeConfiguration
        implements IBitEnum
    {
        /** Documents will be used in the PurchaseRecors from Accounting. */
        PURCHASERECORD, /**
                         * Documents is marked as a type valid for Professional
                         * Service.
                         */
        PROFESSIONALSERVICE;

        /**
         * {@inheritDoc}
         */
        @Override
        public int getInt()
        {
            return BitEnumType.getInt4Index(ordinal());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getBitIndex()
        {
            return ordinal();
        }
    }

    /**
     * Enum used for a multistate for Configuration in ERP_DocumentType.
     */
    public enum DocRelationSituation
        implements IBitEnum
    {
        /** The partial. */
        PARTIAL;

        /**
         * {@inheritDoc}
         */
        @Override
        public int getInt()
        {
            return BitEnumType.getInt4Index(ordinal());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getBitIndex()
        {
            return ordinal();
        }
    }

    /**
     * Singelton.
     */
    private ERP()
    {
    }

    /**
     * @return the SystemConfigruation for Sales
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration getSysConfig()
        throws CacheReloadException
    {
        return SystemConfiguration.get(SYSCONFUUID);
    }
}
