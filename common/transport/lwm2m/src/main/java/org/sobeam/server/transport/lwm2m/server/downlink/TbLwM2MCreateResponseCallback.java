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
package org.sobeam.server.transport.lwm2m.server.downlink;

import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.sobeam.server.transport.lwm2m.server.client.LwM2mClient;
import org.sobeam.server.transport.lwm2m.server.log.LwM2MTelemetryLogService;
import org.sobeam.server.transport.lwm2m.server.uplink.LwM2mUplinkMsgHandler;

public class TbLwM2MCreateResponseCallback extends TbLwM2MUplinkTargetedCallback<CreateRequest, CreateResponse> {

    public TbLwM2MCreateResponseCallback(LwM2mUplinkMsgHandler handler, LwM2MTelemetryLogService logService, LwM2mClient client, String targetId) {
        super(handler, logService, client, targetId);
    }

    @Override
    public void onSuccess(CreateRequest request, CreateResponse response) {
        super.onSuccess(request, response);
        handler.onCreateResponseOk(client, versionedId, request);
    }

}
