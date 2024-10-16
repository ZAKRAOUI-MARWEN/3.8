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
package org.sobeam.server.actors.shared;

import lombok.extern.slf4j.Slf4j;
import org.sobeam.server.actors.ActorSystemContext;
import org.sobeam.server.actors.TbActorCtx;
import org.sobeam.server.common.msg.TbActorMsg;

import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public abstract class AbstractContextAwareMsgProcessor {

    protected final ActorSystemContext systemContext;

    protected AbstractContextAwareMsgProcessor(ActorSystemContext systemContext) {
        super();
        this.systemContext = systemContext;
    }

    private ScheduledExecutorService getScheduler() {
        return systemContext.getScheduler();
    }

    protected void schedulePeriodicMsgWithDelay(TbActorCtx ctx, TbActorMsg msg, long delayInMs, long periodInMs) {
        systemContext.schedulePeriodicMsgWithDelay(ctx, msg, delayInMs, periodInMs);
    }

    protected void scheduleMsgWithDelay(TbActorCtx ctx, TbActorMsg msg, long delayInMs) {
        systemContext.scheduleMsgWithDelay(ctx, msg, delayInMs);
    }

}
