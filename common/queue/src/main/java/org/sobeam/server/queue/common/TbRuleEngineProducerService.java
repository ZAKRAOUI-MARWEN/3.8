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
package org.sobeam.server.queue.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.msg.TbMsg;
import org.sobeam.server.common.msg.queue.ServiceType;
import org.sobeam.server.common.msg.queue.TopicPartitionInfo;
import org.sobeam.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import org.sobeam.server.queue.TbQueueCallback;
import org.sobeam.server.queue.TbQueueProducer;
import org.sobeam.server.queue.discovery.PartitionService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TbRuleEngineProducerService {

    private final PartitionService partitionService;

    public void sendToRuleEngine(TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> producer,
                                 TenantId tenantId, TbMsg tbMsg, TbQueueCallback callback) {
        List<TopicPartitionInfo> tpis = partitionService.resolveAll(ServiceType.TB_RULE_ENGINE, tbMsg.getQueueName(), tenantId, tbMsg.getOriginator());
        if (tpis.size() > 1) {
            UUID correlationId = UUID.randomUUID();
            for (int i = 0; i < tpis.size(); i++) {
                TopicPartitionInfo tpi = tpis.get(i);
                Integer partition = tpi.getPartition().orElse(null);
                UUID id = i > 0 ? UUID.randomUUID() : tbMsg.getId();

                tbMsg = tbMsg.toBuilder()
                        .id(id)
                        .correlationId(correlationId)
                        .partition(partition)
                        .build();
                sendToRuleEngine(producer, tpi, tenantId, tbMsg, i == tpis.size() - 1 ? callback : null);
            }
        } else {
            sendToRuleEngine(producer, tpis.get(0), tenantId, tbMsg, callback);
        }
    }

    private void sendToRuleEngine(TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> producer, TopicPartitionInfo tpi,
                                  TenantId tenantId, TbMsg tbMsg, TbQueueCallback callback) {
        if (log.isTraceEnabled()) {
            log.trace("[{}][{}] Pushing to topic {} message {}", tenantId, tbMsg.getOriginator(), tpi.getFullTopicName(), tbMsg);
        }
        ToRuleEngineMsg msg = ToRuleEngineMsg.newBuilder()
                .setTbMsg(TbMsg.toByteString(tbMsg))
                .setTenantIdMSB(tenantId.getId().getMostSignificantBits())
                .setTenantIdLSB(tenantId.getId().getLeastSignificantBits()).build();
        producer.send(tpi, new TbProtoQueueMsg<>(tbMsg.getId(), msg), callback);
    }

}
