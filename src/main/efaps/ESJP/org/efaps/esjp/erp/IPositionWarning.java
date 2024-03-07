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

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

/**
 * Interface for warnings inside a table.
 *
 * @author The eFaps Team
 */
@EFapsUUID("6c09c446-31bf-4e1c-95e8-8a76de58766a")
@EFapsApplication("eFapsApp-Commons")
public interface IPositionWarning
    extends IWarning
{

    /**
     * @return the positin of the warning
     * @throws EFapsException on error
     */
    int getPosition()
        throws EFapsException;
}
