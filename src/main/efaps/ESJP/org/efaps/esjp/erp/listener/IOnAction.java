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
package org.efaps.esjp.erp.listener;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.db.Instance;
import org.efaps.esjp.common.listener.ITypedClass;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("2c9bbfea-9576-4c94-b622-fd5b8f88d29a")
@EFapsApplication("eFapsApp-Commons")
public interface IOnAction
    extends IEsjpListener
{

    /**
     * Called after the creation/insert of a new Document with the values
     * already set and the instance valid.
     *
     * @param _typeClass typed class instance
     * @param _parameter Parameter as passed by the eFaps API
     * @param _actionRelInst instance of the relation created
     * @throws EFapsException on error
     */
    void afterAssign(final ITypedClass _typeClass,
                     final Parameter _parameter,
                     final Instance _actionRelInst)
        throws EFapsException;

    /**
     * Called after the update a Document. Searches for a relation to an actions
     * an than executes it.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _docInst instance of the relation created
     * @throws EFapsException on error
     */
    void onDocumentUpdate(final Parameter _parameter,
                          final Instance _docInst)
        throws EFapsException;
}
