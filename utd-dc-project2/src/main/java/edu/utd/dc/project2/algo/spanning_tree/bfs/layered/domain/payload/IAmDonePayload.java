package edu.utd.dc.project2.algo.spanning_tree.bfs.layered.domain.payload;

public class IAmDonePayload {
  public boolean isNewNodeDiscovered;

  public int maxProcessId;
  public IAmDonePayload(boolean isNewNodeDiscovered, int maxProcessId) {
    this.isNewNodeDiscovered = isNewNodeDiscovered;
    this.maxProcessId = maxProcessId;
  }

  @Override
  public String toString() {
    return "IAmDonePayload{" + "isNewNodeDiscovered=" + isNewNodeDiscovered + '}';
  }
}
