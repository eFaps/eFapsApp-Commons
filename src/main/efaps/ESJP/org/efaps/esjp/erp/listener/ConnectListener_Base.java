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


package org.efaps.esjp.erp.listener;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.datamodel.Relationship;
import org.efaps.esjp.common.uisearch.Connect_Base.ConnectType;
import org.efaps.esjp.common.uisearch.IConnectListener;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("d61a3c0a-e709-49f3-b620-ddb21ba9a530")
@EFapsApplication("eFapsApp-Commons")
public abstract class ConnectListener_Base
    implements IConnectListener
{
    @Override
    public void evalRelationship(final Parameter _parameter,
                                 final ConnectType _connectType,
                                 final int _idx)
        throws EFapsException
    {
        if (Relationship.Undefined.equals(_connectType.getRelationship())) {
            final Properties properties = ERP.RELATIONSHIPS.get();
            final String relKey = _connectType.getType().getName() + ".Relationship";
            final String fromKey = _connectType.getType().getName() + ".FromAttribute";
            final String toKey = _connectType.getType().getName() + ".ToAttribute";
            final String unKey = _connectType.getType().getName() + ".Unique";
            if (properties.containsKey(relKey) && properties.containsKey(fromKey) && properties.containsKey(toKey)) {
                final Relationship relationship =  EnumUtils.getEnum(Relationship.class,
                                properties.getProperty(relKey));
                _connectType.setRelationship(relationship);
                if (_connectType.getChildAttr().getName().equals(properties.getProperty(fromKey))
                                || _connectType.getParentAttr().getName().equals(properties.getProperty(toKey))) {
                    _connectType.setParentIsFrom(false);
                }
                if (properties.containsKey(unKey)) {
                    _connectType.setUnique(BooleanUtils.toBoolean(properties.getProperty(unKey)));
                }
            }
        }
    }


    @Override
    public int getWeight()
    {
        return 0;
    }
}
