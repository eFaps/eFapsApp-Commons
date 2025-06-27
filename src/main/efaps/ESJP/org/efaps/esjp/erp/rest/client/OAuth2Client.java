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
package org.efaps.esjp.erp.rest.client;

import java.net.URI;
import java.time.LocalDateTime;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;

@EFapsUUID("2fb242cb-8811-4b8f-8232-0ad1770344ba")
@EFapsApplication("eFapsApp-Commons")
public class OAuth2Client
    extends AbstractRestClient
{

    private final URI target;
    private final String scope;
    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private String accessToken;
    private LocalDateTime expiresAt;
    private String refreshToken;
    private LocalDateTime refreshExpiresAt;

    protected OAuth2Client(Builder builder)
    {
        this.target = builder.target;
        this.scope = builder.scope;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.username = builder.username;
        this.password = builder.password;
    }

    protected void login()
        throws EFapsException
    {
        final var request = getClient().target(target)
                        .request(MediaType.APPLICATION_JSON);

        final var form = new Form()
                        .param("client_id", clientId)
                        .param("client_secret", clientSecret);

        if (refreshToken == null || !LocalDateTime.now().isBefore(refreshExpiresAt)) {
            form.param("grant_type", "password")
                            .param("username", username)
                            .param("password", password);
        } else {
            form.param("grant_type", "refresh_token")
                            .param("refresh_token", refreshToken);
        }
        if (scope != null) {
            form.param("scope", scope);
        }

        final var response = request.buildPost(Entity.form(form)).invoke(new GenericType<OAuth2ResponseDto>()
        {
        });

        accessToken = response.getAccessToken();
        expiresAt = LocalDateTime.now().plusSeconds(response.getExpiresIn() - 10);
        refreshToken = response.getRefreshToken();
        refreshExpiresAt = LocalDateTime.now().plusSeconds(response.getRefreshExpiresIn() - 10);
    }

    public String getToken()
        throws EFapsException
    {
        if (expiresAt == null || !LocalDateTime.now().isBefore(expiresAt)) {
            login();
        }
        return accessToken;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {

        private URI target;
        private String scope;
        private String clientId;
        private String clientSecret;
        private String username;
        private String password;

        public Builder withTarget(URI target)
        {
            this.target = target;
            return this;
        }

        public Builder withScope(String scope)
        {
            this.scope = scope;
            return this;
        }

        public Builder withClientId(String clientId)
        {
            this.clientId = clientId;
            return this;
        }

        public Builder withClientSecret(String clientSecret)
        {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder withUsername(String username)
        {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password)
        {
            this.password = password;
            return this;
        }

        public OAuth2Client build()
        {
            return new OAuth2Client(this);
        }
    }
}
