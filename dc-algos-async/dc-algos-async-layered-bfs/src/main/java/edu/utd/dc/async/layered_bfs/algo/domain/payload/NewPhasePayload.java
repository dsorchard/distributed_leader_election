package edu.utd.dc.async.layered_bfs.algo.domain.payload;

/**
 * New Phase message send by root, after phase k ( k>1) to the k'th depth children, to start there
 * neighbour exploration. Contains a decrementing depth coutn.
 */
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
