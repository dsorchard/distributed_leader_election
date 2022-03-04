package edu.utd.dc.project0.algo.leaderelection.lcr.domain.payload;

public class TokenPayload {
  public int maxId;

  public TokenPayload(int maxId) {
    this.maxId = maxId;
  }

  @Override
  public String toString() {
    return "TokenPayload{" + "maxId=" + maxId + '}';
  }
}
