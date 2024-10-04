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
package org.sobeam.server.service.edge.rpc.fetch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sobeam.server.common.data.Device;
import org.sobeam.server.common.data.EdgeUtils;
import org.sobeam.server.common.data.edge.Edge;
import org.sobeam.server.common.data.edge.EdgeEvent;
import org.sobeam.server.common.data.edge.EdgeEventActionType;
import org.sobeam.server.common.data.edge.EdgeEventType;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.dao.device.DeviceService;

@AllArgsConstructor
@Slf4j
public class DevicesEdgeEventFetcher extends BasePageableEdgeEventFetcher<Device> {

    private final DeviceService deviceService;

    @Override
    PageData<Device> fetchEntities(TenantId tenantId, Edge edge, PageLink pageLink) {
        return deviceService.findDevicesByTenantIdAndEdgeId(tenantId, edge.getId(), pageLink);
    }

    @Override
    EdgeEvent constructEdgeEvent(TenantId tenantId, Edge edge, Device device) {
        return EdgeUtils.constructEdgeEvent(tenantId, edge.getId(), EdgeEventType.DEVICE,
                EdgeEventActionType.ADDED, device.getId(), null);
    }

}
