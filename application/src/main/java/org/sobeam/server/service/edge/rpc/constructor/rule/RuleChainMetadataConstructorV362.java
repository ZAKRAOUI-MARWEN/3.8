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
package org.sobeam.server.service.edge.rpc.constructor.rule;

import org.sobeam.common.util.JacksonUtil;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.rule.RuleChainMetaData;
import org.sobeam.server.gen.edge.v1.RuleChainMetadataUpdateMsg;

public class RuleChainMetadataConstructorV362 extends BaseRuleChainMetadataConstructor {

    @Override
    protected void constructRuleChainMetadataUpdatedMsg(TenantId tenantId, RuleChainMetadataUpdateMsg.Builder builder, RuleChainMetaData ruleChainMetaData) {
        builder.setEntity(JacksonUtil.toString(ruleChainMetaData));
    }
}
