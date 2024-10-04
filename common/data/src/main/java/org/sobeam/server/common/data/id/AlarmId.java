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
package org.sobeam.server.common.data.id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.sobeam.server.common.data.EntityType;

import java.util.UUID;

@Schema
public class AlarmId extends UUIDBased implements EntityId {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public AlarmId(@JsonProperty("id") UUID id) {
        super(id);
    }

    public static AlarmId fromString(String alarmId) {
        return new AlarmId(UUID.fromString(alarmId));
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "string", example = "ALARM", allowableValues = "ALARM")
    @Override
    public EntityType getEntityType() {
        return EntityType.ALARM;
    }
}
