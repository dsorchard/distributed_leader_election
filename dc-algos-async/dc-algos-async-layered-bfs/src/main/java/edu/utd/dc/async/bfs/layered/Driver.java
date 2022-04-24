package edu.utd.dc.async.bfs.layered;

import edu.utd.dc.async.bfs.layered.layered.LayeredBfsManager;
import edu.utd.dc.async.bfs.layered.core.io.file.ConfigFileReader;
import edu.utd.dc.async.bfs.layered.telemetry.MessageTelemetryUtils;

public class Driver {

  public static void main(String[] args) {
    // Read Config
    String configFileName = args[0];
    ConfigFileReader configFileReader = new ConfigFileReader(configFileName);

    // Run Layered BFS & find max processId
    LayeredBfsManager layeredBFSManager = new LayeredBfsManager(configFileReader);
    int leaderId = layeredBFSManager.buildBFSTreeAndReturnLeader();

    // Print Leader
    System.out.println("Leader Id : " + leaderId);

    // Print Telemetry
    MessageTelemetryUtils.print();
  }
}
