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
package org.sobeam.server.common.data;

import lombok.Data;
import org.sobeam.server.common.data.id.TenantId;

import java.io.Serializable;

@Data
public class EntitySubtype implements Serializable {

    private static final long serialVersionUID = 8057240243059922101L;

    private final TenantId tenantId;
    private final EntityType entityType;
    private final String type;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EntitySubtype{");
        sb.append("tenantId=").append(tenantId);
        sb.append(", entityType=").append(entityType);
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
