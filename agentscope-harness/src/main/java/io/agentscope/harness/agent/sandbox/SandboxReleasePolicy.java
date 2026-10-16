/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.harness.agent.sandbox;

/**
 * Controls how self-managed sandboxes are released after each agent call.
 *
 * <p>The default {@link #DELETE} behavior destroys the sandbox instance after every call,
 * which is safe but slow for remote sandboxes (E2B, Daytona, etc.) because each subsequent
 * call must recreate the sandbox and restore the workspace from snapshot.
 *
 * <p>With {@link #RETAIN}, the sandbox instance is kept alive between calls. The next call
 * will reconnect to the existing sandbox via the persisted state, which is dramatically faster
 * for remote backends. The platform's own idle timeout will eventually reclaim unused sandboxes.
 */
public enum SandboxReleasePolicy {

    /**
     * Default behavior: call {@code stop()} (persist snapshot) then {@code shutdown()}
     * (destroy backend resources). The sandbox instance is deleted after each call.
     */
    DELETE,

    /**
     * Call {@code stop()} (persist snapshot) but skip {@code shutdown()}. The sandbox
     * instance remains alive and will be reused on the next call via the persisted state.
     *
     * <p>This is a performance optimization for remote sandboxes where recreation is expensive.
     * The sandbox will eventually be reclaimed by the platform's idle timeout.
     */
    RETAIN
}
