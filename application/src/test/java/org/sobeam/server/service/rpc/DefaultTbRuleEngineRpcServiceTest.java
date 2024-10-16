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
package org.sobeam.server.service.rpc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sobeam.server.cluster.TbClusterService;
import org.sobeam.server.common.data.id.DeviceId;
import org.sobeam.server.common.data.msg.TbMsgType;
import org.sobeam.server.common.msg.TbMsg;
import org.sobeam.server.common.msg.TbMsgMetaData;
import org.sobeam.server.gen.transport.TransportProtos;

import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DefaultTbRuleEngineRpcServiceTest {

    @Mock
    private TbClusterService tbClusterServiceMock;

    @InjectMocks
    private DefaultTbRuleEngineRpcService tbRuleEngineRpcService;

    @Test
    public void givenTbMsg_whenSendRestApiCallReply_thenPushNotificationToCore() {
        // GIVEN
        String serviceId = "tb-core-0";
        UUID requestId = UUID.fromString("f64a20df-eb1e-46a3-ba6f-0b3ae053ee0a");
        DeviceId deviceId = new DeviceId(UUID.fromString("1d9f771a-7cdc-4ac7-838c-ba193d05a012"));
        TbMsg msg = TbMsg.newMsg(TbMsgType.REST_API_REQUEST, deviceId, TbMsgMetaData.EMPTY, TbMsg.EMPTY_JSON_OBJECT);
        var restApiCallResponseMsgProto = TransportProtos.RestApiCallResponseMsgProto.newBuilder()
                .setRequestIdMSB(requestId.getMostSignificantBits())
                .setRequestIdLSB(requestId.getLeastSignificantBits())
                .setResponse(TbMsg.toByteString(msg))
                .build();

        // WHEN
        tbRuleEngineRpcService.sendRestApiCallReply(serviceId, requestId, msg);

        // THEN
        then(tbClusterServiceMock).should().pushNotificationToCore(serviceId, restApiCallResponseMsgProto, null);
    }
}
