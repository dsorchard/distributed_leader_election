package edu.utd.dc.project0.core.io.sharedmemory;

import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;

public interface Listener {
  void onReceive(Message payload);
}
