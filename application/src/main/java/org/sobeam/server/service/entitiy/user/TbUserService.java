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
package org.sobeam.server.service.entitiy.user;

import jakarta.servlet.http.HttpServletRequest;
import org.sobeam.server.common.data.UserActivationLink;
import org.sobeam.server.common.data.User;
import org.sobeam.server.common.data.exception.SobeamException;
import org.sobeam.server.common.data.id.CustomerId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.id.UserId;

public interface TbUserService {

    User save(TenantId tenantId, CustomerId customerId, User tbUser, boolean sendActivationMail, HttpServletRequest request, User user) throws SobeamException;

    void delete(TenantId tenantId, CustomerId customerId, User user, User responsibleUser) throws SobeamException;

    UserActivationLink getActivationLink(TenantId tenantId, CustomerId customerId, UserId userId, HttpServletRequest request) throws SobeamException;

}
