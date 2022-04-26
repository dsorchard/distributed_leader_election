package edu.utd.dc.async.layered_bfs.algo.domain.payload;

/**
 * IAM Done Payload which converge cast to the root, with isNewNodeDiscovered & maxProcessId
 * information
 */
public class IAmDonePayload {
  public boolean isNewNodeDiscovered;

  public int maxProcessId;

  public IAmDonePayload(boolean isNewNodeDiscovered, int maxProcessId) {
    this.isNewNodeDiscovered = isNewNodeDiscovered;
    this.maxProcessId = maxProcessId;
  }

  @Override
  public String toString() {
    return "IAmDonePayload{"
        + "isNewNodeDiscovered="
        + isNewNodeDiscovered
        + ", maxProcessId="
        + maxProcessId
        + '}';
  }
}
