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
package org.sobeam.server.dao.oauth2;

import org.sobeam.server.common.data.id.OAuth2ClientId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.oauth2.OAuth2Client;
import org.sobeam.server.common.data.oauth2.OAuth2ClientInfo;
import org.sobeam.server.common.data.oauth2.OAuth2ClientLoginInfo;
import org.sobeam.server.common.data.oauth2.PlatformType;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.dao.entity.EntityDaoService;

import java.util.List;
import java.util.UUID;

public interface OAuth2ClientService extends EntityDaoService {

    List<OAuth2ClientLoginInfo> findOAuth2ClientLoginInfosByDomainName(String domainName);

    List<OAuth2ClientLoginInfo> findOAuth2ClientLoginInfosByMobilePkgNameAndPlatformType(String pkgName, PlatformType platformType);

    List<OAuth2Client> findOAuth2ClientsByTenantId(TenantId tenantId);

    OAuth2Client saveOAuth2Client(TenantId tenantId, OAuth2Client oAuth2Client);

    OAuth2Client findOAuth2ClientById(TenantId tenantId, OAuth2ClientId providerId);

    String findAppSecret(OAuth2ClientId oAuth2ClientId, String pkgName);

    void deleteOAuth2ClientById(TenantId tenantId, OAuth2ClientId oAuth2ClientId);

    void deleteOauth2ClientsByTenantId(TenantId tenantId);

    PageData<OAuth2ClientInfo> findOAuth2ClientInfosByTenantId(TenantId tenantId, PageLink pageLink);

    List<OAuth2ClientInfo> findOAuth2ClientInfosByIds(TenantId tenantId, List<OAuth2ClientId> oAuth2ClientIds);

    boolean isPropagateOAuth2ClientToEdge(TenantId tenantId, OAuth2ClientId oAuth2ClientId);

}
