package edu.utd.dc.project0.core.io.sharedmemory.domain;

import edu.utd.dc.project0.core.support.ProcessId;

public class Message {

  public ProcessId _source;
  public Object data;

  public Message(ProcessId source, Object data) {
    this._source = source;
    this.data = data;
  }
}
