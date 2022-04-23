package edu.utd.dc.project2.algo.bfs.layered.domain.payload;

public class IAmDonePayload {
  public boolean isNewNodeDiscovered;

  public IAmDonePayload(boolean isNewNodeDiscovered) {
    this.isNewNodeDiscovered = isNewNodeDiscovered;
  }

  @Override
  public String toString() {
    return "IAmDonePayload{" + "isNewNodeDiscovered=" + isNewNodeDiscovered + '}';
  }
}