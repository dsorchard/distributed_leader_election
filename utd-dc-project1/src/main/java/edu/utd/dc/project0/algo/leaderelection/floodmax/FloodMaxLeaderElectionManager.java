package edu.utd.dc.project0.algo.leaderelection.floodmax;

import edu.utd.dc.project0.constants.GlobalConstants;
import edu.utd.dc.project0.constants.LogLevel;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.utils.TimeUtils;

import java.util.List;
import java.util.Map;

/**
 * Responsible for : spinning up N Threads of AlgoProcess, Starting a new round and anounicing
 * termination.
 */
public class FloodMaxLeaderElectionManager {

  private final ConfigFileReader configFileReader;

  public FloodMaxLeaderElectionManager(ConfigFileReader configFileReader) {
    this.configFileReader = configFileReader;
  }

  /**
   * Return the max leaderId.
   *
   * @return max leader id.
   */
  public int electLeader() {
    log(LogLevel.DEBUG, configFileReader.toString());

    int n = configFileReader.getSize();
    FloodMaxLeaderElectionSyncActor[] floodMaxProcesses = initProcesses(configFileReader);

    // Start n threads
    Thread[] threads = new Thread[n];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(floodMaxProcesses[i]);
      threads[i].start();
    }

    while (true) {

      TimeUtils.sleep(5_000);

      // Termination check, if no threads are alive, then terminate
      if (isAllThreadsDead(threads)) {
        log(LogLevel.INFO, "Leader Election completed");
        break;
      }

      // Ask processes to start their rounds
      for (FloodMaxLeaderElectionSyncActor process : floodMaxProcesses) {
        process.enableNextRound();
      }
    }

    return 0;
  }

  private boolean isAllThreadsDead(Thread[] threads) {
    for (Thread thread : threads) if (thread.isAlive()) return false;
    return true;
  }

  /**
   * Initializes all the N Threads required for the System.
   *
   * @param configFileReader config
   * @return AlgoProcessArray[n]
   */
  private FloodMaxLeaderElectionSyncActor[] initProcesses(ConfigFileReader configFileReader) {
    int n = configFileReader.getSize();

    FloodMaxLeaderElectionSyncActor[] floodMaxProcesses = new FloodMaxLeaderElectionSyncActor[n];
    for (int i = 0; i < n; i++) {
      ProcessId processId = configFileReader.getProcessIdList().get(i);
      floodMaxProcesses[i] = new FloodMaxLeaderElectionSyncActor(processId);
    }

    for (Map.Entry<Integer, List<Integer>> entry : configFileReader.getAdjList().entrySet()) {
      Integer nodeIdx = entry.getKey();
      for (Integer neighbourIdx : entry.getValue())
        floodMaxProcesses[nodeIdx].addNeighbour(floodMaxProcesses[neighbourIdx]);
    }

    return floodMaxProcesses;
  }

  /** Used for condition logging. Similar to Log4J. */
  private void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
