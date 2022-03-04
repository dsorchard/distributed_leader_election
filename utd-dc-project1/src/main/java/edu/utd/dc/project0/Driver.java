package edu.utd.dc.project0;

import edu.utd.dc.project0.algo.leaderelection.floodmax.FloodMaxLeaderElectionManager;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;

public class Driver {

  public static void main(String[] args) {
    // Read Config
    String configFileName = "C:/Users/spenc/Desktop/utd-dc-projects-master/utd-dc-project1/src/main/resources/inputdata.txt";
    ConfigFileReader configFileReader = new ConfigFileReader(configFileName);

    // Run FloodMax Leader Election Driver
    FloodMaxLeaderElectionManager floodMaxLeaderElectionManager =
        new FloodMaxLeaderElectionManager(configFileReader);
    int leaderId = floodMaxLeaderElectionManager.electLeader();

    // Print Leader
    System.out.println("Leader ID is: " + leaderId);
  }
}
