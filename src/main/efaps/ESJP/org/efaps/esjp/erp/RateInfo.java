/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.esjp.erp.util.ERPSettings;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class is used as POJO and therefore is not thought to be used
 * with standard "_Base" approach.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("852cba11-ab22-4024-acc0-580f777c6750")
@EFapsRevision("$Rev$")
public class RateInfo
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RateInfo.class);

    /**
     * Scale for the BigDecimal values.
     */
    private int scale = 12;

    /**
     * Buy Rate for calculation use.
     */
    private BigDecimal rate;

    /**
     * Buy Rate for use in UserInterface.
     * (equals rate if currency is not inverse)
     */
    private BigDecimal rateUI;

    /**
     * Sales Rate for calculation use.
     */
    private BigDecimal saleRate;

    /**
     * Sale Rate for use in UserInterface.
     * (equals saleRate if currency is not inverse)
     */
    private BigDecimal saleRateUI;

    /**
     * Instance of the currency the rate belongs to.
     */
    private Instance instance4Currency;

    /**
     * Formatter Object used for this RateInfo
     */
    private RateFormatter formatter;

    /**
     * CurrencyInst of this Rate.
     */
    private CurrencyInst currencyInst;

    /**
     * Setter method for instance variable {@link #rate}.
     *
     * @param _rate value for instance variable {@link #rate}
     */
    public void setRate(final BigDecimal _rate)
    {
        this.rate = _rate;
    }

    /**
     * Setter method for instance variable {@link #rateUI}.
     *
     * @param _rate value for instance variable {@link #rateUI}
     */
    public void setRateUI(final BigDecimal _rate)
    {
        this.rateUI = _rate;
    }

    /**
     * Getter method for the instance variable {@link #rate}.
     *
     * @return value of instance variable {@link #rate}
     */
    public BigDecimal getRate()
    {
        return this.rate.setScale(getScale(), BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * Getter method for the instance variable {@link #rateUI}.
     *
     * @return value of instance variable {@link #rateUI}
     */
    public BigDecimal getRateUI()
    {
        return this.rateUI.setScale(getScale(), BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * Getter method for the instance variable {@link #saleRate}.
     *
     * @return value of instance variable {@link #saleRate}
     */
    public BigDecimal getSaleRate()
    {
        return this.saleRate.setScale(getScale(), BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * Setter method for instance variable {@link #saleRate}.
     *
     * @param _saleRate value for instance variable {@link #saleRate}
     */
    public void setSaleRate(final BigDecimal _saleRate)
    {
        this.saleRate = _saleRate;
    }

    /**
     * Getter method for the instance variable {@link #saleRateUI}.
     *
     * @return value of instance variable {@link #saleRateUI}
     */
    public BigDecimal getSaleRateUI()
    {
        return this.saleRateUI.setScale(getScale(), BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * Setter method for instance variable {@link #saleRateUI}.
     *
     * @param _saleRateUI value for instance variable {@link #saleRateUI}
     */
    public void setSaleRateUI(final BigDecimal _saleRateUI)
    {
        this.saleRateUI = _saleRateUI;
    }

    /**
     * Getter method for the instance variable {@link #scale}.
     *
     * @return value of instance variable {@link #scale}
     */
    public int getScale()
    {
        return this.scale;
    }

    /**
     * Setter method for instance variable {@link #scale}.
     *
     * @param _scale value for instance variable {@link #scale}
     */
    public void setScale(final int _scale)
    {
        this.scale = _scale;
    }

    /**
     * Getter method for the instance variable {@link #currencyInstance}.
     *
     * @return value of instance variable {@link #currencyInstance}
     */
    public Instance getInstance4Currency()
    {
        return this.instance4Currency;
    }

    /**
     * Setter method for instance variable {@link #instance4Currency}.
     *
     * @param _currencyInstance value for instance variable {@link #instance4Currency}
     */
    public void setInstance4Currency(final Instance _instance4Currency)
    {
        this.instance4Currency = _instance4Currency;
    }

    /**
     * @return the CurrencyInst object for the related currency.
     */
    public CurrencyInst getCurrencyInst()
    {
        if (this.currencyInst == null) {
            this.currencyInst = new CurrencyInst(getInstance4Currency());
        }
        return this.currencyInst ;
    }

    /**
     * Getter method for the instance variable {@link #formatter}.
     *
     * @return value of instance variable {@link #formatter}
     */
    public RateFormatter getFormatter()
    {
        if (this.formatter == null) {
            this.formatter = new RateFormatter();
        }
        return this.formatter;
    }

    /**
     * Setter method for instance variable {@link #formatter}.
     *
     * @param _formatter value for instance variable {@link #formatter}
     */
    public void setFormatter(final RateFormatter _formatter)
    {
        this.formatter = _formatter;
    }

    public String getRateFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4Rate().format(getRate());
    }

    public String getRateUIFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4RateUI().format(getRateUI());
    }

    public String getSaleRateFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4SaleRate().format(getSaleRate());
    }

    public String getSaleRateUIFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4SaleRateUI().format(getSaleRateUI());
    }

    public Object[] getRateObject()
        throws EFapsException
    {
        final boolean invert = getCurrencyInst().isInvert();

        return new Object[] { invert ? BigDecimal.ONE : getRateUI(), invert ? getRateUI() : BigDecimal.ONE };
    }

    public Object[] getSaleRateObject()
        throws EFapsException
    {
        final boolean invert = getCurrencyInst().isInvert();

        return new Object[] { invert ? BigDecimal.ONE : getSaleRateUI(), invert ? getSaleRateUI() : BigDecimal.ONE };
    }

    /**
     * @return RateInfo with all values set to BigDecimal.ONE
     * @throws EFapsException on error
     */
    public static RateInfo getDummyRateInfo()
        throws EFapsException
    {
        final RateInfo ret = new RateInfo();
        ret.setRate(BigDecimal.ONE);
        ret.setRateUI(BigDecimal.ONE);
        ret.setSaleRate(BigDecimal.ONE);
        ret.setSaleRateUI(BigDecimal.ONE);
        ret.setInstance4Currency(new Currency().getBaseCurrency());
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo  rateinfo
     * @param _key       key for the properties
     * @return rate value
     * @throws EFapsException on error
     */
    public static BigDecimal getRate(final Parameter _parameter,
                                     final RateInfo _rateInfo,
                                     final String _key)
        throws EFapsException
    {
        BigDecimal ret;
        final Properties props = ERP.getSysConfig()
                        .getAttributeValueAsProperties(ERPSettings.RATEINFO, true);
        final String rate = props.getProperty(_key, "buy");
        if (rate.equalsIgnoreCase("sale")) {
            ret = _rateInfo.getSaleRate();
        } else {
            ret = _rateInfo.getRate();
        }
        return ret;
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo rateinfo
     * @param _key key for the properties
     * @return formated rate
     * @throws EFapsException on error
     */
    public static String getRateFrmt(final Parameter _parameter,
                                     final RateInfo _rateInfo,
                                     final String _key)
        throws EFapsException
    {
        String ret;
        final Properties props = ERP.getSysConfig()
                        .getAttributeValueAsProperties(ERPSettings.RATEINFO, true);
        final String rate = props.getProperty(_key, "buy");
        if (rate.equalsIgnoreCase("sale")) {
            ret = _rateInfo.getSaleRateFrmt();
        } else {
            ret = _rateInfo.getRateFrmt();
        }
        return ret;
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo rateinfo
     * @return rate value for UserInterface
     * @throws EFapsException on error
     */
    public static BigDecimal getRateUI(final Parameter _parameter,
                                       final RateInfo _rateInfo,
                                       final String _key)
        throws EFapsException
    {
        BigDecimal ret;
        final Properties props = ERP.getSysConfig()
                        .getAttributeValueAsProperties(ERPSettings.RATEINFO, true);
        final String rate = props.getProperty(_key, "buy");
        if (rate.equalsIgnoreCase("sale")) {
            ret = _rateInfo.getSaleRateUI();
        } else {
            ret = _rateInfo.getRateUI();
        }
        return ret;
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo rateinfo
     * @return fromatted rate string for UserInterface
     * @throws EFapsException on error
     */
    public static String getRateUIFrmt(final Parameter _parameter,
                                       final RateInfo _rateInfo,
                                       final String _key)
        throws EFapsException
    {
        String ret;
        final Properties props = ERP.getSysConfig()
                        .getAttributeValueAsProperties(ERPSettings.RATEINFO, true);
        final String rate = props.getProperty(_key, "buy");
        if (rate.equalsIgnoreCase("sale")) {
            ret = _rateInfo.getSaleRateUIFrmt();
        } else {
            ret = _rateInfo.getRateUIFrmt();
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
