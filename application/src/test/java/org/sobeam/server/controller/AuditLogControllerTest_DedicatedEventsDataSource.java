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
package org.sobeam.server.controller;

import lombok.Getter;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.sobeam.server.dao.service.DaoSqlTest;
import org.sobeam.server.dao.sqlts.insert.sql.DedicatedEventsSqlPartitioningRepository;

@DaoSqlTest
@TestPropertySource(properties = {
        "spring.datasource.events.enabled=true",
        "spring.datasource.events.url=${spring.datasource.url}",
        "spring.datasource.events.driverClassName=${spring.datasource.driverClassName}",
})
public class AuditLogControllerTest_DedicatedEventsDataSource extends AuditLogControllerTest {

    @Getter
    @SpyBean
    private DedicatedEventsSqlPartitioningRepository partitioningRepository;

}
