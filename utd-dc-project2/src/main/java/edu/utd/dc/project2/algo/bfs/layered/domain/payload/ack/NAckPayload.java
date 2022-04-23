package edu.utd.dc.project2.algo.bfs.layered.domain.payload.ack;

/** If you get Reject from all your neighbours, then the node is a Leaf. */
public class NAckPayload {

  public NAckPayload() {}

  @Override
  public String toString() {
    return "NAckPayload{}";
  }
}
