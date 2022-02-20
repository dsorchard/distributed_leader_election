package edu.utd.dc.project0.io.sharedmemory.domain;

import edu.utd.dc.project0.core.support.ProcessId;

public class Message {

  public ProcessId source;
  public Object data;

  public Message(ProcessId source, Object data) {
    this.source = source;
    this.data = data;
  }
}
