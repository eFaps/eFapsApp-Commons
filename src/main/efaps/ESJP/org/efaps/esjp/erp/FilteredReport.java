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


package org.efaps.esjp.erp;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import net.sf.dynamicreports.report.builder.column.ComponentColumnBuilder;


/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_base</code>"
 * class.
 *
 * @author The eFaps Team
 */
@EFapsUUID("0fce31d2-c4b3-4058-9a2c-de2cd41f7615")
@EFapsApplication("eFapsApp-Commons")
public class FilteredReport
    extends FilteredReport_Base
{
    /**
     * @param _object object to be cast to enum value
     * @return enum value
     * @param <S> return type
     */
    public static <S> S getEnumValue(final Object _object)
    {
        return FilteredReport_Base.<S>getEnumValue(_object);
    }

    /**
     * Gets the link column.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _field the field
     * @return the link column
     */
    public static ComponentColumnBuilder getLinkColumn(final Parameter _parameter,
                                                       final String _field)
    {
        return FilteredReport_Base.getLinkColumn(_parameter, _field);
    }
}
