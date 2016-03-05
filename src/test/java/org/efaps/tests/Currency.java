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
package org.efaps.tests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Test example to see the different results on currencty conversions.
 *
 * @author The eFaps Team
 */
public final class Currency
{

    /**
     * Instantiates a new currency.
     */
    private Currency()
    {
    }

    /**
     * The main method.
     *
     * @param _args the arguments
     */
    public static void main(final String... _args)
    {
        try {
            final String dollarStr = "325";
            final String rateStr = "3.413";

            final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
            format.setParseBigDecimal(true);
            final BigDecimal dollar = (BigDecimal) format.parse(dollarStr);

            // multiply
            final DecimalFormat rateFormat = (DecimalFormat) NumberFormat.getInstance();
            rateFormat.setParseBigDecimal(true);
            final BigDecimal rate = (BigDecimal) rateFormat.parse(rateStr);
            final BigDecimal result1 = dollar.multiply(rate);
            System.out.println(result1 + " -> " + result1.setScale(2, RoundingMode.HALF_UP));

            // divide simple
            final BigDecimal result2 = dollar.setScale(8, RoundingMode.HALF_UP).divide(BigDecimal.ONE.setScale(8)
                            .divide(rate, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
            System.out.println(result2 + " -> " + result2.setScale(2, RoundingMode.HALF_UP));

            // rateobject --> unwanted result
            final Object[] rateObj = new Object[] { BigDecimal.ONE, rate };
            final BigDecimal rateAmount = ((BigDecimal) format.parse(dollarStr)).setScale(6, RoundingMode.HALF_UP);
            final BigDecimal rate2 = ((BigDecimal) rateObj[0]).divide((BigDecimal) rateObj[1], 12,
                            RoundingMode.HALF_UP);

            final BigDecimal amount = rateAmount.divide(rate2, 12, RoundingMode.HALF_UP);
            System.out.println(amount + " -> " + amount.setScale(2, RoundingMode.HALF_UP));

            // divide simple different scale
            final BigDecimal result3 = dollar.setScale(8, RoundingMode.HALF_UP).divide(BigDecimal.ONE.setScale(12)
                            .divide(rate, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
            System.out.println(result3 + " -> " + result3.setScale(2, RoundingMode.HALF_UP));

            // rateobject corrected
            final Object[] rateObj2 = new Object[] { BigDecimal.ONE, rate };
            final BigDecimal rateAmount2 = ((BigDecimal) format.parse(dollarStr)).setScale(8, RoundingMode.HALF_UP);
            final BigDecimal rate3 = ((BigDecimal) rateObj2[0]).setScale(12, RoundingMode.HALF_UP).divide(
                            (BigDecimal) rateObj2[1], RoundingMode.HALF_UP);

            final BigDecimal amount2 = rateAmount2.divide(rate3, RoundingMode.HALF_UP);
            System.out.println(amount2 + " -> " + amount2.setScale(2, RoundingMode.HALF_UP));

        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
