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
package org.sobeam.rule.engine.flow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sobeam.common.util.JacksonUtil;
import org.sobeam.rule.engine.api.EmptyNodeConfiguration;
import org.sobeam.rule.engine.api.TbContext;
import org.sobeam.rule.engine.api.TbNodeConfiguration;
import org.sobeam.rule.engine.api.TbNodeException;
import org.sobeam.server.common.data.id.DeviceId;
import org.sobeam.server.common.data.msg.TbMsgType;
import org.sobeam.server.common.msg.TbMsg;
import org.sobeam.server.common.msg.TbMsgMetaData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class TbAckNodeTest {

    private TbAckNode node;
    private EmptyNodeConfiguration config;
    private TbNodeConfiguration nodeConfiguration;

    @Mock
    private TbContext ctxMock;

    @BeforeEach
    public void setUp() {
        node = new TbAckNode();
        config = new EmptyNodeConfiguration().defaultConfiguration();
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));
    }

    @Test
    public void verifyDefaultConfig() {
        assertThat(config.getVersion()).isEqualTo(0);
    }

    @Test
    public void givenDefaultConfig_whenInit_thenOk() {
        assertThatNoException().isThrownBy(() -> node.init(ctxMock, nodeConfiguration));
    }

    @Test
    public void givenMsg_whenOnMsg_thenAckAndTellSuccess() throws TbNodeException {
        node.init(ctxMock, nodeConfiguration);
        DeviceId deviceId = new DeviceId(UUID.fromString("5770153d-6ca2-4447-8a54-5d8a4538e052"));
        TbMsg msg = TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, deviceId, TbMsgMetaData.EMPTY, TbMsg.EMPTY_JSON_OBJECT);
        node.onMsg(ctxMock, msg);

        then(ctxMock).should().ack(msg);
        then(ctxMock).should().tellSuccess(msg);
    }

}
