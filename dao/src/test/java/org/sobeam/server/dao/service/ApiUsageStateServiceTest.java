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
package org.sobeam.server.dao.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sobeam.server.common.data.ApiUsageState;
import org.sobeam.server.common.data.ApiUsageStateValue;
import org.sobeam.server.dao.usagerecord.ApiUsageStateService;


@DaoSqlTest
public class ApiUsageStateServiceTest extends AbstractServiceTest {

    @Autowired
    ApiUsageStateService apiUsageStateService;

    @Test
    public void testFindApiUsageStateByTenantId() {
        ApiUsageState apiUsageState = apiUsageStateService.findTenantApiUsageState(tenantId);
        Assert.assertNotNull(apiUsageState);
    }

    @Test
    public void testUpdateApiUsageState(){
        ApiUsageState apiUsageState = apiUsageStateService.findTenantApiUsageState(tenantId);
        Assert.assertNotNull(apiUsageState);
        Assert.assertTrue(apiUsageState.isTransportEnabled());
        apiUsageState.setTransportState(ApiUsageStateValue.DISABLED);
        apiUsageState = apiUsageStateService.update(apiUsageState);
        Assert.assertNotNull(apiUsageState);
        apiUsageState = apiUsageStateService.findTenantApiUsageState(tenantId);
        Assert.assertNotNull(apiUsageState);
        Assert.assertFalse(apiUsageState.isTransportEnabled());
    }

}
