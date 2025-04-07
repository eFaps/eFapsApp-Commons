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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.ui.rest.dto.ValueDto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = FilteredReportDto.Builder.class)
@EFapsUUID("7aab63b7-aff0-41c8-aa25-c74bbf685751")
@EFapsApplication("eFapsApp-Commons")
public class FilteredReportDto
{

    private final String report;
    private final List<ValueDto> filters;

    private FilteredReportDto(Builder builder)
    {
        this.report = builder.report;
        this.filters = builder.filters;
    }

    public String getReport()
    {
        return report;
    }

    public List<ValueDto> getFilters()
    {
        return filters;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {

        private String report;
        private List<ValueDto> filters = Collections.emptyList();

        private Builder()
        {
        }

        public Builder withReport(String report)
        {
            this.report = report;
            return this;
        }

        public Builder withFilters(List<ValueDto> filters)
        {
            this.filters = filters;
            return this;
        }

        public FilteredReportDto build()
        {
            return new FilteredReportDto(this);
        }
    }
}
