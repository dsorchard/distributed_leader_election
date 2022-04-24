package edu.utd.dc.asynch.core.io.sharedmemory.domain;

import edu.utd.dc.asynch.core.support.ProcessId;

/** Message is transferred along the network. Contains _source metadata and payload. */
public class Message {

  public ProcessId _source;
  public Object payload;

  public Message(ProcessId source, Object payload) {
    this._source = source;
    this.payload = payload;
  }

  @Override
  public String toString() {
    return "Message{" + "_source=" + _source + ", payload=" + payload + '}';
  }
}
