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
package org.sobeam.server.service.notification.channels;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.sobeam.rule.engine.api.SmsService;
import org.sobeam.server.common.data.User;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.notification.NotificationDeliveryMethod;
import org.sobeam.server.common.data.notification.template.SmsDeliveryMethodNotificationTemplate;
import org.sobeam.server.service.notification.NotificationProcessingContext;

@Component
@RequiredArgsConstructor
public class SmsNotificationChannel implements NotificationChannel<User, SmsDeliveryMethodNotificationTemplate> {

    private final SmsService smsService;

    @Override
    public void sendNotification(User recipient, SmsDeliveryMethodNotificationTemplate processedTemplate, NotificationProcessingContext ctx) throws Exception {
        String phone = recipient.getPhone();
        if (StringUtils.isBlank(phone)) {
            throw new RuntimeException("User does not have phone number");
        }

        smsService.sendSms(ctx.getTenantId(), null, new String[]{phone}, processedTemplate.getBody());
    }

    @Override
    public void check(TenantId tenantId) throws Exception {
        if (!smsService.isConfigured(tenantId)) {
            throw new RuntimeException("SMS provider is not configured");
        }
    }

    @Override
    public NotificationDeliveryMethod getDeliveryMethod() {
        return NotificationDeliveryMethod.SMS;
    }

}
