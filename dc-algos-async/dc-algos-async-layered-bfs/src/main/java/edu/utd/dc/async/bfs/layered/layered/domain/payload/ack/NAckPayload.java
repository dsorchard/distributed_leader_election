package edu.utd.dc.async.bfs.layered.layered.domain.payload.ack;

/** If you get Reject from all your neighbours, then the node is a Leaf. */
public class NAckPayload {

  public NAckPayload() {}

  @Override
  public String toString() {
    return "NAckPayload{}";
  }
}
