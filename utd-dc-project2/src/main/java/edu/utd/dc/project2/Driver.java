package edu.utd.dc.project2;

import edu.utd.dc.project2.algo.bfs.layered.LayeredBfsManager;
import edu.utd.dc.project2.core.io.file.ConfigFileReader;

public class Driver {

  public static void main(String[] args) {
    // Read Config
    String configFileName = args[0];
    ConfigFileReader configFileReader = new ConfigFileReader(configFileName);

    // Run FloodMax Leader Election Manager
    LayeredBfsManager layeredBFSManager = new LayeredBfsManager(configFileReader);
    int leaderId = layeredBFSManager.buildBFSTree();

    // Print Leader
    System.out.println(leaderId);
  }
}
