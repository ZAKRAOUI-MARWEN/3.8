///
/// Copyright © 2024 The Sobeam Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

export enum ApiUsageStateValue {
  ENABLED = 'ENABLED',
  WARNING = 'WARNING',
  DISABLED = 'DISABLED'
}

export const ApiUsageStateValueTranslationMap = new Map<ApiUsageStateValue, string>([
  [ApiUsageStateValue.ENABLED, 'notification.enabled'],
  [ApiUsageStateValue.WARNING, 'notification.warning'],
  [ApiUsageStateValue.DISABLED, 'notification.disabled'],
]);

export enum ApiFeature {
  TRANSPORT = 'TRANSPORT',
  DB = 'DB',
  RE = 'RE',
  JS = 'JS',
  TBEL = 'TBEL',
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  ALARM = 'ALARM'
}

export const ApiFeatureTranslationMap = new Map<ApiFeature, string>([
  [ApiFeature.TRANSPORT, 'api-usage.device-api'],
  [ApiFeature.DB, 'api-usage.telemetry-persistence'],
  [ApiFeature.RE, 'api-usage.rule-engine-executions'],
  [ApiFeature.JS, 'api-usage.javascript-executions'],
  [ApiFeature.TBEL, 'api-usage.tbel-executions'],
  [ApiFeature.EMAIL, 'api-usage.email-messages'],
  [ApiFeature.SMS, 'api-usage.sms-messages'],
  [ApiFeature.ALARM, 'api-usage.alarm'],
]);
