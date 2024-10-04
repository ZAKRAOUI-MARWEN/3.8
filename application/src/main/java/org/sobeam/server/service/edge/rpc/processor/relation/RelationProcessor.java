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
package org.sobeam.server.service.edge.rpc.processor.relation;

import com.google.common.util.concurrent.ListenableFuture;
import org.sobeam.server.common.data.edge.Edge;
import org.sobeam.server.common.data.edge.EdgeEvent;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.gen.edge.v1.DownlinkMsg;
import org.sobeam.server.gen.edge.v1.EdgeVersion;
import org.sobeam.server.gen.edge.v1.RelationUpdateMsg;
import org.sobeam.server.service.edge.rpc.processor.EdgeProcessor;

public interface RelationProcessor extends EdgeProcessor {

    ListenableFuture<Void> processRelationMsgFromEdge(TenantId tenantId, Edge edge, RelationUpdateMsg relationUpdateMsg);

    DownlinkMsg convertRelationEventToDownlink(EdgeEvent edgeEvent, EdgeVersion edgeVersion);

}
