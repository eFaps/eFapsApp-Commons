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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

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

    private final Map<Long, CurrencyInst> currencies = new HashMap<Long, CurrencyInst>();

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
            final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
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
        CurrencyInst ret;
        if (this.currencies.containsKey(_currencyId)) {
            ret = this.currencies.get(_currencyId);
        } else {
            ret = new CurrencyInst(Instance.get(CIERP.Currency.getType(), _currencyId));
            this.currencies.put(_currencyId, ret);
        }
        return ret;
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
