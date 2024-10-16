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
package org.sobeam.server.dao.sql.mobile;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.sobeam.server.common.data.EntityType;
import org.sobeam.server.common.data.id.MobileAppId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.mobile.MobileApp;
import org.sobeam.server.common.data.mobile.MobileAppOauth2Client;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.dao.DaoUtil;
import org.sobeam.server.dao.mobile.MobileAppDao;
import org.sobeam.server.dao.model.sql.MobileAppEntity;
import org.sobeam.server.dao.model.sql.MobileAppOauth2ClientCompositeKey;
import org.sobeam.server.dao.model.sql.MobileAppOauth2ClientEntity;
import org.sobeam.server.dao.sql.JpaAbstractDao;
import org.sobeam.server.dao.util.SqlDao;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@SqlDao
public class JpaMobileAppDao extends JpaAbstractDao<MobileAppEntity, MobileApp> implements MobileAppDao {

    private final MobileAppRepository mobileAppRepository;
    private final MobileAppOauth2ClientRepository mobileOauth2ProviderRepository;

    @Override
    protected Class<MobileAppEntity> getEntityClass() {
        return MobileAppEntity.class;
    }

    @Override
    protected JpaRepository<MobileAppEntity, UUID> getRepository() {
        return mobileAppRepository;
    }

    @Override
    public PageData<MobileApp> findByTenantId(TenantId tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(mobileAppRepository.findByTenantId(tenantId.getId(), pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    @Override
    public List<MobileAppOauth2Client> findOauth2ClientsByMobileAppId(TenantId tenantId, MobileAppId mobileAppId) {
        return DaoUtil.convertDataList(mobileOauth2ProviderRepository.findAllByMobileAppId(mobileAppId.getId()));
    }

    @Override
    public void addOauth2Client(MobileAppOauth2Client mobileAppOauth2Client) {
        mobileOauth2ProviderRepository.save(new MobileAppOauth2ClientEntity(mobileAppOauth2Client));
    }

    @Override
    public void removeOauth2Client(MobileAppOauth2Client mobileAppOauth2Client) {
        mobileOauth2ProviderRepository.deleteById(new MobileAppOauth2ClientCompositeKey(mobileAppOauth2Client.getMobileAppId().getId(),
                mobileAppOauth2Client.getOAuth2ClientId().getId()));
    }

    @Override
    public void deleteByTenantId(TenantId tenantId) {
        mobileAppRepository.deleteByTenantId(tenantId.getId());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MOBILE_APP;
    }

}

