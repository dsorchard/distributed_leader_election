package edu.utd.dc.project2.algo.leaderelection.floodmax.domain.payload;

/** If you get Reject from all your neighbours, then the node is a Leaf. */
public class RejectPayload {

  public RejectPayload() {}

  @Override
  public String toString() {
    return "RejectPayload{}";
  }
}
