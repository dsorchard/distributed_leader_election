package edu.utd.dc.project0;

import edu.utd.dc.project0.algo.leaderelection.floodmax.FloodMaxLeaderElectionManager;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;

public class Driver {

  public static void main(String[] args) {
    // Read Config
    String configFileName = args[0];
    ConfigFileReader configFileReader = new ConfigFileReader(configFileName);

    // Run FloodMax Leader Election Manager
    FloodMaxLeaderElectionManager floodMaxLeaderElectionManager =
        new FloodMaxLeaderElectionManager(configFileReader);
    int leaderId = floodMaxLeaderElectionManager.electLeader();

    // Print Leader
    System.out.println(leaderId);
  }
}
