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
package org.efaps.esjp.erp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.CachedMultiPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.Update;
import org.efaps.eql.EQL;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.datetime.JodaTimeUtils;
import org.efaps.esjp.common.uiform.Create;
import org.efaps.esjp.common.uiform.Edit;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uiform.Field_Base.DropDownPosition;
import org.efaps.esjp.db.InstanceUtils;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!.
 *
 * @author The eFaps Team
 */
@EFapsUUID("feb2265d-bc1d-4c71-9f38-aa84d6a1c657")
@EFapsApplication("eFapsApp-Commons")
public abstract class Currency_Base
    extends AbstractCommon
{
    /**
     * CacheKey for ExchangeRates.
     */
    public static final String CACHEKEY4RATE = Currency.class.getName() + ".CacheKey4Rate";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Currency.class);


    /**
     * Key used to access a Request map.
     */
    private static final String REQUEST_KEYRATE = Currency_Base.class + ".RequestKey4RateFieldValue";


    /**
     * Gets the valid until ui.
     *
     * @param _parameter the _parameter
     * @return the valid until ui
     * @throws EFapsException the e faps exception
     */
    public Return getValidUntilUI(final Parameter _parameter)
        throws EFapsException
    {
        final IUIValue fValue = (IUIValue) _parameter.get(ParameterValues.UIOBJECT);
        Object value;
        if (TargetMode.CREATE.equals(_parameter.get(ParameterValues.ACCESSMODE))) {
            value = new DateTime().plusYears(10);
        } else {
            value = fValue.getObject();
        }
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, value);
        return ret;
    }

    public Return create(final Parameter parameter)
        throws EFapsException
    {
        // if app
        if (parameter.get(ParameterValues.PAYLOAD) != null) {
            @SuppressWarnings("unchecked")
            final var values = (Map<String, ?>) parameter.get(ParameterValues.PAYLOAD);
            final var command = (AbstractCommand) parameter.get(ParameterValues.UIOBJECT);
            final var parentInstance = parameter.getInstance();
            final var createType = command.getTargetCreateType();

            final var inverted  = CurrencyInst.get(parentInstance).isInvert();

            final var rateUiValue = values.get("rate");
            final var rateSaleUiValue = values.get("rateSale");
            final var rate = new Object[] { inverted ? 1 : rateUiValue, inverted ? rateUiValue : 1 };
            final var rateSale = new Object[] { inverted ? 1 : rateSaleUiValue, inverted ? rateSaleUiValue : 1 };

            EQL.builder().insert(createType)
                .set(CIERP.CurrencyRateAbstract.CurrencyLink, parentInstance.getId())
                .set(CIERP.CurrencyRateAbstract.TargetCurrencyLink,  Currency.getBaseCurrency())
                .set(CIERP.CurrencyRateAbstract.Rate, rate)
                .set(CIERP.CurrencyRateAbstract.RateSale, rateSale)
                .set(CIERP.CurrencyRateAbstract.ValidFrom, values.get("validFrom"))
                .set(CIERP.CurrencyRateAbstract.ValidUntil,  values.get("validUntil"))
                .execute();
            return new Return();
        } else {
            return new Create().execute(parameter);
        }
    }


    public Return update(final Parameter parameter)
        throws EFapsException
    {
        // if app
        if (parameter.get(ParameterValues.PAYLOAD) != null) {
            @SuppressWarnings("unchecked")
            final var values = (Map<String, ?>) parameter.get(ParameterValues.PAYLOAD);
            final var instance = parameter.getInstance();
            final var eval = EQL.builder().print(instance)
                            .attribute(CIERP.CurrencyRateAbstract.CurrencyLink)
                            .evaluate();

            final var currencyId = eval.get(CIERP.CurrencyRateAbstract.CurrencyLink);
            final var inverted = CurrencyInst.get(currencyId).isInvert();

            final var rateUiValue = values.get("rate");
            final var rateSaleUiValue = values.get("rateSale");
            final var rate = new Object[] { inverted ? 1 : rateUiValue, inverted ? rateUiValue : 1 };
            final var rateSale = new Object[] { inverted ? 1 : rateSaleUiValue, inverted ? rateSaleUiValue : 1 };
            EQL.builder().update(instance)
                            .set(CIERP.CurrencyRateAbstract.Rate, rate)
                            .set(CIERP.CurrencyRateAbstract.RateSale, rateSale)
                            .set(CIERP.CurrencyRateAbstract.ValidFrom, values.get("validFrom"))
                            .set(CIERP.CurrencyRateAbstract.ValidUntil, values.get("validUntil"))
                            .execute();
            return new Return();
        } else {
            return new Edit().execute(parameter);
        }
    }

    /**
     * Trigger4 insert.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException the e faps exception
     */
    public Return trigger4Insert(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
        final Instance rateInstance = _parameter.getInstance();
        final Map<String, Object[]> name2Value = new HashMap<>();
        for (final Entry<?, ?> entry : values.entrySet()) {
            final Attribute attr = (Attribute) entry.getKey();
            name2Value.put(attr.getName(), (Object[]) entry.getValue());
        }
        final Object curIdObj = name2Value.get(CIERP.CurrencyRateAbstract.CurrencyLink.name)[0];
        final Long curId = curIdObj instanceof final Long l ? l : Long.parseLong((String) curIdObj);
        final Object validFromObj = name2Value.get(CIERP.CurrencyRateAbstract.ValidFrom.name)[0];
        final DateTime validFrom = validFromObj instanceof final DateTime d
                        ? d : new DateTime(validFromObj);
        final Object validUntilObj = name2Value.get(CIERP.CurrencyRateAbstract.ValidUntil.name)[0];
        final DateTime validUntil = validUntilObj instanceof final DateTime d
                        ? d : new DateTime(validUntilObj);
        update(_parameter, curId, validFrom, validUntil, rateInstance);
        return new Return();
    }

    /**
     * Update.
     *
     * @param _parameter the _parameter
     * @param _curId the _cur id
     * @param _validFrom the _valid from
     * @param _validUntil the _valid until
     * @param _rateInstance the _rate instance
     * @throws EFapsException the e faps exception
     */
    protected void update(final Parameter _parameter,
                          final Long _curId,
                          final DateTime _validFrom,
                          final DateTime _validUntil,
                          final Instance _rateInstance)
        throws EFapsException
    {
        // first correct the validFrom
        List<Instance> lstInst = new ArrayList<>();
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
        lstInst = new ArrayList<>();
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

    /**
     * Gets the cur id.
     *
     * @param _instance the _instance
     * @return the cur id
     * @throws EFapsException the e faps exception
     */
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
     * @param parameter    Parameter as passed by the efasp API
     * @return value for rate on error
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public Return getRateValue(final Parameter parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        // new UI
        if (parameter.get(ParameterValues.OTHERS) != null
                        && parameter.get(ParameterValues.OTHERS) instanceof Object[]) {
            final var values = (Object[]) parameter.get(ParameterValues.OTHERS);
            ret.put(ReturnValues.VALUES, evalRate(values, true));
        } else if (parameter.get(ParameterValues.ACCESSMODE) != null
                        && parameter.get(ParameterValues.ACCESSMODE).equals(TargetMode.CREATE)) {
            final Instance instance = parameter.getInstance();
            final Instance callInstance = parameter.getCallInstance();
            if (InstanceUtils.isKindOf(instance, CIERP.Currency)) {
                final CurrencyInst currencyInst = CurrencyInst.get(instance);
                if (currencyInst.isInvert()) {
                    ret.put(ReturnValues.TRUE, true);
                }
            } else if (InstanceUtils.isKindOf(callInstance, CIERP.Currency)) {
                final CurrencyInst currencyInst = CurrencyInst.get(callInstance);
                if (currencyInst.isInvert()) {
                    ret.put(ReturnValues.TRUE, true);
                }
            } else {
                // in case that a special default value was set
                final IUIValue uiValue =  (IUIValue) parameter.get(ParameterValues.UIOBJECT);
                if (uiValue != null &&  uiValue.getObject() instanceof Object[]) {
                    final Object[] values = (Object[]) uiValue.getObject();
                    if (values.length > 2) {
                        final CurrencyInst currencyInst = CurrencyInst.get((Long) values[2]);
                        if (currencyInst.getInstance().isValid() && currencyInst.isInvert()) {
                            final Object enomTmp = values[0];
                            values[0] = values[1];
                            values[1] = enomTmp;
                            ret.put(ReturnValues.TRUE, true);
                        }
                    }
                    ret.put(ReturnValues.VALUES, values);
                }
            }
        } else {
            // to prevent that the values are inverted more than one in a request,
            // the fieldvalues must be stored
            final Set<IUIValue> fieldValues;
            if (Context.getThreadContext().containsRequestAttribute(Currency_Base.REQUEST_KEYRATE)) {
                fieldValues = (Set<IUIValue>) Context.getThreadContext().getRequestAttribute(
                                Currency_Base.REQUEST_KEYRATE);
            } else {
                fieldValues = new HashSet<>();
                Context.getThreadContext().setRequestAttribute(Currency_Base.REQUEST_KEYRATE, fieldValues);
            }
            final IUIValue fieldValue = (IUIValue) parameter.get(ParameterValues.UIOBJECT);
            if (fieldValues.contains(fieldValue)) {
                ret.put(ReturnValues.VALUES, fieldValue.getObject());
            } else {
                final Object value = fieldValue.getObject();
                if (value instanceof final Object[] values) {
                    if (values[2] != null) {
                        final CurrencyInst currencyInst = CurrencyInst.get((Long) values[2]);
                        if (currencyInst.isInvert()) {
                            final Object enomTmp = values[0];
                            values[0] = values[1];
                            values[1] = enomTmp;
                            ret.put(ReturnValues.TRUE, true);
                        }
                    }
                    ret.put(ReturnValues.VALUES, values);
                }
                fieldValues.add(fieldValue);
            }
        }
        return ret;
    }

    public BigDecimal evalRate(final Object[] _values,
                               final boolean _considerInverse,
                               final Instance _currencyInstance)
        throws EFapsException
    {
        Object[] values;
        if (_considerInverse) {
            values = _values;
            if (_values.length > 2) {
                values[2] = _currencyInstance.getId();
            } else {
                values = ArrayUtils.add(_values, _currencyInstance.getId());
            }
        } else {
            values = _values;
        }
        return evalRate(values, _considerInverse);
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
            final CurrencyInst currencyInst = CurrencyInst.get((Long) _values[2]);
            if (currencyInst.isInvert()) {
                final Object enomTmp = _values[0];
                _values[0] = _values[1];
                _values[1] = enomTmp;
            }
        }
        final BigDecimal numerator;
        if (_values[0] instanceof BigDecimal) {
            numerator = (BigDecimal) _values[0];
        } else {
            numerator = DecimalType.parseLocalized(_values[0].toString());
        }
        final BigDecimal denominator;
        if (_values[1] instanceof BigDecimal) {
            denominator = (BigDecimal) _values[1];
        } else {
            denominator = DecimalType.parseLocalized(_values[1].toString());
        }
        return numerator.divide(denominator,
                           numerator.scale() > denominator.scale() ? numerator.scale() : denominator.scale(),
                           RoundingMode.HALF_UP);
    }

    /**
     * Gets the target currency link ui.
     *
     * @param _parameter Parameter as passed from the eFaps API
     * @return value for the targetcurrency
     * @throws EFapsException on error
     */
    public Return getTargetCurrencyLinkUI(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final Instance inst = ERP.CURRENCYBASE.get();
        if (inst.isValid()) {
            ret.put(ReturnValues.VALUES, inst.getId());
        } else {
            ret.put(ReturnValues.VALUES, _parameter.getInstance().getId());
        }
        return ret;
    }

    /**
     * Evaluate rate info.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _rate     rateObject to be evaluated
     * @return RateInfo
     * @throws EFapsException on error
     */
    public RateInfo evaluateRateInfo(final Parameter _parameter,
                                     final Object[] _rate)
        throws EFapsException
    {
        final RateInfo ret = RateInfo.getDummyRateInfo();
        if (_rate.length == 4) {
            ret.setCurrencyInstance(Instance.get(CIERP.Currency.getType(), (Long) _rate[2]));
            ret.setRate(evalRate(_rate, false));
            ret.setRateUI(evalRate(_rate, true));
            ret.setSaleRate(ret.getRate());
            ret.setSaleRateUI(ret.getRateUI());
        }
        return ret;
    }

    /**
     * Returns an Array of RateInfo with following content.<br/>
     * <ul>
     * <li>[0]: RateInfo for <code>_currentCurrencyInst</code> against the Base Currency</li>
     * <li>[1]: RateInfo for <code>_targetCurrencyInst</code> against the Base Currency</li>
     * <li>[2]: RateInfo for <code>_currentCurrencyInst</code> against the <code>_targetCurrencyInst</code></li>
     * </ul>
     * @param _parameter Parameter as passed by the eFaps API
     * @param _dateStr   date the rate must be evaluated for
     * @param _currentCurrencyInst  instance of the currency the amount is currently in
     * @param _targetCurrencyInst   instance of the currency the amount will be calculated for
     * @return Array of RateInfo
     * @throws EFapsException on error
     */
    public RateInfo[] evaluateRateInfos(final Parameter _parameter,
                                        final String _dateStr,
                                        final Instance _currentCurrencyInst,
                                        final Instance _targetCurrencyInst)
        throws EFapsException
    {
        return evaluateRateInfos(_parameter, StringUtils.isEmpty(_dateStr) ? new DateTime().withTimeAtStartOfDay()
                        : JodaTimeUtils.getDateFromParameter(_dateStr), _currentCurrencyInst,
                                        _targetCurrencyInst);
    }

    /**
     * Returns an Array of RateInfo with following content.<br/>
     * <ul>
     * <li>[0]: RateInfo for <code>_currentCurrencyInst</code> against the Base Currency</li>
     * <li>[1]: RateInfo for <code>_targetCurrencyInst</code> against the Base Currency</li>
     * <li>[2]: RateInfo for <code>_currentCurrencyInst</code> against the <code>_targetCurrencyInst</code></li>
     * </ul>
     * @param _parameter Parameter as passed by the eFaps API
     * @param _date     date the rate must be evaluated for
     * @param _currentCurrencyInst  instance of the currency the amount is currently in
     * @param _targetCurrencyInst   instance of the currency the amount will be calculated for
     * @return Array of RateInfo
     * @throws EFapsException on error
     */
    public RateInfo[] evaluateRateInfos(final Parameter _parameter,
                                        final DateTime _date,
                                        final Instance _currentCurrencyInst,
                                        final Instance _targetCurrencyInst)
        throws EFapsException
    {

        final Instance baseInst = Currency_Base.getBaseCurrency();

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
            curr2tar.setCurrencyInstance(_currentCurrencyInst);
            curr2tar.setTargetCurrencyInstance(_currentCurrencyInst);
        } else {
            // current to base uses divide ==> base to target uses multiply ==< divide must be used
            curr2tar = new RateInfo();
            curr2tar.setCurrencyInstance(_currentCurrencyInst);
            curr2tar.setTargetCurrencyInstance(_targetCurrencyInst);

            curr2tar.setRate(currentRateInfo.getRate().divide(targetRateInfo.getRate(), RoundingMode.HALF_UP));
            curr2tar.setSaleRate(currentRateInfo.getSaleRate().divide(targetRateInfo.getSaleRate(),
                            RoundingMode.HALF_UP));

            if (curr2tar.isInvert()) {
                curr2tar.setRateUI(currentRateInfo.getRateUI().multiply(targetRateInfo.getRateUI()));
                curr2tar.setSaleRateUI(currentRateInfo.getSaleRateUI().multiply(targetRateInfo.getSaleRateUI()));
            } else {
                curr2tar.setRateUI(curr2tar.getRate());
                curr2tar.setSaleRateUI(curr2tar.getSaleRate());
            }
        }
        return new RateInfo[] { currentRateInfo, targetRateInfo, curr2tar };
    }

    /**
     * Evaluate rate info.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _dateStr date the rate must be evaluated for
     * @param _currentCurrencyInst instance of the currency the rate is wanted for
     * @return RateInfo
     * @throws EFapsException on error
     */
    public RateInfo evaluateRateInfo(final Parameter _parameter,
                                     final String dateStr,
                                     final Instance _currentCurrencyInst)
        throws EFapsException
    {
        DateTime date;
        if (dateStr == null) {
            date = new DateTime();
        } else if (dateStr.matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d$")) {
            date = DateTime.parse(dateStr);
        } else {
            final var shortFormat = DateTimeFormat.shortDate();
            date = shortFormat.parseDateTime(dateStr);
        }
        return evaluateRateInfo(_parameter, date, _currentCurrencyInst);
    }

    /**
     * Evaluate rate info.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _date date the rate must be evaluated for
     * @param _currentCurrencyInst instance of the currency the rate is wanted for
     * @return RateInfo
     * @throws EFapsException on error
     */
    public RateInfo evaluateRateInfo(final Parameter _parameter,
                                     final DateTime _date,
                                     final Instance _currentCurrencyInst)
        throws EFapsException
    {
        final DateTime date = _date.toLocalDate().toDateTimeAtStartOfDay();

        final QueryBuilder queryBldr = new QueryBuilder(getType4ExchangeRate(_parameter));
        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, _currentCurrencyInst.getId());
        queryBldr.addWhereAttrLessValue(CIERP.CurrencyRateAbstract.ValidFrom, date.plusSeconds(1));
        queryBldr.addWhereAttrGreaterValue(CIERP.CurrencyRateAbstract.ValidUntil, date.plusSeconds(1));

        final CachedMultiPrintQuery multi = queryBldr.getCachedPrint(Currency_Base.CACHEKEY4RATE);
        final SelectBuilder sel = SelectBuilder.get().linkto(CIERP.CurrencyRateAbstract.CurrencyLink).instance();
        multi.addSelect(sel);
        multi.addAttribute(CIERP.CurrencyRateAbstract.Rate, CIERP.CurrencyRateAbstract.RateSale);
        multi.execute();
        RateInfo ret = new RateInfo();
        if (multi.next()) {
            final Instance currencyInstance = multi.<Instance>getSelect(sel);
            ret.setRate(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateAbstract.Rate), false));
            ret.setRateUI(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateAbstract.Rate), true, currencyInstance));
            ret.setSaleRate(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateAbstract.RateSale), false));
            ret.setSaleRateUI(evalRate(multi.<Object[]>getAttribute(CIERP.CurrencyRateAbstract.RateSale), true, currencyInstance));
            ret.setCurrencyInstance(multi.<Instance>getSelect(sel));
        } else {
            ret = RateInfo.getDummyRateInfo();
        }
        return ret;
    }

    /**
     * Gets the type4 exchange rate.
     *
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
     * Gets the currency from the UserInterface.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _fieldNames the field names
     * @return the currency from ui
     * @throws EFapsException on error
     */
    public Instance getCurrencyFromUI(final Parameter _parameter,
                                      final String... _fieldNames)
        throws EFapsException
    {
        final String[] fieldNames;
        if (ArrayUtils.isEmpty(_fieldNames)) {
            fieldNames = new String[] { "rateCurrencyId" };
        } else {
            fieldNames = _fieldNames;
        }
        Instance ret = null;
        for (final String fieldName : fieldNames) {
            final String strVal = _parameter.getParameterValue(fieldName);
            if (strVal != null) {
                if (strVal.contains(".")) {
                    ret = Instance.get(strVal);
                } else {
                    ret = Instance.get(CIERP.Currency.getType(), strVal);
                }
            }
            if (ret != null && ret.isValid()) {
                break;
            }
        }
        return ret == null ? Currency.getBaseCurrency() : ret;
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
            public DropDownPosition getDropDownPosition(final Parameter _parameter,
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
        return field.getOptionListFieldValue(_parameter);
    }

    /**
     * Currency drop down field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return currencyDropDownFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final List<DropDownPosition> values = new ArrayList<>();
        final Field field = new Field();
        for (final CurrencyInst curInstObj : CurrencyInst.getAvailable()) {
            final DropDownPosition pos = field.getDropDownPosition(_parameter, curInstObj.getInstance().getOid(),
                            curInstObj.getName());
            pos.setSelected(curInstObj.getInstance().equals(Currency.getBaseCurrency()));
            values.add(pos);
        }
        if (containsProperty(_parameter, "EmptyValue")) {
            values.add(0, new DropDownPosition("",
                            DBProperties.getProperty(getProperty(_parameter, "EmptyValue"))));
        }
        Collections.sort(values, new Comparator<DropDownPosition>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(final DropDownPosition _o1,
                               final DropDownPosition _o2)
            {
                return _o1.getOrderValue().compareTo(_o2.getOrderValue());
            }
        });
        final Return ret = new Return();
        ret.put(ReturnValues.VALUES, values);
        return ret;
    }

    /**
     * Gets the base currency.
     *
     * @return the base currency for eFaps
     * @throws EFapsException on error
     */
    protected static Instance getBaseCurrency()
        throws EFapsException
    {
        final Instance ret =  ERP.CURRENCYBASE.get();
        if (ret == null || ret != null && !ret.isValid()) {
            Currency_Base.LOG.error("There must be an BaseCurrency defined to calculate rates.");
        }
        return ret;
    }

    /**
     * Convert to base.
     *
     * @param _parameter the _parameter
     * @param _current the _current
     * @param _rateInfo the _rate info
     * @param _key the _key
     * @return the big decimal
     * @throws EFapsException the e faps exception
     */
    protected static BigDecimal convertToBase(final Parameter _parameter,
                                              final BigDecimal _current,
                                              final RateInfo _rateInfo,
                                              final String _key)
        throws EFapsException
    {
        BigDecimal ret = BigDecimal.ZERO;
        // if invert multiply else divide
        if (_rateInfo.getCurrencyInstObj().isInvert()) {
            final BigDecimal rate = RateInfo.getRateUI(_parameter, _rateInfo, _key);
            ret = _current.multiply(rate);
        } else {
            final BigDecimal rate = RateInfo.getRate(_parameter, _rateInfo, _key);
            ret = _current.setScale(12, RoundingMode.HALF_UP).divide(rate, RoundingMode.HALF_UP);
        }
        return ret;
    }

    /**
     * Convert to currency.
     *
     * @param _parameter the _parameter
     * @param _current the _current
     * @param _rateInfo the _rate info
     * @param _key the _key
     * @param _target instance of the target currency
     * @return the big decimal
     * @throws EFapsException the eFaps exception
     */
    protected static BigDecimal convertToCurrency(final Parameter _parameter,
                                                  final BigDecimal _current,
                                                  final RateInfo _rateInfo,
                                                  final String _key,
                                                  final Instance _target)
        throws EFapsException
    {
        BigDecimal ret = BigDecimal.ZERO;
        if (_target.equals(Currency.getBaseCurrency())) {
            ret = Currency.convertToBase(_parameter, _current, _rateInfo, _key == null ? "Default" : _key);
        } else if (_target.equals(_rateInfo.getTargetCurrencyInstance())) {
            if (_rateInfo.getCurrencyInstObj().isInvert()) {
                final BigDecimal rate = RateInfo.getRateUI(_parameter, _rateInfo, _key == null ? "Default" : _key);
                ret = _current.multiply(rate);
            } else {
                final BigDecimal rate = RateInfo.getRate(_parameter, _rateInfo, _key == null ? "Default" : _key);
                ret = _current.setScale(12, RoundingMode.HALF_UP).divide(rate, RoundingMode.HALF_UP);
            }
        } else if (_target.equals(_rateInfo.getCurrencyInstance())) {
            ret = _current;
        }
        return ret;
    }

    protected static BigDecimal convert(final Parameter _parameter,
                                     final BigDecimal _amount,
                                     final Instance _fromCurrencyInstance,
                                     final Instance _toCurrencyInstance,
                                     final String _rateKey)
        throws EFapsException
    {
        return Currency_Base.convert(_parameter, _amount, _fromCurrencyInstance, _toCurrencyInstance, _rateKey, LocalDate.now());
    }

    /**
     * Convert a given amount from one Currency into another by converting first the amount
     * into a amount in the system base currency and form the base currency into the final currency.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _fromAmount the original amount to be converted
     * @param _fromCurrencyInstance the currency the original amount is in
     * @param _toCurrencyInstance the currency the amount will be converted into
     * @param _rateKey key that defines if Sales rate or buy rate will apply
     * @param _date date the exchange rates will be evaluated for
     * @return converted amount
     * @throws EFapsException on error
     */
    protected static BigDecimal convert(final Parameter _parameter,
                                        final BigDecimal _fromAmount,
                                        final Instance _fromCurrencyInstance,
                                        final Instance _toCurrencyInstance,
                                        final String _rateKey,
                                        final LocalDate _date)
        throws EFapsException
    {
        final BigDecimal ret;
        if (_fromCurrencyInstance.equals(_toCurrencyInstance)) {
            ret = _fromAmount;
        } else {
            final DateTime date = new DateTime()
                            .withYear(_date.getYear())
                            .withMonthOfYear(_date.getMonthValue())
                            .withDayOfMonth(_date.getDayOfMonth())
                            .withTimeAtStartOfDay();

            final RateInfo fromRateInfo = new Currency().evaluateRateInfo(_parameter, date, _fromCurrencyInstance);
            final CurrencyInst fromCurInst = CurrencyInst.get(_fromCurrencyInstance);

            final BigDecimal baseAmount;
            // the standard conversion to base currency is done by dividing by rate, inverted ones are multiplied by RateUI
            if (fromCurInst.isInvert()) {
                final BigDecimal fromRate = RateInfo.getRateUI(_parameter, fromRateInfo, _rateKey);
                baseAmount = _fromAmount.setScale(fromRateInfo.getScale(), RoundingMode.HALF_UP).multiply(fromRate);
            } else {
                final BigDecimal fromRate = RateInfo.getRate(_parameter, fromRateInfo, _rateKey);
                baseAmount = _fromAmount.setScale(fromRateInfo.getScale(), RoundingMode.HALF_UP)
                                .divide(fromRate, RoundingMode.HALF_UP);
            }

            final RateInfo toRateInfo = new Currency().evaluateRateInfo(_parameter, date, _toCurrencyInstance);
            final CurrencyInst toCurInst = CurrencyInst.get(_toCurrencyInstance);
            // the standard conversion from base currency into any other currency is done by multiplying by rate,
            // inverted ones are divided by RateUI
            if (toCurInst.isInvert()) {
                final BigDecimal toRate = RateInfo.getRateUI(_parameter, toRateInfo, _rateKey);
                ret = baseAmount.setScale(fromRateInfo.getScale(), RoundingMode.HALF_UP)
                        .divide(toRate, RoundingMode.HALF_UP);
            } else {
                final BigDecimal toRate = RateInfo.getRate(_parameter, toRateInfo, _rateKey);
                ret = baseAmount.multiply(toRate);
            }
        }
        return ret;
    }


    /**
     * @return Set of available CurrencyInst
     * @throws EFapsException on error
     */
    protected static Collection<Instance> getAvailable()
        throws EFapsException
    {
        final List<Instance> ret = new ArrayList<>();
        for (final CurrencyInst cur : CurrencyInst.getAvailable()) {
            ret.add(cur.getInstance());
        }
        return ret;
    }
}
