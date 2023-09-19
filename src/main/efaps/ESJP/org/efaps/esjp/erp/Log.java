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

import java.time.OffsetDateTime;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIType;
import org.efaps.db.Instance;
import org.efaps.eql.EQL;
import org.efaps.eql.builder.Insert;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.erp.util.ERP.LogLevel;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@EFapsUUID("121ee1a3-3ebb-46b1-9fe6-1d2ea21f995e")
@EFapsApplication("eFapsApp-Commons")
public class Log
{

    private static final Logger LOG = LoggerFactory.getLogger(Log.class);

    private String key;
    private LogLevel level;
    private OffsetDateTime logDateTime;
    private String message;
    private String value;
    private CIType ciType;

    public String getKey()
    {
        return key;
    }

    public Log withKey(final String key)
    {
        this.key = key;
        return this;
    }

    public LogLevel getLevel()
    {
        return level;
    }

    public Log withLevel(final LogLevel level)
    {
        this.level = level;
        return this;
    }

    public Log withLevel(final String levelStr)
    {
        return withLevel(evalLevel(levelStr));
    }

    public OffsetDateTime getLogDateTime()
    {
        return logDateTime;
    }

    public Log withLogDateTime(final OffsetDateTime logDateTime)
    {
        this.logDateTime = logDateTime;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public Log withMessage(final String message)
    {
        this.message = message;
        return this;
    }

    public String getValue()
    {
        return value;
    }

    public Log withValue(final Object value)
    {
        if (value != null) {
            try {
                this.value = getObjectMapper().writeValueAsString(value);
            } catch (final JsonProcessingException e) {
                LOG.error("Catched", e);
            }
        }
        return this;
    }

    public CIType getCIType()
    {
        return ciType;
    }

    public Log withCIType(final CIType ciType)
    {
        this.ciType = ciType;
        return this;
    }

    public Instance register()
        throws EFapsException
    {
        LOG.info("Registered LOG with: {}", this);

        final var insert = EQL.builder().insert(getType())
                        .set(CIERP.LogAbstract.Level, getLevel())
                        .set(CIERP.LogAbstract.LogDateTime, getLogDateTime())
                        .set(CIERP.LogAbstract.Key, getKey())
                        .set(CIERP.LogAbstract.Message, getMessage())
                        .set(CIERP.LogAbstract.Value, getValue());
        addToInsert(insert);
        return insert.execute();
    }

    public LogLevel evalLevel(String level)
    {
        return EnumUtils.getEnum(LogLevel.class, level);
    }

    protected void addToInsert(final Insert insert)
        throws EFapsException
    {
        // nothing done here in the default implementation
    }

    protected CIType getType()
    {
        return this.ciType == null ? CIERP.Log : this.ciType;
    }

    protected ObjectMapper getObjectMapper()
    {
        final var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
