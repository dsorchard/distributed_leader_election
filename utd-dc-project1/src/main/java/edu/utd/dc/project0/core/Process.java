package edu.utd.dc.project0.core;

import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.io.sharedmemory.Channel;
import edu.utd.dc.project0.io.sharedmemory.Listener;
import edu.utd.dc.project0.io.sharedmemory.domain.Message;

import java.util.ArrayList;
import java.util.List;

public class Process implements Runnable {

  public ProcessId myId;
  public List<Channel> neighbours;
  public Listener listener;

  public Process(ProcessId myId, Listener listener) {
    this.myId = myId;
    this.listener = listener;
    this.neighbours = new ArrayList<>();
  }

  @Override
  public void run() {}

  public void send(ProcessId destination, Message message) {}

  public void onReceive(Message message) {}

  public void addNeighbour(Process process) {
    neighbours.add(new Channel(process));
  }
}
