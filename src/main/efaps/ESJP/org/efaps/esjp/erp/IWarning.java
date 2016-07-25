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

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("9d838692-aca6-4472-a2c8-3a7424095ec6")
@EFapsApplication("eFapsApp-Commons")
public interface IWarning
{
    /**
     * @return the message shown to the User
     *  @throws EFapsException on error
     */
    String getMessage() throws EFapsException;

    /**
     * If set to true the warning dialog must not have the command to go along.
     * @return true is it is an error, else false
     * @throws EFapsException on error
     */
    boolean isError() throws EFapsException;
}
