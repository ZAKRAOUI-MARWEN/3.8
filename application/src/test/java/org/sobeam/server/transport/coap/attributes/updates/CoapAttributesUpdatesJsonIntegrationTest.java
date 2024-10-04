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
package org.sobeam.server.transport.coap.attributes.updates;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sobeam.server.common.data.CoapDeviceType;
import org.sobeam.server.common.data.TransportPayloadType;
import org.sobeam.server.dao.service.DaoSqlTest;
import org.sobeam.server.transport.coap.CoapTestConfigProperties;
import org.sobeam.server.transport.coap.attributes.AbstractCoapAttributesIntegrationTest;

@Slf4j
@DaoSqlTest
public class CoapAttributesUpdatesJsonIntegrationTest extends AbstractCoapAttributesIntegrationTest {

    @Before
    public void beforeTest() throws Exception {
        CoapTestConfigProperties configProperties = CoapTestConfigProperties.builder()
                .deviceName("Test Subscribe to attribute updates")
                .coapDeviceType(CoapDeviceType.DEFAULT)
                .transportPayloadType(TransportPayloadType.JSON)
                .build();
        processBeforeTest(configProperties);
    }

    @After
    public void afterTest() throws Exception {
        processAfterTest();
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServer() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(false);
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServerWithEmptyCurrentStateNotification() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(true);
    }
}
