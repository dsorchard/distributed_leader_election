package edu.utd.dc.synch.core.io.sharedmemory;

import edu.utd.dc.synch.core.io.sharedmemory.domain.Message;

/** Used by {@link edu.utd.dc.synch.core.SyncProcess} to enable Observer nature. */
public interface Listener {
  void onReceive(Message payload);
}
