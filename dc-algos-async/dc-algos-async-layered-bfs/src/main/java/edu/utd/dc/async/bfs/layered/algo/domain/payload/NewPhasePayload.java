package edu.utd.dc.async.bfs.layered.algo.domain.payload;

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
