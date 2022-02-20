package edu.utd.dc.project0.algo.impl.floodmax;

import edu.utd.dc.project0.constants.GlobalConstants;
import edu.utd.dc.project0.constants.LogLevel;
import edu.utd.dc.project0.core.Process;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.utils.TimeUtils;

import java.util.List;
import java.util.Map;

public class LeaderElectionDriver {

  private final ConfigFileReader configFileReader;

  public LeaderElectionDriver(ConfigFileReader configFileReader) {
    this.configFileReader = configFileReader;
  }

  public int electLeader() {
    log(LogLevel.DEBUG, configFileReader.toString());

    int n = configFileReader.getSize();
    Process[] processes = initProcesses(configFileReader);

    // Start n threads
    Thread[] threads = new Thread[n];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(processes[i]);
      threads[i].start();
    }

    while (true) {

      TimeUtils.sleep(5_000);

      // Termination check, if no threads are alive, then terminate
      if (!isAnyThreadAlive(threads)) {
        log(LogLevel.INFO, "Leader Election completed");
        break;
      }

      // Ask processes to start their rounds
      for (Process process : processes) {
        process.algo.setCanStartNextRound(true);
      }
    }

    return 0;
  }

  private boolean isAnyThreadAlive(Thread[] threads) {
    for (Thread thread : threads) if (thread.isAlive()) return true;
    return false;
  }

  private Process[] initProcesses(ConfigFileReader configFileReader) {
    int n = configFileReader.getSize();

    Process[] processes = new Process[n];
    for (int i = 0; i < n; i++) {
      ProcessId processId = configFileReader.getProcessIdList().get(i);
      processes[i] = new Process(processId, new LeaderElectionFloodMaxRbaAlgo(processId.getID()));
    }

    for (Map.Entry<Integer, List<Integer>> entry : configFileReader.getAdjList().entrySet()) {
      Integer nodeIdx = entry.getKey();
      for (Integer neighbourIdx : entry.getValue())
        processes[nodeIdx].addNeighbour(processes[neighbourIdx]);
    }

    return processes;
  }

  private void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
