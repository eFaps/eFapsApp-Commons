/*
 * Copyright 2003 - 2009 The eFaps Team
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.CachedMultiPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.ui.wicket.util.DateUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("feb2265d-bc1d-4c71-9f38-aa84d6a1c657")
@EFapsRevision("$Rev$")
public abstract class Currency_Base
{
    /**
     * CacheKey for ExchangeRates.
     */
    public static String CACHEKEY4RATE = Currency.class.getName()+ ".CacheKey4Rate";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Currency.class);


    /**
     * Key used to access a Request map.
     */
    private static final String REQUEST_KEYRATE = Currency_Base.class + ".RequestKey4RateFieldValue";


    public Return getValidUntilUI(final Parameter _parameter)
        throws EFapsException
    {
        final FieldValue fValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final DateTime value;
        if (fValue.getTargetMode().equals(TargetMode.CREATE)) {
            value = new DateTime().plusYears(10);
        } else {
            value = (DateTime) fValue.getValue();
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, value);
        return ret;
    }

    public Return trigger4Insert(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
        final Instance rateInstance = _parameter.getInstance();
        final Map<String, Object[]> name2Value = new HashMap<String, Object[]>();
        for (final Entry<?, ?> entry : values.entrySet()) {
            final Attribute attr = (Attribute) entry.getKey();
            name2Value.put(attr.getName(), (Object[]) entry.getValue());
        }
        final Object curIdObj = name2Value.get(CIERP.CurrencyRateAbstract.CurrencyLink.name)[0];
        final Long curId = curIdObj instanceof Long ? (Long) curIdObj : Long.parseLong((String) curIdObj);
        final Object validFromObj = name2Value.get(CIERP.CurrencyRateAbstract.ValidFrom.name)[0];
        final DateTime validFrom = validFromObj instanceof DateTime
                        ? (DateTime) validFromObj : new DateTime(validFromObj);
        final Object validUntilObj = name2Value.get(CIERP.CurrencyRateAbstract.ValidUntil.name)[0];
        final DateTime validUntil = validUntilObj instanceof DateTime
                        ? (DateTime) validUntilObj : new DateTime(validUntilObj);
        update(_parameter, curId, validFrom, validUntil, rateInstance);
        return new Return();
    }

    protected void update(final Parameter _parameter,
                          final Long _curId,
                          final DateTime _validFrom,
                          final DateTime _validUntil,
                          final Instance _rateInstance)
        throws EFapsException
    {
        // first correct the validFrom
        List<Instance> lstInst = new ArrayList<Instance>();
        QueryBuilder queryBldr = new QueryBuilder(_rateInstance.getType());
        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, _curId);
        queryBldr.addWhereAttrLessValue(CIERP.CurrencyRateAbstract.ValidFrom, _validFrom);
        queryBldr.addWhereAttrGreaterValue(CIERP.CurrencyRateAbstract.ValidUntil, _validFrom);
        InstanceQuery query = queryBldr.getQuery();
        query.execute();
        lstInst.addAll(query.getValues());

        queryBldr = new QueryBuilder(_rateInstance.getType());
        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, _curId);
        queryBldr.addWhereAttrLessValue(CIERP.CurrencyRateAbstract.ValidFrom, _validFrom);
        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.ValidUntil, _validFrom);
        query = queryBldr.getQuery();
        query.execute();
        lstInst.addAll(query.getValues());
        for (final Instance inst : lstInst) {
            if (!_rateInstance.equals(inst)) {
                final Update update = new Update(inst);
                update.add(CIERP.CurrencyRateAbstract.ValidUntil, _validFrom.minusSeconds(1));
                update.executeWithoutTrigger();
            }
        }

        // correct the ValidUntil
        lstInst = new ArrayList<Instance>();
        QueryBuilder queryBldr2 = new QueryBuilder(_rateInstance.getType());
        queryBldr2.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, _curId);
        queryBldr2.addWhereAttrLessValue(CIERP.CurrencyRateAbstract.ValidFrom, _validUntil);
        queryBldr2.addWhereAttrGreaterValue(CIERP.CurrencyRateAbstract.ValidUntil, _validUntil);
        InstanceQuery query2 = queryBldr2.getQuery();
        query2.execute();
        lstInst.addAll(query2.getValues());

        queryBldr2 = new QueryBuilder(_rateInstance.getType());
        queryBldr2.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, _curId);
        queryBldr2.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.ValidFrom, _validUntil);
        queryBldr2.addWhereAttrGreaterValue(CIERP.CurrencyRateAbstract.ValidUntil, _validUntil);
        query2 = queryBldr2.getQuery();
        query2.execute();
        lstInst.addAll(query2.getValues());
        for (final Instance inst : lstInst) {
            if (!_rateInstance.equals(inst)) {
                final Update update = new Update(inst);
                update.add(CIERP.CurrencyRateAbstract.ValidFrom, _validUntil.plusDays(1));
                update.executeWithoutTrigger();
            }
        }
    }

    protected Long getCurId(final Instance _instance)
        throws EFapsException
    {
        final PrintQuery print = new PrintQuery(_instance);
        print.addAttribute(CIERP.CurrencyRateAbstract.CurrencyLink);
        print.execute();
        return print.<Long> getAttribute(CIERP.CurrencyRateAbstract.CurrencyLink);
    }

    /**
     * Method is used as attribute RATE_VALUE event.
     *
     * @param _parameter    Parameter as passed by the efasp API
     * @return value for rate
     * @throws EFapsException
     */
    @SuppressWarnings("unchecked")
    public Return getRateValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        if (_parameter.get(ParameterValues.ACCESSMODE) != null
                        && _parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)) {
            final Instance instance = _parameter.getInstance();
            if (instance != null && instance.getType().isKindOf(CIERP.Currency.getType())) {
                final CurrencyInst currencyInst = getCurrencyInst(instance.getId());
                if (currencyInst.isInvert()) {
                    ret.put(ReturnValues.TRUE, true);
                }
            }
        } else {
            // to prevent that the values are inverted more than one in a request,
            // the fieldvalues must be stored
            Set<FieldValue> fieldValues;
            if (Context.getThreadContext().containsRequestAttribute(Currency_Base.REQUEST_KEYRATE)) {
                fieldValues = (Set<FieldValue>) Context.getThreadContext().getRequestAttribute(
                                Currency_Base.REQUEST_KEYRATE);
            } else {
                fieldValues = new HashSet<FieldValue>();
                Context.getThreadContext().setRequestAttribute(Currency_Base.REQUEST_KEYRATE, fieldValues);
            }
            final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
            if (fieldValues.contains(fieldValue)) {
                ret.put(ReturnValues.VALUES, fieldValue.getValue());
            } else {
                final Object value = fieldValue.getValue();
                if (value instanceof Object[]) {
                    final Object[] values = (Object[]) value;
                    final CurrencyInst currencyInst = getCurrencyInst((Long) values[2]);
                    if (currencyInst.isInvert()) {
                        final Object enomTmp = values[0];
                        values[0] = values[1];
                        values[1] = enomTmp;
                        ret.put(ReturnValues.TRUE, true);
                    }
                    ret.put(ReturnValues.VALUES, values);
                }
                fieldValues.add(fieldValue);
            }
        }
        return ret;
    }

    /**
     * Evaluate a rate object from the database.
     * @param _values           a rate value from the eFaps Database
     * @param _considerInverse  must the inverse of the currency be considered
     * @return BigDecimal
     * @throws EFapsException on error
     */
    public BigDecimal evalRate(final Object[] _values,
                               final boolean _considerInverse)
        throws EFapsException
    {
        if (_considerInverse) {
            final CurrencyInst currencyInst = getCurrencyInst((Long) _values[2]);
            if (currencyInst.isInvert()) {
                final Object enomTmp = _values[0];
                _values[0] = _values[1];
                _values[1] = enomTmp;
            }
        }
        BigDecimal numerator;
        if (_values[0] instanceof BigDecimal) {
            numerator = (BigDecimal) _values[0];
        } else {
            numerator = DecimalType.parseLocalized(_values[0].toString());
        }
        BigDecimal denominator;
        if (_values[1] instanceof BigDecimal) {
            denominator = (BigDecimal) _values[1];
        } else {
            denominator = DecimalType.parseLocalized(_values[1].toString());
        }
        return numerator.divide(denominator,
                           numerator.scale() > denominator.scale() ? numerator.scale() : denominator.scale(),
                           BigDecimal.ROUND_UP);
    }

    /**
     * @param _currencyId id for the currency instance
     * @return CurrencyInst
     */
    protected CurrencyInst getCurrencyInst(final long _currencyId)
    {
        return new CurrencyInst(Instance.get(CIERP.Currency.getType(), _currencyId));
    }

    /**
     * @param _parameter Parameter as passed from the eFaps API
     * @return value for the targetcurrency
     * @throws EFapsException
     */
    public Return getTargetCurrencyLinkUI(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        //Sales-Configuration
        final SystemConfiguration config = SystemConfiguration.get(
                        UUID.fromString("c9a1cbc3-fd35-4463-80d2-412422a3802f"));
        if (config != null) {
            final Instance linkInst = config.getLink("org.efaps.sales.CurrencyBase");
            ret.put(ReturnValues.VALUES, linkInst.getId());
        } else {
            ret.put(ReturnValues.VALUES, _parameter.getInstance().getId());
        }
        return ret;
    }

    /**
     * @return the base currency for eFaps
     * @throws EFapsException on error
     */
    protected Instance getBaseCurrency()
        throws EFapsException
    {
        // Sales-Configuration
        final SystemConfiguration config = SystemConfiguration.get(
                        UUID.fromString("c9a1cbc3-fd35-4463-80d2-412422a3802f"));
        final Instance ret = config.getLink("org.efaps.sales.CurrencyBase");
        if (ret == null) {
            Currency_Base.LOG.error("There must be an BaseCurrency defined to calculate rates.");
        }
        return ret;
    }

    /**
     * @param _parameter
     * @param _rate
     */
    public RateInfo evaluateRateInfo(final Parameter _parameter,
                                     final Object[] _rate)
        throws EFapsException
    {
        final RateInfo ret = RateInfo.getDummyRateInfo();
        if (_rate.length == 4) {
            ret.setInstance4Currency(Instance.get(CIERP.Currency.getType(), (Long) _rate[2]));
            ret.setRate(evalRate(_rate, false));
            ret.setRateUI(evalRate(_rate, true));
        }
        return ret;
    }


    public RateInfo[] evaluateRateInfos(final Parameter _parameter,
                                        final String _dateStr,
                                        final Instance _currentCurrencyInst,
                                        final Instance _targetCurrencyInst)
        throws EFapsException
    {
        return evaluateRateInfos(_parameter, _dateStr != null && _dateStr.length() > 0
                        ? DateUtil.getDateFromParameter(_dateStr) : new DateTime(), _currentCurrencyInst,
                                        _targetCurrencyInst);
    }

    /**
     * Returns an Array of RateInfo with following content.:<br/>
     * <ul>
     * <li>[0]: RateInfo for <code>_currentCurrencyInst</code> against the Base Currency</li>
     * <li>[1]: RateInfo for <code>_targetCurrencyInst</code> against the Base Currency</li>
     * <li>[2]: RateInfo for <code>_currentCurrencyInst</code> against the <code>_targetCurrencyInst</code></li>
     * </ul>
     * @param _parameter Parameter as passed by the eFaps API
     * @param _date     date the rate must be evaluated for
     * @param _currentCurrencyInst instance of the currency the rate is wanted for
     * @param _targetCurrencyInst instance of the currency the rate is wanted for
     * @return RateInfo
     * @throws EFapsException on error
     */
    public RateInfo[] evaluateRateInfos(final Parameter _parameter,
                                        final DateTime _date,
                                        final Instance _currentCurrencyInst,
                                        final Instance _targetCurrencyInst)
        throws EFapsException
    {

        final Instance baseInst = getBaseCurrency();

        final RateInfo currentRateInfo;
        if (_currentCurrencyInst.equals(baseInst)) {
            currentRateInfo = RateInfo.getDummyRateInfo();
        } else {
            currentRateInfo = evaluateRateInfo(_parameter, _date, _currentCurrencyInst);
        }

        final RateInfo targetRateInfo;
        if (_targetCurrencyInst.equals(baseInst)) {
            targetRateInfo = RateInfo.getDummyRateInfo();
        } else {
            targetRateInfo = evaluateRateInfo(_parameter, _date, _targetCurrencyInst);
        }

        final RateInfo curr2tar;
        if (_targetCurrencyInst.equals(_currentCurrencyInst)) {
            curr2tar = RateInfo.getDummyRateInfo();
            curr2tar.setInstance4Currency(null);
        } else {
            curr2tar = new RateInfo();
            curr2tar.setRate(targetRateInfo.getRate().divide(currentRateInfo.getRate(), BigDecimal.ROUND_HALF_DOWN));
            curr2tar.setRateUI(targetRateInfo.getRateUI().divide(currentRateInfo.getRateUI(),
                            BigDecimal.ROUND_HALF_DOWN));
            curr2tar.setSaleRate(targetRateInfo.getSaleRate().divide(currentRateInfo.getSaleRate(),
                            BigDecimal.ROUND_HALF_DOWN));
            curr2tar.setSaleRateUI(targetRateInfo.getSaleRateUI().divide(currentRateInfo.getSaleRateUI(),
                            BigDecimal.ROUND_HALF_DOWN));
        }
        return new RateInfo[] { currentRateInfo, targetRateInfo, curr2tar };
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _dateStr date the rate must be evaluated for
     * @param _currentCurrencyInst instance of the currency the rate is wanted for
     * @return RateInfo
     * @throws EFapsException on error
     */
    public RateInfo evaluateRateInfo(final Parameter _parameter,
                                     final String _dateStr,
                                     final Instance _currentCurrencyInst)
        throws EFapsException
    {
        return evaluateRateInfo(_parameter, _dateStr != null && _dateStr.length() > 0
                        ? DateUtil.getDateFromParameter(_dateStr) : new DateTime(), _currentCurrencyInst);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _date date the rate must be evaluated for
     * @param _currentCurrencyInst instance of the currency the rate is wanted
     *            for
     * @return RateInfo
     * @throws EFapsException on error
     */
    public RateInfo evaluateRateInfo(final Parameter _parameter,
                                     final DateTime _date,
                                     final Instance _currentCurrencyInst)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(getType4ExchangeRate(_parameter));
        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, _currentCurrencyInst.getId());
        queryBldr.addWhereAttrLessValue(CIERP.CurrencyRateAbstract.ValidFrom, _date.plusSeconds(1));
        queryBldr.addWhereAttrGreaterValue(CIERP.CurrencyRateAbstract.ValidUntil, _date.minusSeconds(1));

        final CachedMultiPrintQuery multi = queryBldr.getCachedPrint(Currency_Base.CACHEKEY4RATE);
        final SelectBuilder sel = SelectBuilder.get().linkto(CIERP.CurrencyRateAbstract.CurrencyLink).instance();
        multi.addSelect(sel);
        multi.addAttribute(CIERP.CurrencyRateAbstract.Rate, CIERP.CurrencyRateAbstract.RateSale);
        multi.execute();
        RateInfo ret = new RateInfo();
        if (multi.next()) {
            ret.setRate(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateClient.Rate), false));
            ret.setRateUI(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateClient.Rate), true));
            ret.setSaleRate(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateClient.RateSale), false));
            ret.setSaleRateUI(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateClient.RateSale), true));
            ret.setInstance4Currency(multi.<Instance>getSelect(sel));
        } else {
            ret = RateInfo.getDummyRateInfo();
        }
        return ret;
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return RateInfo type for the exchange rate
     * @throws EFapsException on error
     */
    protected Type getType4ExchangeRate(final Parameter _parameter)
        throws EFapsException
    {
        return CIERP.CurrencyRateClient.getType();
    }

    /**
     * Extension of the standard DropDown Field mechanism to select a default currency.
     * @param _parameter    Parameter as passed by the eFaps APi
     * @return  Return containing html snippet
     * @throws EFapsException on error
     */
    public Return getCurrencyDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Field field = new Field()
        {

            @Override
            protected DropDownPosition getDropDownPosition(final Parameter _parameter,
                                                           final Object _value,
                                                           final Object _option)
                throws EFapsException
            {
                final DropDownPosition position = super.getDropDownPosition(_parameter, _value, _option);
                // Sales-Configuration
                final SystemConfiguration config = SystemConfiguration.get(UUID
                                .fromString("c9a1cbc3-fd35-4463-80d2-412422a3802f"));
                if (config != null) {
                    final Instance inst = config.getLink("Currency4ProductPrice");
                    if (inst.isValid()) {
                        // check if the value is long, and assume that it is the id
                        if (position.getValue() instanceof Long) {
                            position.setSelected(inst.getId() == (Long) position.getValue());
                        } else {
                            position.setSelected(inst.getOid().equals(String.valueOf(position.getValue())));
                        }
                    }
                }
                return position;
            }
        };
        return field.dropDownFieldValue(_parameter);
    }
}
