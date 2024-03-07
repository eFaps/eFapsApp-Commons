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
package org.efaps.esjp.erp.eventdefinition;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example definition implemented for test purpose.
 *
 * @author The eFaps Team
 */
@EFapsUUID("62de8cfa-9a4d-4523-b22a-9683dc611644")
@EFapsApplication("eFapsApp-Commons")
public class LogEventDefinition
    extends AbstractEventDefinition
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogEventDefinition.class);

    @Override
    public void execute(final Instance _defInstance,
                        final JobExecutionContext _jobExec)
        throws EFapsException
    {
        init(_defInstance, _jobExec);
        LogEventDefinition.LOG.info("Executed");
    }
}
