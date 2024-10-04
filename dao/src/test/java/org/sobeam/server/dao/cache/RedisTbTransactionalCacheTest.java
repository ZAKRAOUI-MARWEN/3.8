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
package org.sobeam.server.dao.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.sobeam.server.cache.CacheSpecsMap;
import org.sobeam.server.cache.RedisSslCredentials;
import org.sobeam.server.cache.TBRedisCacheConfiguration;
import org.sobeam.server.common.data.id.DeviceId;
import org.sobeam.server.common.data.relation.RelationTypeGroup;
import org.sobeam.server.dao.relation.RelationCacheKey;
import org.sobeam.server.dao.relation.RelationRedisCache;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RelationRedisCache.class, CacheSpecsMap.class, TBRedisCacheConfiguration.class})
@TestPropertySource(properties = {
        "cache.type=redis",
        "cache.specs.relations.timeToLiveInMinutes=1440",
        "cache.specs.relations.maxSize=0",
})
@Slf4j
public class RedisTbTransactionalCacheTest {

    @MockBean
    private RelationRedisCache relationRedisCache;
    @MockBean
    private RedisConnectionFactory connectionFactory;
    @MockBean
    private RedisConnection redisConnection;
    @MockBean
    private RedisSslCredentials redisSslCredentials;

    @Test
    public void testNoOpWhenCacheDisabled() {
        when(connectionFactory.getConnection()).thenReturn(redisConnection);

        relationRedisCache.put(createRelationCacheKey(), null);
        relationRedisCache.putIfAbsent(createRelationCacheKey(), null);
        relationRedisCache.evict(createRelationCacheKey());
        relationRedisCache.evict(List.of(createRelationCacheKey()));
        relationRedisCache.getAndPutInTransaction(createRelationCacheKey(), null, false);
        relationRedisCache.getAndPutInTransaction(createRelationCacheKey(), null, null, null, false);
        relationRedisCache.getOrFetchFromDB(createRelationCacheKey(), null, false, false);

        verify(connectionFactory, never()).getConnection();
        verifyNoInteractions(redisConnection);
    }

    private RelationCacheKey createRelationCacheKey() {
        return new RelationCacheKey(new DeviceId(UUID.randomUUID()), new DeviceId(UUID.randomUUID()), null, RelationTypeGroup.COMMON);
    }

}
