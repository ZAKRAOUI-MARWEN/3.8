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
package org.sobeam.server.cache.customer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.sobeam.server.cache.CaffeineTbTransactionalCache;
import org.sobeam.server.common.data.CacheConstants;
import org.sobeam.server.common.data.Customer;

@ConditionalOnProperty(prefix = "cache", value = "type", havingValue = "caffeine", matchIfMissing = true)
@Service("CustomerCache")
public class CustomerCaffeineCache extends CaffeineTbTransactionalCache<CustomerCacheKey, Customer> {

    public CustomerCaffeineCache(CacheManager cacheManager) {
        super(cacheManager, CacheConstants.CUSTOMER_CACHE);
    }

}
