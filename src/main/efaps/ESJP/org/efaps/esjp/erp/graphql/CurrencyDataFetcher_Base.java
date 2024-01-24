/*
 * Copyright 2003 - 2020 The eFaps Team
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
package org.efaps.esjp.erp.graphql;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.erp.CurrencyInst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@EFapsUUID("6bc3603a-600e-4248-a1f4-aa96a1a4394f")
@EFapsApplication("eFapsApp-Commons")
public abstract class CurrencyDataFetcher_Base
    implements DataFetcher<Object>
{
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyDataFetcher.class);

    @Override
    public Object get(final DataFetchingEnvironment _environment)
        throws Exception
    {
        final Map<String, Object> source = _environment.getSource();
        LOG.info("Fetching currency object for: {}", source);
        final Long currencyId = (Long) source.get(_environment.getField().getName());
        final Map<String, String> data = new HashMap<>();
        if (currencyId != null) {
            final var currencyInst = CurrencyInst.get(currencyId);
            data.put("isoCode", currencyInst.getISOCode());
            data.put("isoNumber", currencyInst.getISONumber());
            data.put("name", currencyInst.getName());
            data.put("symbol", currencyInst.getSymbol());
        }
        return DataFetcherResult.newResult()
                        .data(data)
                        .build();
    }
}
