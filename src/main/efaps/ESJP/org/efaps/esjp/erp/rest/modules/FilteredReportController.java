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
package org.efaps.esjp.erp.rest.modules;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.Module;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.esjp.ui.rest.dto.ValueDto;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@EFapsUUID("66384c77-abe5-4bd5-abf5-89d972f583c8")
@EFapsApplication("eFapsApp-Commons")
@Path("/ui/modules/filtered-report")
public class FilteredReportController
{

    private static final Logger LOG = LoggerFactory.getLogger(FilteredReportController.class);

    @Path("/{id}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getReport(@PathParam("id") final String moduleId,
                              @Context UriInfo uriInfo)
        throws EFapsException
    {
        LOG.info("Getting FilteredReport for: {}", moduleId);
        final var queryParams = uriInfo.getQueryParameters();
        final var module = Module.get(UUID.fromString(moduleId));
        final var className = module.getProperty("FilteredReport");
        String htmlContent = null;
        List<ValueDto> filters = null;
        try {
            final IFilteredReportProvider provider = (IFilteredReportProvider) Class.forName(className).getConstructor()
                            .newInstance();
            final var parameter = ParameterUtil.instance();
            final var report = provider.getReport(parameter);
            provider.setFilterMap(evalFilterMap(queryParams, provider));
            htmlContent = report.getHtml(parameter, true);
            filters = provider.getFilters();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            LOG.error("Catched", e);
        }
        return Response.ok(FilteredReportDto.builder()
                        .withReport(htmlContent)
                        .withFilters(filters).build())
                        .build();
    }

    protected Map<String, Object> evalFilterMap(final MultivaluedMap<String, String> queryParams,
                                                final IFilteredReportProvider provider)
    {
        final Map<String, Object> ret = new HashMap<>();
        final List<ValueDto> filters = provider.getFilters();

        for (final var entry : queryParams.entrySet()) {
            final var filterOpt = filters.stream().filter(dto -> dto.getName().equals(entry.getKey())).findFirst();
            if (filterOpt.isPresent()) {
                final Object val = switch (filterOpt.get().getType()) {
                    case DATE: {
                        yield DateTime.parse(entry.getValue().get(0));
                    }
                    case PICKLIST: {
                        yield evalPickListValue(provider, entry.getKey(), entry.getValue());
                    }
                    default:
                        yield entry.getValue().get(0);
                };
                ret.put(entry.getKey(), val);
            } else {
                ret.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return ret;
    }

    protected Object evalPickListValue(final IFilteredReportProvider provider,
                                       final String key,
                                       final List<String> values) {
        return provider.evalFilterValue4Key(key, values);
    }

}
