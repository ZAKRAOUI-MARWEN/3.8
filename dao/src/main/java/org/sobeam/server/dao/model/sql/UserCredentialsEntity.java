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
package org.sobeam.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.sobeam.server.common.data.id.UserCredentialsId;
import org.sobeam.server.common.data.id.UserId;
import org.sobeam.server.common.data.security.UserCredentials;
import org.sobeam.server.dao.model.BaseEntity;
import org.sobeam.server.dao.model.BaseSqlEntity;
import org.sobeam.server.dao.model.ModelConstants;
import org.sobeam.server.dao.util.mapping.JsonConverter;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.USER_CREDENTIALS_TABLE_NAME)
public final class UserCredentialsEntity extends BaseSqlEntity<UserCredentials> implements BaseEntity<UserCredentials> {

    @Column(name = ModelConstants.USER_CREDENTIALS_USER_ID_PROPERTY, unique = true)
    private UUID userId;

    @Column(name = ModelConstants.USER_CREDENTIALS_ENABLED_PROPERTY)
    private boolean enabled;

    @Column(name = ModelConstants.USER_CREDENTIALS_PASSWORD_PROPERTY)
    private String password;

    @Column(name = ModelConstants.USER_CREDENTIALS_ACTIVATE_TOKEN_PROPERTY, unique = true)
    private String activateToken;

    @Column(name = ModelConstants.USER_CREDENTIALS_ACTIVATE_TOKEN_EXP_TIME_PROPERTY)
    private Long activateTokenExpTime;

    @Column(name = ModelConstants.USER_CREDENTIALS_RESET_TOKEN_PROPERTY, unique = true)
    private String resetToken;

    @Column(name = ModelConstants.USER_CREDENTIALS_RESET_TOKEN_EXP_TIME_PROPERTY)
    private Long resetTokenExpTime;

    @Convert(converter = JsonConverter.class)
    @Column(name = ModelConstants.USER_CREDENTIALS_ADDITIONAL_PROPERTY)
    private JsonNode additionalInfo;

    public UserCredentialsEntity() {
        super();
    }

    public UserCredentialsEntity(UserCredentials userCredentials) {
        if (userCredentials.getId() != null) {
            this.setUuid(userCredentials.getId().getId());
        }
        this.setCreatedTime(userCredentials.getCreatedTime());
        if (userCredentials.getUserId() != null) {
            this.userId = userCredentials.getUserId().getId();
        }
        this.enabled = userCredentials.isEnabled();
        this.password = userCredentials.getPassword();
        this.activateToken = userCredentials.getActivateToken();
        this.activateTokenExpTime = userCredentials.getActivateTokenExpTime();
        this.resetToken = userCredentials.getResetToken();
        this.resetTokenExpTime = userCredentials.getResetTokenExpTime();
        this.additionalInfo = userCredentials.getAdditionalInfo();
    }

    @Override
    public UserCredentials toData() {
        UserCredentials userCredentials = new UserCredentials(new UserCredentialsId(this.getUuid()));
        userCredentials.setCreatedTime(createdTime);
        if (userId != null) {
            userCredentials.setUserId(new UserId(userId));
        }
        userCredentials.setEnabled(enabled);
        userCredentials.setPassword(password);
        userCredentials.setActivateToken(activateToken);
        userCredentials.setActivateTokenExpTime(activateTokenExpTime);
        userCredentials.setResetToken(resetToken);
        userCredentials.setResetTokenExpTime(resetTokenExpTime);
        userCredentials.setAdditionalInfo(additionalInfo);
        return userCredentials;
    }

}
