/*
 * Copyright 2003 - 2018 The eFaps Team
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

import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.common.uitable.CommonDelete;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;

/**
 * The Class Bin_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("11168991-8120-44f5-9a36-9a5ece633751")
@EFapsApplication("eFapsApp-Commons")
public abstract class Bin_Base
{

    /**
     * Creates the.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return create(final Parameter _parameter)
        throws EFapsException
    {
        final Create create = new Create()
        {

            @Override
            protected void add2basicInsert(final Parameter _parameter,
                                           final Insert _insert)
                throws EFapsException
            {
                final String name = new Naming().fromNumberGenerator(_parameter, null);
                if (name != null) {
                    _insert.add(CIERP.BinAbstract.Name, name);
                }
                super.add2basicInsert(_parameter, _insert);
            }

            @Override
            public void connect(final Parameter _parameter,
                                final Instance _instance)
                throws EFapsException
            {
                super.connect(_parameter, _instance);
                if (getProperty(_parameter, "BinConnectType") != null) {
                    final String type = getProperty(_parameter, "BinConnectType");
                    for (final Instance inst : getSelectedInstances(_parameter)) {
                        final Insert insert = UUIDUtil.isUUID(type) ? new Insert(UUID.fromString(type))
                                        : new Insert(type);
                        insert.add(CIERP.BinAbstract2ObjectAbstract.FromLinkAbstract, _instance);
                        insert.add(CIERP.BinDocumentAbstract2Document.ToLinkAbstract, inst);
                        insert.execute();
                    }
                }
            }
        };
        return create.execute(_parameter);
    }

    /**
     * Delete.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return delete(final Parameter _parameter)
        throws EFapsException
    {
        return new CommonDelete()
        {
            @Override
            protected boolean getValidate4Instance(final Parameter _parameter, final Instance _instance)
                throws EFapsException
            {
                final QueryBuilder queryBldr = new QueryBuilder(CIERP.BinDocumentAbstract2Document);
                queryBldr.addWhereAttrEqValue(CIERP.BinDocumentAbstract2Document.FromLinkAbstract, _instance);
                return queryBldr.getQuery().execute().isEmpty();
            };
        }.execute(_parameter);
    }
}
