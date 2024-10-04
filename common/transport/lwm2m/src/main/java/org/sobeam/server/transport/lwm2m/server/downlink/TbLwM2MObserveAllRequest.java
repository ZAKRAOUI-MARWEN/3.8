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

import lombok.Builder;
import lombok.Getter;
import org.sobeam.server.transport.lwm2m.server.LwM2MOperationType;

import java.util.Set;

public class TbLwM2MObserveAllRequest implements TbLwM2MDownlinkRequest<Set<String>> {

    @Getter
    private final long timeout;

    @Builder
    private TbLwM2MObserveAllRequest(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public LwM2MOperationType getType() {
        return LwM2MOperationType.OBSERVE_READ_ALL;
    }



}
