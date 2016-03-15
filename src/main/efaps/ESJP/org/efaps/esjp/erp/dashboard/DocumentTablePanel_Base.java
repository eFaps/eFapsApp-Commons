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
package org.efaps.esjp.erp.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.ui.dashboard.AbstractTablePanel;
import org.efaps.util.EFapsException;

/**
 * The Class DocumentTablePanel_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("a0f830b7-63af-46ff-864c-91f549ad115b")
@EFapsApplication("eFapsApp-Commons")
public abstract class DocumentTablePanel_Base
    extends AbstractTablePanel
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract dashboard panel.
     *
     * @param _config the _config
     */
    public DocumentTablePanel_Base(final String _config)
    {
        super(_config);
    }

    /**
     * Gets the selects.
     *
     * @return the selects
     */
    protected List<String> getSelects()
    {
        final List<String> ret = new ArrayList<>();

        final Properties properties = getConfig();
        final String formatStr = "Select%02d";
        for (int i = 1; i < 100; i++) {
            final String nameTmp = String.format(formatStr, i);
            if (properties.containsKey(nameTmp)) {
                ret.add(properties.getProperty(nameTmp));
            } else {
                break;
            }
        }
        return ret;
    }

    /**
     * Gets the selects.
     *
     * @param _idx the idx
     * @return the selects
     */
    protected String getLabel(final int _idx)
    {
        final String ret;
        final Properties properties = getConfig();
        final String formatStr = "Label%02d";
        final String nameTmp = String.format(formatStr, _idx);
        if (properties.containsKey(nameTmp)) {
            ret = properties.getProperty(nameTmp);
        } else {
            ret= null;
        }
        return ret;
    }

    @Override
    protected List<Map<String, Object>> getDataSource()
        throws EFapsException
    {
        final List<Map<String, Object>> ret = new ArrayList<>();
        final QueryBuilder queryBldr = AbstractCommon.getQueryBldrFromProperties(getConfig());
        final MultiPrintQuery multi = queryBldr.getPrint();
        final List<String> selects = getSelects();
        for (final String select : selects) {
            multi.addSelect(select);
        }
        multi.execute();
        while (multi.next()) {
            final Map<String, Object> map = new LinkedHashMap<>();
            int idx = 0;
            for (final String select : selects) {
                idx++;
                final Object value = multi.getSelect(select);
                final String label = getLabel(idx);
                if (label == null) {
                    final Attribute attr = multi.getAttribute4Select(select);
                    map.put(DBProperties.getProperty(attr.getLabelKey()), value);
                } else {
                    map.put(label, value);
                }
            }
            ret.add(map);
        }
        return ret;
    }
}
