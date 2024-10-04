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
package org.sobeam.server.queue.provider;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.sobeam.server.gen.transport.TransportProtos.ToCoreMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToCoreNotificationMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToEdgeMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToEdgeNotificationMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToHousekeeperServiceMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToRuleEngineNotificationMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToTransportMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToUsageStatsServiceMsg;
import org.sobeam.server.gen.transport.TransportProtos.ToVersionControlServiceMsg;
import org.sobeam.server.queue.TbQueueProducer;
import org.sobeam.server.queue.common.TbProtoQueueMsg;

@Service
@ConditionalOnExpression("'${service.type:null}'=='tb-transport'")
public class TbTransportQueueProducerProvider implements TbQueueProducerProvider {

    private final TbTransportQueueFactory tbQueueProvider;
    private TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> toRuleEngine;
    private TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> toTbCore;
    private TbQueueProducer<TbProtoQueueMsg<ToCoreNotificationMsg>> toTbCoreNotifications;
    private TbQueueProducer<TbProtoQueueMsg<ToUsageStatsServiceMsg>> toUsageStats;
    private TbQueueProducer<TbProtoQueueMsg<ToHousekeeperServiceMsg>> toHousekeeper;

    public TbTransportQueueProducerProvider(TbTransportQueueFactory tbQueueProvider) {
        this.tbQueueProvider = tbQueueProvider;
    }

    @PostConstruct
    public void init() {
        this.toTbCore = tbQueueProvider.createTbCoreMsgProducer();
        this.toRuleEngine = tbQueueProvider.createRuleEngineMsgProducer();
        this.toUsageStats = tbQueueProvider.createToUsageStatsServiceMsgProducer();
        this.toTbCoreNotifications = tbQueueProvider.createTbCoreNotificationsMsgProducer();
        this.toHousekeeper = tbQueueProvider.createHousekeeperMsgProducer();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToTransportMsg>> getTransportNotificationsMsgProducer() {
        throw new RuntimeException("Not Implemented! Should not be used by Transport!");
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> getRuleEngineMsgProducer() {
        return toRuleEngine;
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> getTbCoreMsgProducer() {
        return toTbCore;
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineNotificationMsg>> getRuleEngineNotificationsMsgProducer() {
        throw new RuntimeException("Not Implemented! Should not be used by Transport!");
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreNotificationMsg>> getTbCoreNotificationsMsgProducer() {
        return toTbCoreNotifications;
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToEdgeMsg>> getTbEdgeMsgProducer() {
        throw new RuntimeException("Not Implemented! Should not be used Transport!");
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToEdgeNotificationMsg>> getTbEdgeNotificationsMsgProducer() {
        throw new RuntimeException("Not Implemented! Should not be used by Transport!");
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToVersionControlServiceMsg>> getTbVersionControlMsgProducer() {
        throw new RuntimeException("Not Implemented! Should not be used by Transport!");
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToUsageStatsServiceMsg>> getTbUsageStatsMsgProducer() {
        return toUsageStats;
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToHousekeeperServiceMsg>> getHousekeeperMsgProducer() {
        return toHousekeeper;
    }

}
