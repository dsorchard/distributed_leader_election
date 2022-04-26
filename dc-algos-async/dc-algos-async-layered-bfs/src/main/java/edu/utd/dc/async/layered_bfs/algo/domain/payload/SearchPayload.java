package edu.utd.dc.async.layered_bfs.algo.domain.payload;

/** Search Message send from the tip of k depth children to explore their neighbours. */
public class SearchPayload {

  public SearchPayload() {}

  @Override
  public String toString() {
    return "SearchPayload{}";
  }
}
