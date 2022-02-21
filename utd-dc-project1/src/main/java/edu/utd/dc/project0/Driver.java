package edu.utd.dc.project0;

import edu.utd.dc.project0.algo.leaderelection.floodmax.FloodMaxLeaderElectionManager;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;

public class Driver {
  static String configFileName =
      "/Users/xvamp/Library/CloudStorage/OneDrive-TheUniversityofTexasatDallas/arjun_mbp/workspace/sem2/DC/utd-dc-projects/utd-dc-project1/src/main/resources/inputdata.txt";

  public static void main(String[] args) {

    ConfigFileReader configFileReader = new ConfigFileReader(configFileName);

    FloodMaxLeaderElectionManager floodMaxLeaderElectionManager = new FloodMaxLeaderElectionManager(configFileReader);
    int leaderId = floodMaxLeaderElectionManager.electLeader();
    System.out.println(leaderId);
  }
}
