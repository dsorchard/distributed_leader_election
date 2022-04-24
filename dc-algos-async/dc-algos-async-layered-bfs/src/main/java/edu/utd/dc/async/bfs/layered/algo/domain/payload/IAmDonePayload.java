package edu.utd.dc.async.bfs.layered.algo.domain.payload;

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
