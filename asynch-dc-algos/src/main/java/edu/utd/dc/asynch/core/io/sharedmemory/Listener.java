package edu.utd.dc.asynch.core.io.sharedmemory;

import edu.utd.dc.asynch.core.io.sharedmemory.domain.Message;

/** Used by {@link edu.utd.dc.synch.core.SyncProcess} to enable Observer nature. */
public interface Listener {
  void onReceive(Message payload);
}
