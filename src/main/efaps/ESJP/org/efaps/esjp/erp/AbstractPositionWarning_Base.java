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

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("641ccc69-5ccf-44a5-bd6d-e004b2732a9b")
@EFapsRevision("$Rev$")
public abstract class AbstractPositionWarning_Base
    extends AbstractWarning
    implements IPositionWarning
{
    /**
     * Position of the warning.
     */
    private int position;

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition()
    {
        return this.position;
    }

    /**
     * Setter method for instance variable {@link #position}.
     *
     * @param _position value for instance variable {@link #position}
     * @return this for chaining
     */
    public AbstractPositionWarning_Base setPosition(final int _position)
    {
        this.position = _position;
        return this;
    }
}
