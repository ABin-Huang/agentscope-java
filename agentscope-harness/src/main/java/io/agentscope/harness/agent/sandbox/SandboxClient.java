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

import io.agentscope.harness.agent.sandbox.snapshot.RemoteSandboxSnapshot;
import io.agentscope.harness.agent.sandbox.snapshot.RemoteSnapshotSpec;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshot;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec;

/**
 * Factory for creating and resuming {@link Sandbox} instances.
 *
 * @param <O> the type of client options for this implementation
 */
public interface SandboxClient<O extends SandboxClientOptions> {

    /**
     * Creates a new sandbox with the given workspace spec and snapshot spec.
     *
     * <p>Returned in a pre-start state; call {@link Sandbox#start()} before use.
     */
    Sandbox create(WorkspaceSpec workspaceSpec, SandboxSnapshotSpec snapshotSpec, O options);

    /**
     * Resumes a sandbox from previously serialized {@link SandboxState}.
     */
    Sandbox resume(SandboxState state);

    void delete(Sandbox sandbox);

    String serializeState(SandboxState state);

    SandboxState deserializeState(String json);

    /**
     * Deserializes sandbox state from JSON and re-binds the {@link RemoteSnapshotClient}
     * from the given {@link SandboxSnapshotSpec} when the state contains a
     * {@link RemoteSandboxSnapshot}.
     *
     * <p>{@link RemoteSandboxSnapshot} is serialized with only its {@code id} (the client
     * is {@code @JsonIgnore}), so the client must be re-injected from the snapshot spec
     * at resume time. This default method performs that re-binding generically, so all
     * {@link SandboxClient} implementations automatically support remote snapshots
     * without needing to override this method.
     *
     * @param json the serialized sandbox state JSON
     * @param snapshotSpec the snapshot spec used when the sandbox was created; may be
     *        {@code null} (in which case no re-binding occurs)
     * @return the deserialized state with the remote snapshot client re-bound if applicable
     */
    default SandboxState deserializeState(String json, SandboxSnapshotSpec snapshotSpec) {
        SandboxState state = deserializeState(json);
        rebindRemoteSnapshot(state, snapshotSpec);
        return state;
    }

    /**
     * Re-binds the {@link RemoteSnapshotClient} into the {@link RemoteSandboxSnapshot}
     * held by the given state, using the client from the {@link RemoteSnapshotSpec}.
     *
     * <p>This is called automatically by the default
     * {@link #deserializeState(String, SandboxSnapshotSpec)} method. Implementations that
     * override that method should call this helper to preserve remote snapshot support.
     *
     * @param state the deserialized sandbox state
     * @param snapshotSpec the snapshot spec that was used to create the sandbox
     */
    static void rebindRemoteSnapshot(SandboxState state, SandboxSnapshotSpec snapshotSpec) {
        if (!(snapshotSpec instanceof RemoteSnapshotSpec remoteSnapshotSpec)) {
            return;
        }
        SandboxSnapshot snapshot = state.getSnapshot();
        if (!(snapshot instanceof RemoteSandboxSnapshot)) {
            return;
        }
        state.setSnapshot(
                new RemoteSandboxSnapshot(remoteSnapshotSpec.getClient(), snapshot.getId()));
    }
}
