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

import java.util.List;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.jasperreport.AbstractDynamicReport;
import org.efaps.esjp.ui.rest.dto.ValueDto;
import org.efaps.util.EFapsException;

@EFapsUUID("ff6a7a04-ed37-442e-b44b-75722acb0775")
@EFapsApplication("eFapsApp-Commons")
public interface IFilteredReportProvider
{

    AbstractDynamicReport getReport(final Parameter parameter)
        throws EFapsException;

    void setFilterMap(final Map<String, Object> filterMap);

    List<ValueDto> getFilters();

    default Object evalFilterValue4Key(String key,
                                       List<String> values)
    {
        return null;
    }
}
