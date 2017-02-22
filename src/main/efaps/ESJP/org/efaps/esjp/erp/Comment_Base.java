/*
 * Copyright 2003 - 2017 The eFaps Team
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

import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The Class Comment_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("2c83bbe9-bdde-4b1c-bd38-7930a190d529")
@EFapsApplication("eFapsApp-Commons")
public abstract class Comment_Base
    extends AbstractCommon
{

    /**
     * Gets the comment field value.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the comment field value
     * @throws EFapsException on error
     */
    public Return getCommentFieldValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();

        final Instance instance = _parameter.getInstance();
        final QueryBuilder queryBldr = new QueryBuilder(CIERP.CommentDocument);
        queryBldr.addWhereAttrEqValue(CIERP.CommentDocument.DocumentLink, instance);
        queryBldr.addOrderByAttributeDesc(CIERP.CommentDocument.Created);
        final MultiPrintQuery multi = queryBldr.getPrint();
        // Admin_User_PersonMsgPhrase
        final MsgPhrase msgPhrase = MsgPhrase.get(UUID.fromString("eec67428-1228-4b91-88c7-e600901887b2"));
        final SelectBuilder selPers = SelectBuilder.get().linkto(CIERP.CommentDocument.Creator);
        multi.addAttribute(CIERP.CommentDocument.Content, CIERP.CommentDocument.Created);
        multi.addMsgPhrase(selPers, msgPhrase);
        multi.setEnforceSorted(true);
        multi.execute();
        final DateTimeFormatter formatter = DateTimeFormat.shortDateTime().withLocale(Context.getThreadContext()
                        .getLocale());
        while (multi.next()) {
            final DateTime dateTime = multi.getAttribute(CIERP.CommentDocument.Created);
            html.append("<h4>").append(multi.getMsgPhrase(selPers, msgPhrase))
                .append(" - ").append(dateTime.toString(formatter)).append("</h4>")
                .append("<p>")
                .append(StringEscapeUtils.escapeHtml4(multi.getAttribute(CIERP.CommentDocument.Content)))
                .append("</p>");
        }
        ret.put(ReturnValues.SNIPLETT, html.toString());
        return ret;
    }
}
