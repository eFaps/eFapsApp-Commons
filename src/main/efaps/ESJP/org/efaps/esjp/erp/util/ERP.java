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

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.IBitEnum;
import org.efaps.admin.datamodel.attributetype.BitEnumType;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("2806f80c-8395-41ce-9410-0c6bbb4614b7")
@EFapsRevision("$Rev$")
public final class ERP
{

    /**
     * Enum used for a multistate for Activation in ERP_DocumentType.
     */
    public enum DocTypeActivation
        implements IBitEnum
    {
        /** NONE. */
        NONE,
        /** Docu. */
        TAX,
        /** Outgoing. */
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
        PURCHASERECORD;

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
        // Commons-Configuration
        return SystemConfiguration.get(UUID.fromString("9ac2673a-18f9-41ba-b9be-5b0980bdf6f3"));
    }
}
