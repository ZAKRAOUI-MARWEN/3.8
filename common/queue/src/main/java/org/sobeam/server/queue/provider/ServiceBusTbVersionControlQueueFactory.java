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

import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.sobeam.server.gen.transport.TransportProtos;
import org.sobeam.server.queue.TbQueueAdmin;
import org.sobeam.server.queue.TbQueueConsumer;
import org.sobeam.server.queue.TbQueueProducer;
import org.sobeam.server.queue.azure.servicebus.TbServiceBusAdmin;
import org.sobeam.server.queue.azure.servicebus.TbServiceBusConsumerTemplate;
import org.sobeam.server.queue.azure.servicebus.TbServiceBusProducerTemplate;
import org.sobeam.server.queue.azure.servicebus.TbServiceBusQueueConfigs;
import org.sobeam.server.queue.azure.servicebus.TbServiceBusSettings;
import org.sobeam.server.queue.common.TbProtoQueueMsg;
import org.sobeam.server.queue.discovery.TopicService;
import org.sobeam.server.queue.settings.TbQueueCoreSettings;
import org.sobeam.server.queue.settings.TbQueueVersionControlSettings;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='service-bus' && '${service.type:null}'=='tb-vc-executor'")
public class ServiceBusTbVersionControlQueueFactory implements TbVersionControlQueueFactory {

    private final TbServiceBusSettings serviceBusSettings;
    private final TbQueueCoreSettings coreSettings;
    private final TbQueueVersionControlSettings vcSettings;
    private final TopicService topicService;

    private final TbQueueAdmin coreAdmin;
    private final TbQueueAdmin notificationAdmin;
    private final TbQueueAdmin vcAdmin;

    public ServiceBusTbVersionControlQueueFactory(TbServiceBusSettings serviceBusSettings,
                                                  TbQueueCoreSettings coreSettings,
                                                  TbQueueVersionControlSettings vcSettings,
                                                  TbServiceBusQueueConfigs serviceBusQueueConfigs,
                                                  TopicService topicService
    ) {
        this.serviceBusSettings = serviceBusSettings;
        this.coreSettings = coreSettings;
        this.vcSettings = vcSettings;
        this.topicService = topicService;

        this.coreAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getCoreConfigs());
        this.notificationAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getNotificationsConfigs());
        this.vcAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getVcConfigs());
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
        return new TbServiceBusProducerTemplate<>(coreAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getUsageStatsTopic()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToCoreNotificationMsg>> createTbCoreNotificationsMsgProducer() {
        return new TbServiceBusProducerTemplate<>(notificationAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getTopic()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<TransportProtos.ToVersionControlServiceMsg>> createToVersionControlMsgConsumer() {
        return new TbServiceBusConsumerTemplate<>(vcAdmin, serviceBusSettings, topicService.buildTopicName(vcSettings.getTopic()),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), TransportProtos.ToVersionControlServiceMsg.parseFrom(msg.getData()), msg.getHeaders())
        );
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToHousekeeperServiceMsg>> createHousekeeperMsgProducer() {
        return new TbServiceBusProducerTemplate<>(coreAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getHousekeeperTopic()));
    }

    @PreDestroy
    private void destroy() {
        if (coreAdmin != null) {
            coreAdmin.destroy();
        }
        if (notificationAdmin != null) {
            notificationAdmin.destroy();
        }
        if (vcAdmin != null) {
            vcAdmin.destroy();
        }
    }
}
