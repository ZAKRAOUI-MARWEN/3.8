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
package org.sobeam.server.cache.resourceInfo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.sobeam.server.cache.CacheSpecsMap;
import org.sobeam.server.cache.RedisTbTransactionalCache;
import org.sobeam.server.cache.TBRedisCacheConfiguration;
import org.sobeam.server.cache.TbJsonRedisSerializer;
import org.sobeam.server.common.data.CacheConstants;
import org.sobeam.server.common.data.TbResourceInfo;

@ConditionalOnProperty(prefix = "cache", value = "type", havingValue = "redis")
@Service("ResourceInfoCache")
public class ResourceInfoRedisCache extends RedisTbTransactionalCache<ResourceInfoCacheKey, TbResourceInfo> {

    public ResourceInfoRedisCache(TBRedisCacheConfiguration configuration, CacheSpecsMap cacheSpecsMap, RedisConnectionFactory connectionFactory) {
        super(CacheConstants.RESOURCE_INFO_CACHE, cacheSpecsMap, connectionFactory, configuration, new TbJsonRedisSerializer<>(TbResourceInfo.class));
    }
}
