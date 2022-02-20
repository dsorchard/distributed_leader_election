package edu.utd.dc.project0.algo.impl.floodmax;

import edu.utd.dc.project0.algo.RbaAlgo;

public class LeaderElectionFloodMaxRbaAlgo implements RbaAlgo {

  public boolean canStartRound;
  public boolean isTerminated;

  public int maxId;

  public LeaderElectionFloodMaxRbaAlgo(int maxId) {
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
