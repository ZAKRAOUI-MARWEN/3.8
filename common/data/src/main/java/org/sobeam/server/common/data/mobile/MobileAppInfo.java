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
package org.sobeam.server.common.data.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.sobeam.server.common.data.id.MobileAppId;
import org.sobeam.server.common.data.oauth2.OAuth2ClientInfo;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema
public class MobileAppInfo extends MobileApp {

    @Schema(description = "List of available oauth2 clients")
    private List<OAuth2ClientInfo> oauth2ClientInfos;

    public MobileAppInfo(MobileApp mobileApp, List<OAuth2ClientInfo> oauth2ClientInfos) {
        super(mobileApp);
        this.oauth2ClientInfos = oauth2ClientInfos;
    }

    public MobileAppInfo() {
        super();
    }

    public MobileAppInfo(MobileAppId mobileAppId) {
        super(mobileAppId);
    }

}
