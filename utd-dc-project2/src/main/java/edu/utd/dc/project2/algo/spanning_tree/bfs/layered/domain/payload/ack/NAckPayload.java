package edu.utd.dc.project2.algo.spanning_tree.bfs.layered.domain.payload.ack;

/** If you get Reject from all your neighbours, then the node is a Leaf. */
public class NAckPayload {
  public int maxProcessId;

  public NAckPayload(int processId) {
    this.maxProcessId = processId;
  }

  @Override
  public String toString() {
    return "NAckPayload{}";
  }
}
