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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("53910077-1cac-4966-af2f-8fcccbd5f2e2")
@EFapsApplication("eFapsApp-Commons")
public abstract class WarningUtil_Base
{

    /**
     * @param _warnings warnings to be rendered as Html
     * @return StringBUildr containing the html
     * @throws EFapsException on error
     */
    public static StringBuilder getHtml4Warning(final List<IWarning> _warnings)
        throws EFapsException
    {
        final StringBuilder html = new StringBuilder();
        final List<IWarning> msgWarnings = new ArrayList<>();
        final List<IPositionWarning> posWarnings = new ArrayList<>();

        for (final IWarning warning : _warnings) {
            if (warning instanceof IPositionWarning) {
                posWarnings.add((IPositionWarning) warning);
            } else {
                msgWarnings.add(warning);
            }
        }
        if (!posWarnings.isEmpty()) {
            Collections.sort(posWarnings, new Comparator<IPositionWarning>()
            {
                @Override
                public int compare(final IPositionWarning _o1,
                                   final IPositionWarning _o2)
                {
                    int ret = 0;
                    try {
                        ret = Integer.valueOf(_o1.getPosition()).compareTo(Integer.valueOf(_o2.getPosition()));
                    } catch (final EFapsException e) {
                        e.printStackTrace();
                    }
                    return ret;
                }
            });
            html.append("<table class=\"eFapsWarningTable\">").append("<tr><th>")
                .append(DBProperties.getProperty(WarningUtil.class.getName() + ".Header.Row"))
                .append("</th><th>")
                .append(DBProperties.getProperty(WarningUtil.class.getName() + ".Header.Msg"))
                .append("</th></tr>");
            for (final IPositionWarning warning : posWarnings) {
                html.append("<tr><td>").append(warning.getPosition()).append("</td><td>")
                    .append(warning.getMessage())
                    .append("</td></tr>");
            }
            html.append("</table>");
        }
        if (!msgWarnings.isEmpty()) {
            html.append("<table class=\"eFapsMsgWarningTable\">").append("<tr><th>")
                .append(DBProperties.getProperty(WarningUtil.class.getName() + ".Header.Msg"))
                .append("</th></tr>");
            for (final IWarning warning : msgWarnings) {
                html.append("<tr><td>")
                    .append(warning.getMessage())
                    .append("</td></tr>");
            }
            html.append("</table>");
        }
        return html;
    }

    /**
     * @param _warnings warnings to be rendered as Html
     * @return StringBUildr containing the html
     * @throws EFapsException on error
     */
    public static boolean hasError(final List<IWarning> _warnings)
        throws EFapsException
    {
        boolean ret = false;
        for (final IWarning warning : _warnings) {
            if (warning.isError()) {
                ret = true;
                break;
            }
        }
        return ret;
    }
}
