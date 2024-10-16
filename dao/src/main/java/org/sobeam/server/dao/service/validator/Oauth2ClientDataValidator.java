/**
 * Copyright © 2024 The Sobeam Authors
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
package org.sobeam.server.dao.service.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.sobeam.server.common.data.StringUtils;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.oauth2.MapperType;
import org.sobeam.server.common.data.oauth2.OAuth2BasicMapperConfig;
import org.sobeam.server.common.data.oauth2.OAuth2CustomMapperConfig;
import org.sobeam.server.common.data.oauth2.OAuth2MapperConfig;
import org.sobeam.server.common.data.oauth2.OAuth2Client;
import org.sobeam.server.common.data.oauth2.TenantNameStrategyType;
import org.sobeam.server.dao.exception.DataValidationException;
import org.sobeam.server.dao.service.DataValidator;

@Component
@AllArgsConstructor
public class Oauth2ClientDataValidator extends DataValidator<OAuth2Client> {

    @Override
    protected void validateDataImpl(TenantId tenantId, OAuth2Client oAuth2Client) {
        OAuth2MapperConfig mapperConfig = oAuth2Client.getMapperConfig();
        if (mapperConfig.getType() == MapperType.BASIC) {
            OAuth2BasicMapperConfig basicConfig = mapperConfig.getBasic();
            if (basicConfig == null) {
                throw new DataValidationException("Basic config should be specified!");
            }
            if (StringUtils.isEmpty(basicConfig.getEmailAttributeKey())) {
                throw new DataValidationException("Email attribute key should be specified!");
            }
            if (basicConfig.getTenantNameStrategy() == null) {
                throw new DataValidationException("Tenant name strategy should be specified!");
            }
            if (basicConfig.getTenantNameStrategy() == TenantNameStrategyType.CUSTOM
                    && StringUtils.isEmpty(basicConfig.getTenantNamePattern())) {
                throw new DataValidationException("Tenant name pattern should be specified!");
            }
        }
        if (mapperConfig.getType() == MapperType.GITHUB) {
            OAuth2BasicMapperConfig basicConfig = mapperConfig.getBasic();
            if (basicConfig == null) {
                throw new DataValidationException("Basic config should be specified!");
            }
            if (!StringUtils.isEmpty(basicConfig.getEmailAttributeKey())) {
                throw new DataValidationException("Email attribute key cannot be configured for GITHUB mapper type!");
            }
            if (basicConfig.getTenantNameStrategy() == null) {
                throw new DataValidationException("Tenant name strategy should be specified!");
            }
            if (basicConfig.getTenantNameStrategy() == TenantNameStrategyType.CUSTOM
                    && StringUtils.isEmpty(basicConfig.getTenantNamePattern())) {
                throw new DataValidationException("Tenant name pattern should be specified!");
            }
        }
        if (mapperConfig.getType() == MapperType.CUSTOM) {
            OAuth2CustomMapperConfig customConfig = mapperConfig.getCustom();
            if (customConfig == null) {
                throw new DataValidationException("Custom config should be specified!");
            }
            if (StringUtils.isEmpty(customConfig.getUrl())) {
                throw new DataValidationException("Custom mapper URL should be specified!");
            }
        }
    }
}
