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
package org.sobeam.server.transport.lwm2m.server.ota.software;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sobeam.server.common.data.ota.OtaPackageType;
import org.sobeam.server.transport.lwm2m.server.ota.LwM2MClientOtaInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class LwM2MClientSwOtaInfo extends LwM2MClientOtaInfo<LwM2MSoftwareUpdateStrategy, SoftwareUpdateState, SoftwareUpdateResult> {

    public LwM2MClientSwOtaInfo(String endpoint, String baseUrl, LwM2MSoftwareUpdateStrategy strategy) {
        super(endpoint, baseUrl, strategy);
    }

    @JsonIgnore
    @Override
    public OtaPackageType getType() {
        return OtaPackageType.SOFTWARE;
    }


    public void update(SoftwareUpdateResult result) {
        this.result = result;
        switch (result) {
            case INITIAL:
                break;
                //TODO: implement
            default:
                failedPackageId = getPackageId(targetName, targetVersion);
                break;
        }
    }

}
