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
package org.sobeam.server.dao.attributes;

import com.google.common.util.concurrent.ListenableFuture;
import org.sobeam.server.common.data.AttributeScope;
import org.sobeam.server.common.data.id.DeviceProfileId;
import org.sobeam.server.common.data.id.EntityId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.kv.AttributeKvEntry;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
public interface AttributesService {

    ListenableFuture<Optional<AttributeKvEntry>> find(TenantId tenantId, EntityId entityId, AttributeScope scope, String attributeKey);

    ListenableFuture<List<AttributeKvEntry>> find(TenantId tenantId, EntityId entityId, AttributeScope scope, Collection<String> attributeKeys);

    ListenableFuture<List<AttributeKvEntry>> findAll(TenantId tenantId, EntityId entityId, AttributeScope scope);

    @Deprecated(since = "3.7.0")
    ListenableFuture<List<Long>> save(TenantId tenantId, EntityId entityId, String scope, List<AttributeKvEntry> attributes);

    ListenableFuture<List<Long>> save(TenantId tenantId, EntityId entityId, AttributeScope scope, List<AttributeKvEntry> attributes);

    ListenableFuture<Long> save(TenantId tenantId, EntityId entityId, AttributeScope scope, AttributeKvEntry attribute);

    @Deprecated(since = "3.7.0")
    ListenableFuture<List<String>> removeAll(TenantId tenantId, EntityId entityId, String scope, List<String> attributeKeys);

    ListenableFuture<List<String>> removeAll(TenantId tenantId, EntityId entityId, AttributeScope scope, List<String> attributeKeys);

    List<String> findAllKeysByDeviceProfileId(TenantId tenantId, DeviceProfileId deviceProfileId);

    List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds);

    List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds, String scope);

    int removeAllByEntityId(TenantId tenantId, EntityId entityId);

}
