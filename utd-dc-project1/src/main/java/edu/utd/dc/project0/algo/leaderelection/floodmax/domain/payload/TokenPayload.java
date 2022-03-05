package edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload;

public class TokenPayload {
  public int leaderId;

  public TokenPayload(int leaderId) {
    this.leaderId = leaderId;
  }

  @Override
  public String toString() {
    return "TokenPayload{" + "leaderId=" + leaderId + '}';
  }
}
