package edu.utd.dc.project0;

import edu.utd.dc.project0.constants.GlobalConstants;
import edu.utd.dc.project0.constants.LogLevel;
import edu.utd.dc.project0.core.Process;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.utils.TimeUtils;

import java.util.List;
import java.util.Map;

public class Driver {

  public static void main(String[] args) {
    ConfigFileReader configFileReader =
        new ConfigFileReader(
            "/Users/xvamp/Library/CloudStorage/OneDrive-TheUniversityofTexasatDallas/arjun_mbp/workspace/sem2/DC/utd-dc-projects/utd-dc-project1/src/main/resources/inputdata.txt");

    prettyPrintConfig(configFileReader);

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
        // process.setCanStartRound(true);
      }
    }
  }

  private static boolean isAnyThreadAlive(Thread[] threads) {
    for (Thread thread : threads) if (thread.isAlive()) return true;
    return false;
  }

  private static Process[] initProcesses(ConfigFileReader configFileReader) {
    int n = configFileReader.getSize();

    Process[] processes = new Process[n];
    for (int i = 0; i < n; i++) {
      ProcessId processId = configFileReader.getProcessIdList().get(i);
      processes[i] = new Process(processId);
    }

    for (Map.Entry<Integer, List<Integer>> entry : configFileReader.getAdjList().entrySet()) {
      Integer nodeIdx = entry.getKey();
      for (Integer neighbourIdx : entry.getValue())
        processes[nodeIdx].addNeighbour(processes[neighbourIdx]);
    }

    return processes;
  }

  private static void prettyPrintConfig(ConfigFileReader configFileReader) {

    System.out.println(configFileReader.getSize());

    configFileReader
        .getAdjList()
        .forEach(
            (k, v) -> {
              System.out.print(k + " -- ");

              for (Integer processId : v) System.out.print(processId + ", ");

              System.out.print("\n");
            });
  }

  private static void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
