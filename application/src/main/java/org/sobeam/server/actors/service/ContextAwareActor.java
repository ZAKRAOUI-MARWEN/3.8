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
package org.sobeam.server.actors.service;

import lombok.extern.slf4j.Slf4j;
import org.sobeam.server.actors.AbstractTbActor;
import org.sobeam.server.actors.ActorSystemContext;
import org.sobeam.server.actors.ProcessFailureStrategy;
import org.sobeam.server.common.msg.TbActorMsg;

@Slf4j
public abstract class ContextAwareActor extends AbstractTbActor {

    public static final int ENTITY_PACK_LIMIT = 1024;

    protected final ActorSystemContext systemContext;

    public ContextAwareActor(ActorSystemContext systemContext) {
        super();
        this.systemContext = systemContext;
    }

    @Override
    public boolean process(TbActorMsg msg) {
        if (log.isDebugEnabled()) {
            log.debug("Processing msg: {}", msg);
        }
        if (!doProcess(msg)) {
            log.warn("Unprocessed message: {}!", msg);
        }
        return false;
    }

    protected abstract boolean doProcess(TbActorMsg msg);

    @Override
    public ProcessFailureStrategy onProcessFailure(TbActorMsg msg, Throwable t) {
        log.debug("[{}] Processing failure for msg {}", getActorRef().getActorId(), msg, t);
        return doProcessFailure(t);
    }

    protected ProcessFailureStrategy doProcessFailure(Throwable t) {
        if (t instanceof Error) {
            return ProcessFailureStrategy.stop();
        } else {
            return ProcessFailureStrategy.resume();
        }
    }
}
