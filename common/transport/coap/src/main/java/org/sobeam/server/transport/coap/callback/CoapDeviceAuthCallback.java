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
package org.sobeam.server.transport.coap.callback;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.sobeam.server.common.data.DeviceProfile;
import org.sobeam.server.common.transport.TransportServiceCallback;
import org.sobeam.server.common.transport.auth.ValidateDeviceCredentialsResponse;

import java.util.function.BiConsumer;

@Slf4j
public class CoapDeviceAuthCallback implements TransportServiceCallback<ValidateDeviceCredentialsResponse> {
    private final CoapExchange exchange;
    private final BiConsumer<ValidateDeviceCredentialsResponse, DeviceProfile> onSuccess;

    public CoapDeviceAuthCallback(CoapExchange exchange, BiConsumer<ValidateDeviceCredentialsResponse, DeviceProfile> onSuccess) {
        this.exchange = exchange;
        this.onSuccess = onSuccess;
    }

    @Override
    public void onSuccess(ValidateDeviceCredentialsResponse msg) {
        DeviceProfile deviceProfile = msg.getDeviceProfile();
        if (msg.hasDeviceInfo() && deviceProfile != null) {
            onSuccess.accept(msg, deviceProfile);
        } else {
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED);
        }
    }

    @Override
    public void onError(Throwable e) {
        log.warn("Failed to process request", e);
        exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
