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
package org.sobeam.script.api.tbel;

import com.google.common.util.concurrent.ListenableFuture;
import org.mvel2.ExecutionContext;
import org.sobeam.script.api.TbScriptExecutionTask;


public class TbelScriptExecutionTask extends TbScriptExecutionTask {

    private final ExecutionContext context;

    public TbelScriptExecutionTask(ExecutionContext context, ListenableFuture<Object> resultFuture) {
        super(resultFuture);
        this.context = context;
    }

    @Override
    public void stop(){
        context.stop();
    }
}
