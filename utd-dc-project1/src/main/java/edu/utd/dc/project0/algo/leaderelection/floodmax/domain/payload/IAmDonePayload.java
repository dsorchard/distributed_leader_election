package edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload;

public class IAmDonePayload {
  public int maxId;

  public IAmDonePayload(int maxId) {
    this.maxId = maxId;
  }

  @Override
  public String toString() {
    return "TokenPayload{" + "maxId=" + maxId + '}';
  }
}
