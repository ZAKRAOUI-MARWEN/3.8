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
package org.sobeam.server.common.data.sync.vc;

import org.sobeam.server.common.data.EntityType;
import org.sobeam.server.common.data.sync.vc.request.create.AutoVersionCreateConfig;

import java.util.HashMap;

public class AutoCommitSettings extends HashMap<EntityType, AutoVersionCreateConfig> {

    private static final long serialVersionUID = -5757067601838792059L;

}
