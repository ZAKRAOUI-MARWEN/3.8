/**
 * Copyright © 2016-2024 The Sobeam Authors
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
package org.sobeam.server.common.msg.rpc;

import lombok.Data;
import org.sobeam.server.common.data.id.DeviceId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.msg.MsgType;
import org.sobeam.server.common.msg.ToDeviceActorNotificationMsg;

@Data
public class FromDeviceRpcResponseActorMsg implements ToDeviceActorNotificationMsg {

    private static final long serialVersionUID = -6648120137236354987L;

    private final Integer requestId;
    private final TenantId tenantId;
    private final DeviceId deviceId;
    private final FromDeviceRpcResponse msg;

    @Override
    public MsgType getMsgType() {
        return MsgType.DEVICE_RPC_RESPONSE_TO_DEVICE_ACTOR_MSG;
    }
}
