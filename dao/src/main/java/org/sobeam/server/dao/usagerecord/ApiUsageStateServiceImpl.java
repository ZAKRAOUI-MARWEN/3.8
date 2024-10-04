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
package org.sobeam.server.dao.usagerecord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sobeam.server.common.data.ApiFeature;
import org.sobeam.server.common.data.ApiUsageRecordKey;
import org.sobeam.server.common.data.ApiUsageState;
import org.sobeam.server.common.data.ApiUsageStateValue;
import org.sobeam.server.common.data.EntityType;
import org.sobeam.server.common.data.Tenant;
import org.sobeam.server.common.data.TenantProfile;
import org.sobeam.server.common.data.id.ApiUsageStateId;
import org.sobeam.server.common.data.id.EntityId;
import org.sobeam.server.common.data.id.HasId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.kv.BasicTsKvEntry;
import org.sobeam.server.common.data.kv.LongDataEntry;
import org.sobeam.server.common.data.kv.StringDataEntry;
import org.sobeam.server.common.data.kv.TsKvEntry;
import org.sobeam.server.common.data.tenant.profile.TenantProfileConfiguration;
import org.sobeam.server.dao.entity.AbstractEntityService;
import org.sobeam.server.dao.eventsourcing.DeleteEntityEvent;
import org.sobeam.server.dao.eventsourcing.SaveEntityEvent;
import org.sobeam.server.dao.service.DataValidator;
import org.sobeam.server.dao.tenant.TenantProfileDao;
import org.sobeam.server.dao.tenant.TenantService;
import org.sobeam.server.dao.timeseries.TimeseriesService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.sobeam.server.dao.service.Validator.validateId;

@Service("ApiUsageStateDaoService")
@Slf4j
public class ApiUsageStateServiceImpl extends AbstractEntityService implements ApiUsageStateService {
    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";

    private final ApiUsageStateDao apiUsageStateDao;
    private final TenantProfileDao tenantProfileDao;
    private final TenantService tenantService;
    private final TimeseriesService tsService;
    private final DataValidator<ApiUsageState> apiUsageStateValidator;

    public ApiUsageStateServiceImpl(ApiUsageStateDao apiUsageStateDao, TenantProfileDao tenantProfileDao,
                                    TenantService tenantService, @Lazy TimeseriesService tsService,
                                    DataValidator<ApiUsageState> apiUsageStateValidator) {
        this.apiUsageStateDao = apiUsageStateDao;
        this.tenantProfileDao = tenantProfileDao;
        this.tenantService = tenantService;
        this.tsService = tsService;
        this.apiUsageStateValidator = apiUsageStateValidator;
    }

    @Transactional
    @Override
    public void deleteApiUsageStateByEntityId(EntityId entityId) {
        log.trace("Executing deleteApiUsageStateByEntityId [{}]", entityId);
        validateId(entityId.getId(), id -> "Invalid entity id " + id);
        ApiUsageState apiUsageState = findApiUsageStateByEntityId(entityId);
        deleteApiUsageState(apiUsageState);
    }

    private void deleteApiUsageState(ApiUsageState apiUsageState) {
        if (apiUsageState != null) {
            apiUsageStateDao.removeById(apiUsageState.getTenantId(), apiUsageState.getUuidId());
            eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(apiUsageState.getTenantId()).entityId(apiUsageState.getId()).build());
        }
    }

    @Override
    public void deleteEntity(TenantId tenantId, EntityId id, boolean force) {
        ApiUsageState apiUsageState = findApiUsageStateById(tenantId, (ApiUsageStateId) id);
        deleteApiUsageState(apiUsageState);
    }

    @Override
    public ApiUsageState createDefaultApiUsageState(TenantId tenantId, EntityId entityId) {
        entityId = Objects.requireNonNullElse(entityId, tenantId);
        log.trace("Executing createDefaultUsageRecord [{}]", entityId);
        validateId(tenantId, id -> INCORRECT_TENANT_ID + id);
        ApiUsageState apiUsageState = new ApiUsageState();
        apiUsageState.setTenantId(tenantId);
        apiUsageState.setEntityId(entityId);
        apiUsageState.setTransportState(ApiUsageStateValue.ENABLED);
        apiUsageState.setReExecState(ApiUsageStateValue.ENABLED);
        apiUsageState.setJsExecState(ApiUsageStateValue.ENABLED);
        apiUsageState.setTbelExecState(ApiUsageStateValue.ENABLED);
        apiUsageState.setDbStorageState(ApiUsageStateValue.ENABLED);
        apiUsageState.setSmsExecState(ApiUsageStateValue.ENABLED);
        apiUsageState.setEmailExecState(ApiUsageStateValue.ENABLED);
        apiUsageState.setAlarmExecState(ApiUsageStateValue.ENABLED);
        apiUsageStateValidator.validate(apiUsageState, ApiUsageState::getTenantId);

        ApiUsageState saved = apiUsageStateDao.save(apiUsageState.getTenantId(), apiUsageState);

        List<TsKvEntry> apiUsageStates = new ArrayList<>();
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.TRANSPORT.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.DB.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.RE.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.JS.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.TBEL.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.EMAIL.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.SMS.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        apiUsageStates.add(new BasicTsKvEntry(saved.getCreatedTime(),
                new StringDataEntry(ApiFeature.ALARM.getApiStateKey(), ApiUsageStateValue.ENABLED.name())));
        tsService.save(tenantId, saved.getId(), apiUsageStates, 0L);

        if (entityId.getEntityType() == EntityType.TENANT && !entityId.equals(TenantId.SYS_TENANT_ID)) {
            tenantId = (TenantId) entityId;
            Tenant tenant = tenantService.findTenantById(tenantId);
            TenantProfile tenantProfile = tenantProfileDao.findById(tenantId, tenant.getTenantProfileId().getId());
            TenantProfileConfiguration configuration = tenantProfile.getProfileData().getConfiguration();

            List<TsKvEntry> profileThresholds = new ArrayList<>();
            for (ApiUsageRecordKey key : ApiUsageRecordKey.values()) {
                if (key.getApiLimitKey() == null) continue;
                profileThresholds.add(new BasicTsKvEntry(saved.getCreatedTime(), new LongDataEntry(key.getApiLimitKey(), configuration.getProfileThreshold(key))));
            }
            tsService.save(tenantId, saved.getId(), profileThresholds, 0L);
        }

        return saved;
    }

    @Override
    public ApiUsageState update(ApiUsageState apiUsageState) {
        log.trace("Executing save [{}]", apiUsageState.getTenantId());
        validateId(apiUsageState.getTenantId(), id -> INCORRECT_TENANT_ID + id);
        validateId(apiUsageState.getId(), "Can't save new usage state. Only update is allowed!");
        ApiUsageState savedState = apiUsageStateDao.save(apiUsageState.getTenantId(), apiUsageState);
        eventPublisher.publishEvent(SaveEntityEvent.builder().tenantId(savedState.getTenantId()).entityId(savedState.getId())
                .entity(savedState).build());
        return savedState;
    }

    @Override
    public ApiUsageState findTenantApiUsageState(TenantId tenantId) {
        log.trace("Executing findTenantUsageRecord, tenantId [{}]", tenantId);
        validateId(tenantId, id -> INCORRECT_TENANT_ID + id);
        return apiUsageStateDao.findTenantApiUsageState(tenantId.getId());
    }

    @Override
    public ApiUsageState findApiUsageStateByEntityId(EntityId entityId) {
        validateId(entityId.getId(), id -> "Invalid entity id " + id);
        return apiUsageStateDao.findApiUsageStateByEntityId(entityId);
    }

    @Override
    public ApiUsageState findApiUsageStateById(TenantId tenantId, ApiUsageStateId id) {
        log.trace("Executing findApiUsageStateById, tenantId [{}], apiUsageStateId [{}]", tenantId, id);
        validateId(tenantId, t -> INCORRECT_TENANT_ID + t);
        validateId(id, u -> "Incorrect apiUsageStateId " + u);
        return apiUsageStateDao.findById(tenantId, id.getId());
    }

    @Override
    public Optional<HasId<?>> findEntity(TenantId tenantId, EntityId entityId) {
        return Optional.ofNullable(findApiUsageStateById(tenantId, new ApiUsageStateId(entityId.getId())));
    }

    @Transactional
    @Override
    public void deleteByTenantId(TenantId tenantId) {
        deleteApiUsageStateByEntityId(tenantId);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.API_USAGE_STATE;
    }

}
