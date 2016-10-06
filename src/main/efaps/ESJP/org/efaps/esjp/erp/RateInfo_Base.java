/*
 * Copyright 2003 - 2015 The eFaps Team
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

import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("4471a1ed-59ff-4805-bf8f-62899d8e8efc")
@EFapsApplication("eFapsApp-Commons")
public abstract class RateInfo_Base
{

    /** The Constant DEFAULTKEY. */
    protected static final String DEFAULTKEY = "sale";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RateInfo.class);

    /**
     * Scale for the BigDecimal values.
     */
    private int scale = 12;

    /**
     * Purchase Rate for calculation use.
     */
    private BigDecimal rate;

    /**
     * Purchase Rate for use in UserInterface. (equals rate if currency is not
     * inverse)
     */
    private BigDecimal rateUI;

    /**
     * Sales Rate for calculation use.
     */
    private BigDecimal saleRate;

    /**
     * Sale Rate for use in UserInterface. (equals saleRate if currency is not
     * inverse)
     */
    private BigDecimal saleRateUI;

    /**
     * Instance of the currency the rate belongs to. The amount will be
     * converted from this currency to the currencyInst.
     */
    private Instance currencyInst;

    /**
     * Instance of the currency the rate is used to convert to. The amount will
     * be converted from currencyInst to the targetCurrencyInst.
     */
    private Instance targetCurrencyInst;

    /**
     * Formatter Object used for this RateInfo.
     */
    private RateFormatter formatter;

    /**
     * @return is the rate value for UI inverted or not
     * @throws EFapsException on error
     */
    public boolean isInvert()
        throws EFapsException
    {
        final boolean ret;
        // if this rate is against the base currency the definition form the DB
        // must be get
        if (getTargetCurrencyInstance().equals(Currency.getBaseCurrency())) {
            ret = getCurrencyInstObj().isInvert();
        } else {
            //TODO must be SystemConfig for every Currency 2 Currency conversion
            ret = false;
        }
        return ret;
    }

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
    public Instance getCurrencyInstance()
    {
        return this.currencyInst;
    }

    /**
     * Setter method for instance variable {@link #instance4Currency}.
     *
     * @param _currencyInst value for instance variable
     *            {@link #instance4Currency}
     */
    public void setCurrencyInstance(final Instance _currencyInst)
    {
        this.currencyInst = _currencyInst;
    }

    /**
     * Getter method for the instance variable {@link #targetCurrencyInst}.
     *
     * @return value of instance variable {@link #targetCurrencyInst}
     * @throws EFapsException on error
     */
    public Instance getTargetCurrencyInstance()
        throws EFapsException
    {
        if (this.targetCurrencyInst == null) {
            this.targetCurrencyInst = Currency.getBaseCurrency();
        }
        return this.targetCurrencyInst;
    }

    /**
     * Setter method for instance variable {@link #targetCurrencyInst}.
     *
     * @param _targetCurrencyInst value for instance variable
     *            {@link #targetCurrencyInst}
     */
    public void setTargetCurrencyInstance(final Instance _targetCurrencyInst)
    {
        this.targetCurrencyInst = _targetCurrencyInst;
    }

    /**
     * @return the CurrencyInst object for the related currency.
     * @throws EFapsException on error
     */
    public CurrencyInst getCurrencyInstObj()
        throws EFapsException
    {
        return CurrencyInst.get(getCurrencyInstance());
    }

    /**
     * @return the CurrencyInst object for the related currency.
     * @throws EFapsException on error
     */
    public CurrencyInst getTargetCurrencyInstObj()
        throws EFapsException
    {
        return CurrencyInst.get(getTargetCurrencyInstance());
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

    /**
     * @return formatter for rate
     * @throws EFapsException on error
     */
    public String getRateFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4Rate().format(getRate());
    }

    /**
     * @return formatter for rateui
     * @throws EFapsException on error
     */
    public String getRateUIFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4RateUI().format(getRateUI());
    }

    /**
     * @return formatter for SaleRate
     * @throws EFapsException on error
     */
    public String getSaleRateFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4SaleRate().format(getSaleRate());
    }

    /**
     * @return formatter for SaleRateUI
     * @throws EFapsException on error
     */
    public String getSaleRateUIFrmt()
        throws EFapsException
    {
        return getFormatter().getFrmt4SaleRateUI().format(getSaleRateUI());
    }

    /**
     * @return object for rate
     * @throws EFapsException on error
     */
    public Object[] getRateObject()
        throws EFapsException
    {
        final boolean invert = getCurrencyInstObj().isInvert();
        return new Object[] { invert ? BigDecimal.ONE : getRateUI(), invert ? getRateUI() : BigDecimal.ONE };
    }

    /**
     * @return object for salerate
     * @throws EFapsException on error
     */
    public Object[] getSaleRateObject()
        throws EFapsException
    {
        final boolean invert = getCurrencyInstObj().isInvert();
        return new Object[] { invert ? BigDecimal.ONE : getSaleRateUI(), invert ? getSaleRateUI() : BigDecimal.ONE };
    }

    /**
     * Reverse.
     *
     * @return the rate info
     * @throws EFapsException on error
     */
    public RateInfo reverse()
        throws EFapsException
    {
        final RateInfo ret = new RateInfo();
        ret.setRate(getRateUI());
        ret.setRateUI(getRate());
        ret.setSaleRate(getSaleRateUI());
        ret.setSaleRateUI(getSaleRate());
        ret.setCurrencyInstance(getTargetCurrencyInstance());
        ret.setTargetCurrencyInstance(getCurrencyInstance());
        return ret;
    }

    /**
     * @return RateInfo with all values set to BigDecimal.ONE
     * @throws EFapsException on error
     */
    protected static RateInfo getDummyRateInfo()
        throws EFapsException
    {
        final RateInfo ret = new RateInfo();
        ret.setRate(BigDecimal.ONE);
        ret.setRateUI(BigDecimal.ONE);
        ret.setSaleRate(BigDecimal.ONE);
        ret.setSaleRateUI(BigDecimal.ONE);
        ret.setCurrencyInstance(Currency.getBaseCurrency());
        ret.setTargetCurrencyInstance(Currency.getBaseCurrency());
        return ret;
    }

    /**
     * Gets the rate info.
     *
     * @param _rateObj the rate obj
     * @return the rate info
     * @throws EFapsException the e faps exception
     */
    protected static RateInfo getRateInfo(final Object[] _rateObj)
        throws EFapsException
    {
        final RateInfo ret = new RateInfo();
        ret.setRate((BigDecimal) _rateObj[0]);
        ret.setRateUI((BigDecimal) _rateObj[1]);
        ret.setSaleRate((BigDecimal) _rateObj[0]);
        ret.setSaleRateUI((BigDecimal) _rateObj[1]);
        ret.setCurrencyInstance(CurrencyInst.get(_rateObj[2]).getInstance());
        ret.setTargetCurrencyInstance(CurrencyInst.get(_rateObj[3]).getInstance());
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo rateinfo
     * @param _key key for the properties
     * @return rate value
     * @throws EFapsException on error
     */
    protected static BigDecimal getRate(final Parameter _parameter,
                                        final RateInfo _rateInfo,
                                        final String _key)
        throws EFapsException
    {
        final BigDecimal ret;
        final Properties props =  ERP.RATEINFO.get();
        final String rate = props.getProperty(_key, RateInfo.DEFAULTKEY);
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
    protected static String getRateFrmt(final Parameter _parameter,
                                        final RateInfo _rateInfo,
                                        final String _key)
        throws EFapsException
    {
        final String ret;
        final Properties props =  ERP.RATEINFO.get();
        final String rate = props.getProperty(_key, RateInfo.DEFAULTKEY);
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
     * @param _key key for the properties
     * @return rate value for UserInterface
     * @throws EFapsException on error
     */
    protected static BigDecimal getRateUI(final Parameter _parameter,
                                          final RateInfo _rateInfo,
                                          final String _key)
        throws EFapsException
    {
        final BigDecimal ret;
        final Properties props =  ERP.RATEINFO.get();
        final String rate = props.getProperty(_key, RateInfo.DEFAULTKEY);
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
     * @param _key key for the properties
     * @return fromatted rate string for UserInterface
     * @throws EFapsException on error
     */
    protected static String getRateUIFrmt(final Parameter _parameter,
                                          final RateInfo _rateInfo,
                                          final String _key)
        throws EFapsException
    {
        final String ret;
        final Properties props =  ERP.RATEINFO.get();
        final String rate = props.getProperty(_key, RateInfo.DEFAULTKEY);
        if (rate.equalsIgnoreCase("sale")) {
            ret = _rateInfo.getSaleRateUIFrmt();
        } else {
            ret = _rateInfo.getRateUIFrmt();
        }
        return ret;
    }

    /**
     * Get the name for the document on creation.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rateInfo rateinfo
     * @param _key key for the properties
     * @return fromatted rate string for UserInterface
     * @throws EFapsException on error
     */
    protected static Object[] getRateObject(final Parameter _parameter,
                                            final RateInfo _rateInfo,
                                            final String _key)
        throws EFapsException
    {
        final Object[] ret;
        final Properties props =  ERP.RATEINFO.get();
        final String rate = props.getProperty(_key, RateInfo.DEFAULTKEY);
        if (rate.equalsIgnoreCase("sale")) {
            ret = _rateInfo.getSaleRateObject();
        } else {
            ret = _rateInfo.getRateObject();
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
