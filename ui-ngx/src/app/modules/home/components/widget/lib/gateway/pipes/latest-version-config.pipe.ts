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

import { Pipe, PipeTransform } from '@angular/core';
import { GatewayVersion } from '@home/components/widget/lib/gateway/gateway-widget.models';
import {
  GatewayConnectorVersionMappingUtil
} from '@home/components/widget/lib/gateway/utils/gateway-connector-version-mapping.util';

@Pipe({
  name: 'isLatestVersionConfig',
  standalone: true,
})
export class LatestVersionConfigPipe implements PipeTransform {
  transform(configVersion: number | string): boolean {
    return GatewayConnectorVersionMappingUtil.parseVersion(configVersion)
      >= GatewayConnectorVersionMappingUtil.parseVersion(GatewayVersion.Current);
  }
}
