package edu.utd.dc.async.layered_bfs.algo.domain.payload.ack;

/** If you get IAM done message from your neighbour, then the neighbour is child of the node. */
public class PAckPayload {

  public PAckPayload() {}

  @Override
  public String toString() {
    return "PAckPayload{}";
  }
}