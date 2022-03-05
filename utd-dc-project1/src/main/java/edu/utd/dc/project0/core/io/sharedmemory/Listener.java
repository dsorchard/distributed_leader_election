package edu.utd.dc.project0.core.io.sharedmemory;

import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;

/** Used by {@link edu.utd.dc.project0.core.SyncProcess} to enable Observer nature. */
public interface Listener {
  void onReceive(Message payload);
}
