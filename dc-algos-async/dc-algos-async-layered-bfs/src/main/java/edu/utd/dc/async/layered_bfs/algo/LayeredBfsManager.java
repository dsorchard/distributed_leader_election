package edu.utd.dc.async.layered_bfs.algo;

import edu.utd.dc.async.layered_bfs.constants.GlobalConstants;
import edu.utd.dc.async.layered_bfs.io.file.ConfigFileReader;
import edu.utd.dc.async.core.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.common.constants.LogLevel;
import edu.utd.dc.common.domain.support.ProcessId;
import edu.utd.dc.common.utils.TimeUtils;

import java.util.List;
import java.util.Map;

/**
 * Responsible for : spinning up N Threads of AlgoProcess, Starts a new round until every thread
 * sleeps.
 */
public class LayeredBfsManager {

  private final ConfigFileReader configFileReader;

  public LayeredBfsManager(ConfigFileReader configFileReader) {
    this.configFileReader = configFileReader;
  }

  /**
   * Spins up N threads, executed FloodMaxLeader Election and return maxId.
   *
   * @return max leader id.
   */
  public int buildBFSTreeAndReturnLeader() {
    log(LogLevel.DEBUG, configFileReader.toString());

    int n = configFileReader.getSize();
    LayeredBfsASyncProcess[] layeredBfsProcesses = initProcesses(configFileReader);

    // Start n threads
    Thread[] threads = new Thread[n];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(layeredBfsProcesses[i]);
      threads[i].start();
    }

    // Initiate algorithm from root.
    int root = configFileReader.getRoot();
    LayeredBfsASyncProcess rootProcess = layeredBfsProcesses[root];
    rootProcess.initiate();

    // Endless while loop
    while (true) {

      TimeUtils.sleep(500);

      // Termination check, if no threads are alive, then terminate
      if (isAllThreadsDead(threads)) {
        log(LogLevel.DEBUG, "Layered BFS completed");
        break;
      }

      // Tick
      SharedMemoryBus.tick();
    }

    return rootProcess.getLeaderId();
  }

  private boolean isAllThreadsDead(Thread[] threads) {
    for (Thread thread : threads) if (thread.isAlive()) return false;
    return true;
  }

  /**
   * Initializes all the N Runnable Processes required for the System.
   *
   * @param configFileReader config
   * @return AlgoProcessArray[n]
   */
  private LayeredBfsASyncProcess[] initProcesses(ConfigFileReader configFileReader) {
    int n = configFileReader.getSize();

    // instantiate array
    LayeredBfsASyncProcess[] layeredBfsProcesses = new LayeredBfsASyncProcess[n];

    // initialize values
    for (int i = 0; i < n; i++) {
      ProcessId processId = configFileReader.getProcessIdList().get(i);
      layeredBfsProcesses[i] = new LayeredBfsASyncProcess(processId);
    }

    // connect edges
    for (Map.Entry<Integer, List<Integer>> entry : configFileReader.getAdjList().entrySet()) {
      Integer nodeIdx = entry.getKey();
      for (Integer neighbourIdx : entry.getValue())
        layeredBfsProcesses[nodeIdx].addNeighbour(layeredBfsProcesses[neighbourIdx]);
    }

    return layeredBfsProcesses;
  }

  /** Used for condition logging. Similar to Log4J. */
  private void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
