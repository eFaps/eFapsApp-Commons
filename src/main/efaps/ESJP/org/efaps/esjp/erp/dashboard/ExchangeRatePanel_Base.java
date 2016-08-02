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

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.esjp.common.dashboard.AbstractDashboardPanel;
import org.efaps.esjp.erp.Currency;
import org.efaps.esjp.erp.CurrencyInst;
import org.efaps.esjp.erp.RateInfo;
import org.efaps.esjp.ui.html.Table;
import org.efaps.util.EFapsBaseException;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * The Class ExchangeRatePanel_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("5ff2db82-b28b-43fa-acea-a8ee4446e019")
@EFapsApplication("eFapsApp-Commons")
public abstract class ExchangeRatePanel_Base
    extends AbstractDashboardPanel
    implements IEsjpSnipplet
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new exchange rate panel.
     */
    public ExchangeRatePanel_Base()
    {
        super();
    }

    /**
     * Instantiates a new exchange rate panel.
     *
     * @param _config the config
     */
    public ExchangeRatePanel_Base(final String _config)
    {
        super(_config);
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
        throws EFapsBaseException
    {
        final StringBuilder html = new StringBuilder()
                    .append("<div style=\"height:")
                    .append(getHeight()).append("px;width:")
                    .append(getWidth()).append("px\">");
        final Table table = new Table().setStyle("margin: auto;padding: 20px;");
        boolean first = true;
        for (final CurrencyInst currencyInst : CurrencyInst.getAvailable()) {
            if (!currencyInst.getInstance().equals(Currency.getBaseCurrency())) {
                if (first) {
                    first = false;
                } else {
                    table.addRow().addColumn("&nbsp;").addRow().addColumn("&nbsp;");
                }
                final RateInfo rateInfo = new Currency().evaluateRateInfo(new Parameter(), new DateTime(), currencyInst
                                .getInstance());
                table.addRow()
                    .addHeaderColumn(currencyInst.getName())
                    .addRow()
                        .addColumn(DBProperties.getProperty("ERP_CurrencyRateClient/RateSale.Label"))
                            .getCurrentColumn().setStyle("font-style: italic;").getCurrentTable()
                        .addColumn(rateInfo.getRateUIFrmt())
                    .addRow()
                        .addColumn(DBProperties.getProperty("ERP_CurrencyRateClient/Rate.Label"))
                            .getCurrentColumn().setStyle("font-style: italic;").getCurrentTable()
                        .addColumn(rateInfo.getSaleRateUIFrmt())
                    .addRow()
                        .addColumn(currencyInst.getLatestValidFrom().toString(getDateFormat()))
                        .getCurrentColumn().setStyle("text-align: center;");
            }
        }
        return html.append(table.toHtml()).append("</div>");
    }

    @Override
    public boolean isVisible()
        throws EFapsBaseException
    {
        return true;
    }
}
