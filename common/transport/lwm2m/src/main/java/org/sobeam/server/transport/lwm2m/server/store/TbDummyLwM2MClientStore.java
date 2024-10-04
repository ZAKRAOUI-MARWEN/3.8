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
package org.sobeam.server.transport.lwm2m.server.store;

import org.sobeam.server.transport.lwm2m.server.client.LwM2mClient;

import java.util.Collections;
import java.util.Set;

public class TbDummyLwM2MClientStore implements TbLwM2MClientStore {
    @Override
    public LwM2mClient get(String endpoint) {
        return null;
    }

    @Override
    public Set<LwM2mClient> getAll() {
        return Collections.emptySet();
    }

    @Override
    public void put(LwM2mClient client) {

    }

    @Override
    public void remove(String endpoint) {

    }
}
