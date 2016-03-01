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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.esjp.common.dashboard.AbstractDashboardPanel;
import org.efaps.esjp.erp.Currency;
import org.efaps.esjp.erp.CurrencyInst;
import org.efaps.esjp.erp.RateInfo;
import org.efaps.esjp.ui.html.dojo.charting.Axis;
import org.efaps.esjp.ui.html.dojo.charting.Data;
import org.efaps.esjp.ui.html.dojo.charting.LineChart;
import org.efaps.esjp.ui.html.dojo.charting.Orientation;
import org.efaps.esjp.ui.html.dojo.charting.Plot;
import org.efaps.esjp.ui.html.dojo.charting.Serie;
import org.efaps.esjp.ui.html.dojo.charting.Util;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("986640f5-40a2-43da-9e19-0980a94d1be9")
@EFapsApplication("eFapsApp-Commons")
public abstract class CurrencyPanel_Base
    extends AbstractDashboardPanel
    implements IEsjpSnipplet
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new currency panel_ base.
     */
    public CurrencyPanel_Base()
    {
        super();
    }

    /**
     * Instantiates a new currency panel_ base.
     *
     * @param _config the _config
     */
    public CurrencyPanel_Base(final String _config)
    {
        super(_config);
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    protected Integer getDays()
    {
        return Integer.valueOf(getConfig().getProperty("Days", "14"));
    }

   /**
    * Gets the date format.
    *
    * @return the date format
    * @throws EFapsException the e faps exception
    */
    protected String getDateFormat()
        throws EFapsException
    {
        return getConfig().getProperty("DateFormat", "dd/MM/yyyy");
    }

    @Override
    public CharSequence getHtmlSnipplet()
        throws EFapsException
    {
        CharSequence ret;
        if (isCached()) {
            ret = getFromCache();
        } else {
            final DateTime start = new DateTime().withTimeAtStartOfDay().minusDays(getDays());
            final DateTime end = new DateTime().withTimeAtStartOfDay().plusDays(1);

            DateTime current = start;
            final List<Map<String, Object>> values = new ArrayList<>();

            for (final CurrencyInst currencyInst : CurrencyInst.getAvailable()) {
                if (!currencyInst.getInstance().equals(Currency.getBaseCurrency())) {
                    while (current.isBefore(end)) {
                        final RateInfo rateInfo = new Currency().evaluateRateInfo(new Parameter(), current,
                                        currencyInst.getInstance());
                        final Map<String, Object> map  = new HashMap<>();
                        map.put("date", current.toString(getDateFormat()));
                        map.put("currency", currencyInst);
                        map.put("value", rateInfo.getRateUI());
                        map.put("valueFrmt", rateInfo.getRateUIFrmt());
                        current = current.plusDays(1);
                        values.add(map);
                    }
                }
                current = start;
            }

            int x = 0;
            final Map<String, Integer> xmap = new LinkedHashMap<>();
            final LineChart chart = new LineChart().setWidth(getWidth()).setHeight(getHeight());
            final String title = getTitle();
            if (title != null && !title.isEmpty()) {
                chart.setTitle(title);
            }
            chart.setOrientation(Orientation.VERTICAL_CHART_LEGEND);

            final Map<String, Map<String, Serie<Data>>> seriesMap = new HashMap<>();
            final Axis xAxis = new Axis().setName("x");
            chart.addAxis(xAxis);
            for (final Map<String, Object> map : values) {

                if (!xmap.containsKey(map.get("date"))) {
                    xmap.put((String) map.get("date"), x++);
                }
                final Map<String, Serie<Data>> series;
                final CurrencyInst currInst =  (CurrencyInst) map.get("currency");
                if (seriesMap.containsKey(currInst.getISOCode())) {
                    series = seriesMap.get(currInst.getISOCode());
                } else {
                    series = new HashMap<>();
                    final Serie<Data> serie = new Serie<Data>();
                    final DateTime validFrom = currInst.getLatestValidFrom();
                    serie.setName(currInst.getName() + " " + (validFrom == null
                                    ? "" : validFrom.toString(getDateFormat())));
                    series.put(currInst.getISOCode(), serie);
                    chart.addSerie(serie);
                    seriesMap.put(currInst.getISOCode(), series);
                }

                final Data dataTmp = new Data().setSimple(false);
                final Serie<Data> serie = series.get(currInst.getISOCode());
                if (serie != null) {
                    serie.addData(dataTmp);
                    final BigDecimal y = ((BigDecimal) map.get("value")).abs();
                    dataTmp.setXValue(xmap.get(map.get("date")));
                    dataTmp.setYValue(y);
                    dataTmp.setTooltip(map.get("valueFrmt") + " " + currInst.getName() + " - " + map.get("date"));
                }
            }
            final List<Map<String, Object>> labels = new ArrayList<>();
            for (final Entry<String, Integer> entry : xmap.entrySet()) {
                final Map<String, Object> map = new HashMap<>();
                map.put("value", entry.getValue());
                map.put("text", Util.wrap4String(entry.getKey()));
                labels.add(map);
            }
            xAxis.setLabels(Util.mapCollectionToObjectArray(labels));
            xAxis.addConfig("rotation", 75);
            chart.addAxis(new Axis().setName("y").setVertical(true).addConfig("includeZero", true)
                            .addConfig("fixUpper", "\"major\""));
            chart.addPlot(new Plot().addConfig("type", "Lines").addConfig("markers", true).addConfig("tension", 2));

            ret = chart.getHtmlSnipplet();
            cache(ret);
        }
        return ret;
    }

    @Override
    public boolean isVisible()
        throws EFapsException
    {
        return true;
    }
}
