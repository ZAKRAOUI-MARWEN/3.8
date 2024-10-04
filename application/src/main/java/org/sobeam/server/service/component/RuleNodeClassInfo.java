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
package org.sobeam.server.service.component;

import lombok.Data;
import org.sobeam.rule.engine.api.RuleNode;

@Data
public class RuleNodeClassInfo {

    private final Class<?> clazz;
    private final RuleNode annotation;

    public String getClassName(){
        return clazz.getName();
    }

    public String getSimpleName() {
        return clazz.getSimpleName();
    }

    public int getCurrentVersion() {
        return annotation.version();
    }

    public boolean isVersioned() {
        return annotation.version() > 0;
    }

}
