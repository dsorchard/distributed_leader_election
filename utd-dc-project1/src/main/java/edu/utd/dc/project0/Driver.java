package edu.utd.dc.project0;

import edu.utd.dc.project0.algo.impl.floodmax.LeaderElectionDriver;
import edu.utd.dc.project0.core.io.file.ConfigFileReader;

public class Driver {
  static String configFileName =
      "/Users/xvamp/Library/CloudStorage/OneDrive-TheUniversityofTexasatDallas/arjun_mbp/workspace/sem2/DC/utd-dc-projects/utd-dc-project1/src/main/resources/inputdata.txt";

  public static void main(String[] args) {

    ConfigFileReader configFileReader = new ConfigFileReader(configFileName);

    LeaderElectionDriver leaderElectionDriver = new LeaderElectionDriver(configFileReader);
    int leaderId = leaderElectionDriver.electLeader();
    System.out.println(leaderId);
  }
}
