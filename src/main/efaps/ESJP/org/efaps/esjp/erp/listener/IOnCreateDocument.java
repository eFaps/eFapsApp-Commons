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
 *
 * @author The eFaps Team
 */
@EFapsUUID("835949a3-4a9d-4c89-b0fa-a7f188e30c6d")
@EFapsApplication("eFapsApp-Commons")
public interface IOnCreateDocument
    extends IEsjpListener
{

    /**
     * Called after the creation/insert of a new Document with the values
     * already set and the instance valid.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _createdDoc Created Document
     * @throws EFapsException on error
     */
    void afterCreate(final Parameter parameter,
                     final Instance instance)
        throws EFapsException;

    /**
     * Called for the standard JavaScript field value to be able to add own
     * scripts via listener.
     *
     * @param _typeClass Typed class as an easy way to distinguish
     * @param _parameter Parameter as passed by the eFaps API
     * @throws EFapsException on error
     * @return JavaScript for UserInterface
     */
    default CharSequence getJavaScript4Doc(final ITypedClass _typeClass,
                                           final Parameter _parameter)
        throws EFapsException
    {
        return "";
    }
}
