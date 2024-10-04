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
package org.sobeam.server.service.entitiy.user;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sobeam.rule.engine.api.MailService;
import org.sobeam.server.common.data.EntityType;
import org.sobeam.server.common.data.User;
import org.sobeam.server.common.data.UserActivationLink;
import org.sobeam.server.common.data.audit.ActionType;
import org.sobeam.server.common.data.exception.SobeamErrorCode;
import org.sobeam.server.common.data.exception.SobeamException;
import org.sobeam.server.common.data.id.CustomerId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.id.UserId;
import org.sobeam.server.common.data.security.UserCredentials;
import org.sobeam.server.dao.user.UserService;
import org.sobeam.server.queue.util.TbCoreComponent;
import org.sobeam.server.service.entitiy.AbstractTbEntityService;
import org.sobeam.server.service.security.system.SystemSecurityService;

import java.util.concurrent.TimeUnit;

@Service
@TbCoreComponent
@AllArgsConstructor
@Slf4j
public class DefaultUserService extends AbstractTbEntityService implements TbUserService {

    private final UserService userService;
    private final MailService mailService;
    private final SystemSecurityService systemSecurityService;

    @Override
    public User save(TenantId tenantId, CustomerId customerId, User tbUser, boolean sendActivationMail,
                     HttpServletRequest request, User user) throws SobeamException {
        ActionType actionType = tbUser.getId() == null ? ActionType.ADDED : ActionType.UPDATED;
        try {
            boolean sendEmail = tbUser.getId() == null && sendActivationMail;
            User savedUser = checkNotNull(userService.saveUser(tenantId, tbUser));
            if (sendEmail) {
                UserActivationLink activationLink = getActivationLink(tenantId, customerId, savedUser.getId(), request);
                try {
                    mailService.sendActivationEmail(activationLink.value(), activationLink.ttlMs(), savedUser.getEmail());
                } catch (SobeamException e) {
                    userService.deleteUser(tenantId, savedUser);
                    throw e;
                }
            }
            logEntityActionService.logEntityAction(tenantId, savedUser.getId(), savedUser, customerId, actionType, user);
            return savedUser;
        } catch (Exception e) {
            logEntityActionService.logEntityAction(tenantId, emptyId(EntityType.USER), tbUser, actionType, user, e);
            throw e;
        }
    }

    @Override
    public void delete(TenantId tenantId, CustomerId customerId, User user, User responsibleUser) throws SobeamException {
        ActionType actionType = ActionType.DELETED;
        UserId userId = user.getId();

        try {
            userService.deleteUser(tenantId, user);
            logEntityActionService.logEntityAction(tenantId, userId, user, customerId, actionType, responsibleUser, customerId.toString());
        } catch (Exception e) {
            logEntityActionService.logEntityAction(tenantId, emptyId(EntityType.USER),
                    actionType, responsibleUser, e, userId.toString());
            throw e;
        }
    }

    @Override
    public UserActivationLink getActivationLink(TenantId tenantId, CustomerId customerId, UserId userId, HttpServletRequest request) throws SobeamException {
        UserCredentials userCredentials = userService.findUserCredentialsByUserId(tenantId, userId);
        if (!userCredentials.isEnabled() && userCredentials.getActivateToken() != null) {
            long ttl = userCredentials.getActivationTokenTtl();
            if (ttl < TimeUnit.MINUTES.toMillis(15)) { // renew link if less than 15 minutes before expiration
                userCredentials = userService.generateUserActivationToken(userCredentials);
                userCredentials = userService.saveUserCredentials(tenantId, userCredentials);
                ttl = userCredentials.getActivationTokenTtl();
                log.debug("[{}][{}] Regenerated expired user activation token", tenantId, userId);
            }
            String baseUrl = systemSecurityService.getBaseUrl(tenantId, customerId, request);
            String link = baseUrl + "/api/noauth/activate?activateToken=" + userCredentials.getActivateToken();
            return new UserActivationLink(link, ttl);
        } else {
            throw new SobeamException("User is already activated!", SobeamErrorCode.BAD_REQUEST_PARAMS);
        }
    }

}
