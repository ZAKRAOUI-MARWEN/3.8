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

import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.sobeam.server.common.transport.TransportServiceCallback;

public class CoapResponseCallback implements TransportServiceCallback<Void> {

    protected final CoapExchange exchange;
    protected final Response onSuccessResponse;
    protected final Response onFailureResponse;

    public CoapResponseCallback(CoapExchange exchange, Response onSuccessResponse, Response onFailureResponse) {
        this.exchange = exchange;
        this.onSuccessResponse = onSuccessResponse;
        this.onFailureResponse = onFailureResponse;
    }

    /**
     * @param msg
     */
    @Override
    public void onSuccess(Void msg) {
        this.onSuccessResponse.setConfirmable(isConRequest());
        exchange.respond(this.onSuccessResponse);
    }

    /**
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        exchange.respond(onFailureResponse);
    }

    protected boolean isConRequest() {
        return exchange.advanced().getRequest().isConfirmable();
    }
}
