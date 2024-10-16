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
package org.sobeam.server.common.transport;

import org.sobeam.server.common.data.ResourceType;
import org.sobeam.server.common.data.TbResource;
import org.sobeam.server.common.data.id.TenantId;

import java.util.Optional;

public interface TransportResourceCache {

    Optional<TbResource> get(TenantId tenantId, ResourceType resourceType, String resourceId);

    void update(TenantId tenantId, ResourceType resourceType, String resourceI);

    void evict(TenantId tenantId, ResourceType resourceType, String resourceId);
}
