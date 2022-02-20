package edu.utd.dc.project0.algo;

import edu.utd.dc.project0.core.Process;

public class FloodMaxAlgo {

  private final Process process;
  public boolean canStartRound;
  public boolean isTerminated;

  public int maxId;

  public FloodMaxAlgo(Process process) {

    this.process = process;
    this.canStartRound = false;
    this.isTerminated = false;

    this.maxId = process.processId.getID();
  }

  public void elect() {
    while (!isTerminated) {
      if (canStartRound) nextRound();
      canStartRound = false;
    }
  }

  public void setCanStartRound(boolean canStartRound) {
    this.canStartRound = canStartRound;
  }

  private void nextRound() {}
}
