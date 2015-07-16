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

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsListener;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * Class is used as POJO and therefore is not thought to be used with standard
 * "_Base" approach.
 *
 * @author The eFaps Team
 * @version $Id$
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
     * @return static access
     */
    public static NumberFormatter get()
    {
        return NumberFormatter_Base.get();
    }
}
