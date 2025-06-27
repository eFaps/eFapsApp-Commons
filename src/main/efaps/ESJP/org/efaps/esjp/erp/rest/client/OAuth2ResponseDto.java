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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = OAuth2ResponseDto.Builder.class)
@EFapsUUID("8dba08ef-4976-42fd-93f4-32e4569aa50c")
@EFapsApplication("eFapsApp-Commons")
public class OAuth2ResponseDto
{

    private final String accessToken;
    private final String tokenType;
    private final int expiresIn;
    private final String refreshToken;
    private final String scope;
    private final int refreshExpiresIn;

    private OAuth2ResponseDto(Builder builder)
    {
        this.accessToken = builder.accessToken;
        this.tokenType = builder.tokenType;
        this.expiresIn = builder.expiresIn;
        this.refreshToken = builder.refreshToken;
        this.scope = builder.scope;
        this.refreshExpiresIn = builder.refreshExpiresIn;
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getTokenType()
    {
        return tokenType;
    }

    public int getExpiresIn()
    {
        return expiresIn;
    }

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public String getScope()
    {
        return scope;
    }

    public int getRefreshExpiresIn()
    {
        return refreshExpiresIn;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder
    {

        private String accessToken;
        private String tokenType;
        private int expiresIn;
        private String refreshToken;
        private String scope;
        private int refreshExpiresIn;

        private Builder()
        {
        }

        @JsonProperty("access_token")
        public Builder withAccessToken(String accessToken)
        {
            this.accessToken = accessToken;
            return this;
        }

        @JsonProperty("token_type")
        public Builder withTokenType(String tokenType)
        {
            this.tokenType = tokenType;
            return this;
        }

        @JsonProperty("expires_in")
        public Builder withExpiresIn(int expiresIn)
        {
            this.expiresIn = expiresIn;
            return this;
        }

        @JsonProperty("refresh_token")
        public Builder withRefreshToken(String refreshToken)
        {
            this.refreshToken = refreshToken;
            return this;
        }

        @JsonProperty("refresh_expires_in")
        public Builder withRefreshExpiresIn(int refreshExpiresIn)
        {
            this.refreshExpiresIn = refreshExpiresIn;
            return this;
        }

        @JsonProperty("scope")
        public Builder withScope(String scope)
        {
            this.scope = scope;
            return this;
        }

        public OAuth2ResponseDto build()
        {
            return new OAuth2ResponseDto(this);
        }
    }
}
