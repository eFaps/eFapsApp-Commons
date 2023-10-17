/*
 * Copyright 2003 - 2023 The eFaps Team
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIType;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("e0da4d2c-8275-40bd-b561-e8c2a4ca2027")
@EFapsApplication("eFapsApp-Commons")
public class SerialNumbers
{

    private static final Logger LOG = LoggerFactory.getLogger(SerialNumbers.class);

    public static String getNext(final CIType documentType,
                                 final String serial,
                                 final CIType... additionalTypes)
        throws EFapsException
    {
        String[] additionalTypesStr = null;
        if (additionalTypes != null) {
            additionalTypesStr = Arrays.asList(additionalTypes).stream().map(dtype -> dtype.uuid.toString())
                            .toArray(String[]::new);
        }
        return getNext(documentType.uuid.toString(), serial, additionalTypesStr);
    }

    public static String getNext(final String documentType,
                                 final String serial,
                                 final String... additionalTypes)
        throws EFapsException
    {
        final var types = new HashSet<String>();
        types.add(documentType);
        if (additionalTypes != null) {
            types.addAll(Arrays.asList(additionalTypes));
        }
        final var maxNumber = getMaxNumber(serial, types);
        return serial + "-" + getNextNumber(documentType, maxNumber);
    }

    public static String getPlaceholder(final CIType documentType,
                                        final String serial)
        throws EFapsException
    {
        return getPlaceholder(documentType.getType().getName(), serial);
    }


    public static String getPlaceholder(final String documentType,
                                        final String serial)
        throws EFapsException
    {
        return serial + "-" + getNextNumber(documentType, -1);
    }

    public static String getNextNumber(final String documentType,
                                       final Integer maxNumber)
        throws EFapsException
    {
        String typeKey;
        if (UUIDUtil.isUUID(documentType)) {
            typeKey = Type.get(UUID.fromString(documentType)).getName();
        } else {
            typeKey = documentType;
        }
        final var serialProps = ERP.SERIALNUMBERS.get();
        final int length = Integer.valueOf(serialProps.getProperty(typeKey + ".SuffixLength", "6"));
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(length);
        nf.setMaximumIntegerDigits(length);
        nf.setGroupingUsed(false);
        return nf.format(maxNumber + 1);
    }

    public static Integer getMaxNumber(final String serial,
                                       final Collection<String> types)
        throws EFapsException
    {
        final var typeIds = new ArrayList<String>();
        for (final String typeStr : types) {
            final Type type;
            if (UUIDUtil.isUUID(typeStr)) {
                type = Type.get(UUID.fromString(typeStr));
            } else {
                type = Type.get(typeStr);
            }
            typeIds.add(String.valueOf(type.getId()));
        }

        final var length = serial.length() + 2;
        final StringBuilder bldr = new StringBuilder()
                        .append("SELECT cast(regexp_replace(SUBSTRING(name,").append(length)
                        .append("), '\\D', '', 'g') as INTEGER) as MAXNUM\n")
                        .append(" FROM t_erpdoc\n")
                        .append(" WHERE name LIKE '").append(serial).append("-%'")
                        .append(" AND SUBSTRING(name,").append(length).append(") ~ '\\d+'\n")
                        .append(" AND TYPEID IN (").append(typeIds.stream().collect(Collectors.joining(",")))
                        .append(")")
                        .append(" AND COMPANYID = ").append(Context.getThreadContext().getCompany().getId())
                        .append(" ORDER by MAXNUM desc \n")
                        .append(" LIMIT 1");
        return executeStatement(bldr);
    }

    private static Integer executeStatement(final CharSequence cmd)
        throws EFapsException
    {
        Integer maxNumber = 0;

        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            LOG.debug("Getting highest number with {}", cmd);
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(cmd.toString());
                if (rs.next()) {
                    maxNumber = rs.getInt(1);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (final SQLException e) {
            LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
        }
        return maxNumber;
    }

}
