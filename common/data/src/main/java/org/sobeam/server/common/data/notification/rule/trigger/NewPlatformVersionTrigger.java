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
package org.sobeam.server.common.data.notification.rule.trigger;

import lombok.Builder;
import lombok.Data;
import org.sobeam.server.common.data.UpdateMessage;
import org.sobeam.server.common.data.id.EntityId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.notification.rule.trigger.config.NotificationRuleTriggerType;

@Data
@Builder
public class NewPlatformVersionTrigger implements NotificationRuleTrigger {

    private final UpdateMessage updateInfo;

    @Override
    public NotificationRuleTriggerType getType() {
        return NotificationRuleTriggerType.NEW_PLATFORM_VERSION;
    }

    @Override
    public TenantId getTenantId() {
        return TenantId.SYS_TENANT_ID;
    }

    @Override
    public EntityId getOriginatorEntityId() {
        return TenantId.SYS_TENANT_ID;
    }


    @Override
    public boolean deduplicate() {
        return true;
    }

    @Override
    public String getDeduplicationKey() {
        return String.join(":", NotificationRuleTrigger.super.getDeduplicationKey(),
                updateInfo.getCurrentVersion(), updateInfo.getLatestVersion());
    }

    @Override
    public long getDefaultDeduplicationDuration() {
        return 0;
    }

}
