package edu.utd.dc.common.domain.io;

import edu.utd.dc.common.domain.support.ProcessId;

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
