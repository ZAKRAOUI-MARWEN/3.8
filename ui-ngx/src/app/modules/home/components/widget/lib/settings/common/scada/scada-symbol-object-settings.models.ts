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

import {
  ScadaSymbolBehavior,
  ScadaSymbolProperty,
  ScadaSymbolPropertyType
} from '@home/components/widget/lib/scada/scada-symbol.models';

export interface ScadaSymbolBehaviorGroup {
  title?: string;
  behaviors: ScadaSymbolBehavior[];
}

export interface ScadaSymbolPropertyRow {
  label: string;
  properties: ScadaSymbolProperty[];
  switch?: ScadaSymbolProperty;
  rowClass?: string;
}

export const toBehaviorGroups = (behaviors: ScadaSymbolBehavior[]): ScadaSymbolBehaviorGroup[] => {
  const result: ScadaSymbolBehaviorGroup[] = [];
  for (const behavior of behaviors) {
    if (!behavior.group) {
      result.push({
        title: null,
        behaviors: [behavior]
      });
    } else {
      let behaviorGroup = result.find(g => g.title === behavior.group);
      if (!behaviorGroup) {
        behaviorGroup = {
          title: behavior.group,
          behaviors: []
        };
        result.push(behaviorGroup);
      }
      behaviorGroup.behaviors.push(behavior);
    }
  }
  return result;
};

export const toPropertyRows = (properties: ScadaSymbolProperty[]): ScadaSymbolPropertyRow[] => {
  const result: ScadaSymbolPropertyRow[] = [];
  for (const property of properties) {
    let propertyRow = result.find(r => r.label === property.name);
    if (!propertyRow) {
      propertyRow = {
        label: property.name,
        properties: [],
        rowClass: property.rowClass
      };
      result.push(propertyRow);
    }
    if (property.type === ScadaSymbolPropertyType.switch) {
      propertyRow.switch = property;
    } else {
      propertyRow.properties.push(property);
    }
  }
  return result;
};
