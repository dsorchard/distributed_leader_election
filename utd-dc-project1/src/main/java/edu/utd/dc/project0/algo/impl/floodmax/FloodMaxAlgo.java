package edu.utd.dc.project0.algo.impl.floodmax;

import edu.utd.dc.project0.algo.RBAAlgo;

public class FloodMaxAlgo implements RBAAlgo {

  public boolean canStartRound;
  public boolean isTerminated;

  public int maxId;

  public FloodMaxAlgo(int maxId) {
    this.isTerminated = false;
    this.canStartRound = false;
    this.maxId = maxId;
  }

  @Override
  public void init() {
    while (!isTerminated) {
      if (canStartRound) nextRound();
      canStartRound = false;
    }
  }

  @Override
  public void setCanStartNextRound(boolean canStartRound) {
    this.canStartRound = canStartRound;
  }

  @Override
  public void nextRound() {}
}
