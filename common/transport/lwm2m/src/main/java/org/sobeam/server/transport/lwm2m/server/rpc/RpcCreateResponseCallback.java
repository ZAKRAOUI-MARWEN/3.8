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
package org.sobeam.server.transport.lwm2m.server.rpc;

import org.eclipse.leshan.core.request.LwM2mRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.sobeam.server.common.transport.TransportService;
import org.sobeam.server.gen.transport.TransportProtos;
import org.sobeam.server.transport.lwm2m.server.client.LwM2mClient;
import org.sobeam.server.transport.lwm2m.server.downlink.DownlinkRequestCallback;

import java.util.Optional;

public class RpcCreateResponseCallback<R extends LwM2mRequest<T>, T extends CreateResponse> extends RpcLwM2MDownlinkCallback<R, T> {

    public RpcCreateResponseCallback(TransportService transportService, LwM2mClient client, TransportProtos.ToDeviceRpcRequestMsg requestMsg, DownlinkRequestCallback<R, T> callback) {
        super(transportService, client, requestMsg, callback);
    }

    @Override
    protected Optional<String> serializeSuccessfulResponse(T response) {
        String value = response.getLocation() != null ? "location=" + response.getLocation() : "";
        return Optional.of(value);
    }
}
