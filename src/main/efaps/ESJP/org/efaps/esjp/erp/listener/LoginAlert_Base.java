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
package org.efaps.esjp.erp.listener;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Role;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.common.loginalert.ILoginAlert;
import org.efaps.esjp.common.properties.PropertiesUtil;
import org.efaps.esjp.erp.CurrencyInst;
import org.efaps.esjp.erp.util.ERP;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.pages.login.LoginPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.joda.time.DateTime;

/**
 * The Class LoginAlertListener_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("fe70fe69-b11c-49a3-b33b-389a6d010073")
@EFapsApplication("eFapsApp-Commons")
public abstract class LoginAlert_Base
    implements ILoginAlert
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logout. */
    private boolean logout = false;

    @Override
    public void add2Alert(final StringBuilder _html)
        throws EFapsException
    {
        final Properties properties = ERP.CURRENCIES.get();
        for (final CurrencyInst curInst : CurrencyInst.getAvailable()) {
            final Properties props = PropertiesUtil.getProperties4Prefix(properties, curInst.getUUID().toString(),
                            true);
            final Properties tmpProp2 = PropertiesUtil.getProperties4Prefix(properties, curInst.getISOCode().toString(),
                            true);
            props.putAll(tmpProp2);

            if (BooleanUtils.toBoolean(props.getProperty("ForceDailyRate"))) {
                final Collection<String> forceRoles = PropertiesUtil.analyseProperty(props, "Force4Role", 0).values();
                boolean force = false;
                for (final String roleStr : forceRoles) {
                    final Role role;
                    if (UUIDUtil.isUUID(roleStr)) {
                        role = Role.get(UUID.fromString(roleStr));
                    } else {
                        role = Role.get(roleStr);
                    }
                    if (role.isAssigned()) {
                        force = true;
                        break;
                    }
                }
                if (force) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIERP.CurrencyRateAbstract);
                    queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, curInst.getInstance());
                    queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.ValidFrom, new DateTime()
                                    .withTimeAtStartOfDay());
                    if (queryBldr.getQuery().execute().isEmpty()) {
                        _html.append(DBProperties.getFormatedDBProperty(LoginAlert.class.getName() + ".ForceDailyRate",
                                        (Object) curInst.getName()));
                        setLogout(true);
                        break;
                    }
                } else {
                    final Collection<String> warnRoles = PropertiesUtil.analyseProperty(props, "Warn4Role", 0).values();
                    boolean warn = false;
                    for (final String roleStr : warnRoles) {
                        final Role role;
                        if (UUIDUtil.isUUID(roleStr)) {
                            role = Role.get(UUID.fromString(roleStr));
                        } else {
                            role = Role.get(roleStr);
                        }
                        if (role.isAssigned()) {
                            warn = true;
                            break;
                        }
                    }
                    if (warn) {
                        final QueryBuilder queryBldr = new QueryBuilder(CIERP.CurrencyRateAbstract);
                        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.CurrencyLink, curInst.getInstance());
                        queryBldr.addWhereAttrEqValue(CIERP.CurrencyRateAbstract.ValidFrom, new DateTime()
                                        .withTimeAtStartOfDay());
                        if (queryBldr.getQuery().execute().isEmpty()) {
                            _html.append(DBProperties.getFormatedDBProperty(LoginAlert.class.getName()
                                            + ".WarnDailyRate", (Object) curInst.getName()));
                            break;
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onClose()
    {
        if (isLogout()) {
            final String url = Configuration.getAttribute(ConfigAttribute.LOGOUT_URL);
            if (StringUtils.isEmpty(url)) {
                throw new RestartResponseException(LoginPage.class);
            }
            EFapsSession.get().logout();
            if (StringUtils.isNotEmpty(url)) {
                throw new RedirectToUrlException(url);
            }
        }
    }

    @Override
    public int getWeight()
    {
        return 0;
    }

    /**
     * Checks if is logout.
     *
     * @return the logout
     */
    protected boolean isLogout()
    {
        return logout;
    }

    /**
     * Sets the logout.
     *
     * @param _logout the new logout
     */
    protected void setLogout(final boolean _logout)
    {
        logout = _logout;
    }
}
