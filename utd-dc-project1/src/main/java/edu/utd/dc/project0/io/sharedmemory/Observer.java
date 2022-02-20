package edu.utd.dc.project0.io.sharedmemory;

import edu.utd.dc.project0.io.sharedmemory.domain.Message;

public interface Observer {

  void onReceive(Message payload);
}
