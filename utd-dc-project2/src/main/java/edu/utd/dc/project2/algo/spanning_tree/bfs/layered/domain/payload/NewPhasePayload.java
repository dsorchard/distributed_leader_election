package edu.utd.dc.project2.algo.spanning_tree.bfs.layered.domain.payload;

public class NewPhasePayload {

  public int depth;

  public NewPhasePayload(int depth) {
    this.depth = depth;
  }

  @Override
  public String toString() {
    return "NewPhasePayload{" + "depth=" + depth + '}';
  }
}
