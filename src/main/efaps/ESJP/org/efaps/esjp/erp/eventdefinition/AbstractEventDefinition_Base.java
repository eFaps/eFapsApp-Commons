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

package org.efaps.esjp.erp.eventdefinition;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Company;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.quartz.IEventDefinition;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractEventDefinition_Base.java 11608 2014-01-07 22:53:24Z
 *          jan@moxter.net $
 */
@EFapsUUID("e318cf5c-63aa-45d1-bed8-8729e305e9c0")
@EFapsRevision("$Rev$")
public abstract class AbstractEventDefinition_Base
    implements IEventDefinition
{

    private Properties properties;

    private final List<EventSchedule> events = new ArrayList<EventSchedule>();

    protected void init(final Instance _defInstance,
                        final JobExecutionContext _jobExec)
        throws EFapsException
    {
        initProperties(_defInstance);
        initEvents(_defInstance);
    }

    protected void initEvents(final Instance _defInstance)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.EventScheduleAbstract);
        queryBldr.addWhereAttrEqValue(CIERP.EventScheduleAbstract.DefinitionLinkAbstract, _defInstance);
        add2QueryBlrd4initEvents(queryBldr);
        final InstanceQuery query = queryBldr.getQuery();
        query.setCompanyDepended(false);
        final MultiPrintQuery multi = new MultiPrintQuery(query.executeWithoutAccessCheck());
        multi.addAttribute(CIERP.EventScheduleAbstract.StatusAbstract, CIERP.EventScheduleAbstract.Company);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            final EventSchedule event = getEventSchedule(multi.getCurrentInstance(),
                            multi.<Company>getAttribute(CIERP.EventScheduleAbstract.Company),
                            multi.<Long>getAttribute(CIERP.EventScheduleAbstract.StatusAbstract));
            this.events.add(event);
        }
    }

    protected void add2QueryBlrd4initEvents(final QueryBuilder _queryBldr)
        throws EFapsException
    {
        final DateTime today = new DateTime().withTimeAtStartOfDay();
        _queryBldr.addWhereAttrGreaterValue(CIERP.EventScheduleAbstract.Date, today.minusSeconds(1));
        _queryBldr.addWhereAttrLessValue(CIERP.EventScheduleAbstract.Date, today.plusDays(1));
    }

    protected void initProperties(final Instance _defInstance)
        throws EFapsException
    {
        this.properties = new Properties();

        final PrintQuery print = new PrintQuery(_defInstance);
        print.addAttribute(CIERP.EventDefinitionAbstract.Properties);
        print.executeWithoutAccessCheck();
        try {
            this.properties.load(new StringReader(print.<String>getAttribute(CIERP.EventDefinitionAbstract.Properties)));
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected EventSchedule getEventSchedule(final Instance _instance,
                                             final Company _company,
                                             final Long _statusId)
        throws EFapsException
    {
        return new EventSchedule(_instance, _company, _statusId);
    }

    /**
     * Getter method for the instance variable {@link #properties}.
     *
     * @return value of instance variable {@link #properties}
     */
    public Properties getProperties()
    {
        return this.properties;
    }

    /**
     * Setter method for instance variable {@link #properties}.
     *
     * @param _properties value for instance variable {@link #properties}
     */
    public void setProperties(final Properties _properties)
    {
        this.properties = _properties;
    }

    /**
     * Getter method for the instance variable {@link #events}.
     *
     * @return value of instance variable {@link #events}
     */
    public List<EventSchedule> getEvents()
    {
        return this.events;
    }

    public static class EventSchedule
    {

        private final Instance instance;
        private final Long statusId;
        private final Company company;

        /**
         * @param _instance
         */
        public EventSchedule(final Instance _instance,
                             final Company _company,
                             final Long _statusId)
        {
            this.instance = _instance;
            this.company = _company;
            this.statusId = _statusId;
        }

        /**
         * Getter method for the instance variable {@link #instance}.
         *
         * @return value of instance variable {@link #instance}
         */
        public Instance getInstance()
        {
            return this.instance;
        }

        /**
         * Getter method for the instance variable {@link #company}.
         *
         * @return value of instance variable {@link #company}
         */
        public Company getCompany()
        {
            return this.company;
        }

        /**
         * Getter method for the instance variable {@link #statusId}.
         *
         * @return value of instance variable {@link #statusId}
         */
        public Long getStatusId()
        {
            return this.statusId;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
