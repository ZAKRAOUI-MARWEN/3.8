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
package org.sobeam.server.dao.domain;

import org.sobeam.server.common.data.domain.Domain;
import org.sobeam.server.common.data.domain.DomainOauth2Client;
import org.sobeam.server.common.data.id.DomainId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.dao.Dao;

import java.util.List;

public interface DomainDao extends Dao<Domain> {

    PageData<Domain> findByTenantId(TenantId tenantId, PageLink pageLink);

    int countDomainByTenantIdAndOauth2Enabled(TenantId tenantId, boolean oauth2Enabled);

    List<DomainOauth2Client> findOauth2ClientsByDomainId(TenantId tenantId, DomainId domainId);

    void addOauth2Client(DomainOauth2Client domainOauth2Client);

    void removeOauth2Client(DomainOauth2Client domainOauth2Client);

    void deleteByTenantId(TenantId tenantId);
}
