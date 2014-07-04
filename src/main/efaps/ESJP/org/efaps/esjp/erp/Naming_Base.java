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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.efaps.admin.common.NumberGenerator;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.esjp.erp.util.ERPSettings;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b37a8c36-5925-4546-b8fc-06aa570fa72c")
@EFapsRevision("$Rev$")
public abstract class Naming_Base
    extends AbstractCommon
{

    /**
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key key for the properties
     * @return new Name
     * @throws EFapsException on error
     */
    public String fromNumberGenerator(final Parameter _parameter,
                                      final String _key)
        throws EFapsException
    {
        return fromNumberGenerator(_parameter, _parameter.getInstance(), _key);
    }

    /**
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _instance Instance to be evaluated
     * @param _key key for the properties
     * @return new Name
     * @throws EFapsException on error
     */
    public String fromNumberGenerator(final Parameter _parameter,
                                      final Instance _instance,
                                      final String _key)
        throws EFapsException
    {
        String ret = null;
        final Properties props = ERP.getSysConfig().getAttributeValueAsProperties(ERPSettings.NUMBERGENERATOR, true);
        final String ngKey = props.getProperty(_key);

        final List<Object> argList = new ArrayList<>();
        final List<String> selects = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            final String params = props.getProperty(_key + ".Parameter" + String.format("%02d", i));
            if (params == null) {
                break;
            } else {
                if ("date".equalsIgnoreCase(params)) {
                    argList.add(new Date());
                } else {
                    selects.add(params);
                }
            }
        }
        if (!selects.isEmpty() && _instance != null && _instance.isValid()) {
            final PrintQuery print = new PrintQuery(_instance);
            print.addSelect(selects.toArray(new String[selects.size()]));
            print.executeWithoutAccessCheck();
            for (final String select : selects) {
                argList.add(print.getSelect(select));
            }
        }

        final NumberGenerator numGen = isUUID(ngKey) ? NumberGenerator.get(UUID.fromString(ngKey)) : NumberGenerator
                        .get(ngKey);
        if (!argList.isEmpty()) {
            ret = numGen.getNextVal(argList.toArray());
        } else {
            ret = numGen.getNextVal();
        }
        return ret;
    }

}
