package edu.utd.dc.project0.algo.leaderelection.floodmax;

import edu.utd.dc.project0.core.SyncProcess;
import edu.utd.dc.project0.core.support.ProcessId;

public class LCRLeaderElectionSyncProcess extends SyncProcess {

    public LCRLeaderElectionSyncProcess(ProcessId processId) {
        super(processId);
    }

    @Override
    protected void handlePreRound(int roundNumber) {

    }

    @Override
    protected void handleOutgoing() {

    }

    @Override
    protected void handleIncoming() {

    }
}
