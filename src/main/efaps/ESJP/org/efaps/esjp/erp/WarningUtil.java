/*
 * Copyright 2003 - 2014 The eFaps Team
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

import java.util.List;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;


/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_base</code>"
 * class.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("03767017-6122-4f85-8b30-a0360eb606a2")
@EFapsRevision("$Rev$")
public class WarningUtil
    extends WarningUtil_Base
{

    public static StringBuilder getHtml4Warning(final List<IWarning> _warnings)
        throws EFapsException
    {
        return WarningUtil_Base.getHtml4Warning(_warnings);
    }

    public static boolean hasError(final List<IWarning> _warnings)
        throws EFapsException
    {
        return WarningUtil_Base.hasError(_warnings);
    }
}