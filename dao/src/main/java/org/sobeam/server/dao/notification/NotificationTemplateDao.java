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
package org.sobeam.server.dao.notification;

import org.sobeam.server.common.data.id.NotificationTemplateId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.notification.NotificationType;
import org.sobeam.server.common.data.notification.template.NotificationTemplate;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.dao.Dao;
import org.sobeam.server.dao.ExportableEntityDao;

import java.util.List;

public interface NotificationTemplateDao extends Dao<NotificationTemplate>, ExportableEntityDao<NotificationTemplateId, NotificationTemplate> {

    PageData<NotificationTemplate> findByTenantIdAndNotificationTypesAndPageLink(TenantId tenantId, List<NotificationType> notificationTypes, PageLink pageLink);

    int countByTenantIdAndNotificationTypes(TenantId tenantId, List<NotificationType> notificationTypes);

    void removeByTenantId(TenantId tenantId);

}
