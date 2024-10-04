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
package org.sobeam.rule.engine.sms;

import lombok.Data;
import org.sobeam.rule.engine.api.NodeConfiguration;
import org.sobeam.server.common.data.sms.config.SmsProviderConfiguration;

@Data
public class TbSendSmsNodeConfiguration implements NodeConfiguration {

    private String numbersToTemplate;
    private String smsMessageTemplate;
    private boolean useSystemSmsSettings;
    private SmsProviderConfiguration smsProviderConfiguration;

    @Override
    public NodeConfiguration defaultConfiguration() {
        TbSendSmsNodeConfiguration configuration = new TbSendSmsNodeConfiguration();
        configuration.numbersToTemplate = "${userPhone}";
        configuration.smsMessageTemplate = "Device ${deviceName} has high temperature ${temp}";
        configuration.setUseSystemSmsSettings(true);
        return configuration;
    }
}
