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
package org.sobeam.server.dao.sql;

import org.sobeam.server.dao.model.BaseEntity;
import org.sobeam.server.dao.util.SqlDao;

@SqlDao
public abstract class JpaPartitionedAbstractDao<E extends BaseEntity<D>, D> extends JpaAbstractDao<E, D> {

    @Override
    protected E doSave(E entity, boolean isNew, boolean flush) {
        createPartition(entity);
        return super.doSave(entity, isNew, flush);
    }

    public abstract void createPartition(E entity);

}
