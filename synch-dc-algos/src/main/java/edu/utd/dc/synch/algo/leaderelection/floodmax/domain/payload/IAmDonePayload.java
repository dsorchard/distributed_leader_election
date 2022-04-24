package edu.utd.dc.synch.algo.leaderelection.floodmax.domain.payload;

/** If you get IAM done message from your neighbour, then the neighbour is child of the node. */
public class IAmDonePayload {

  public IAmDonePayload() {}

  @Override
  public String toString() {
    return "IAmDonePayload{}";
  }
}
