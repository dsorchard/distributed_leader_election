package edu.utd.dc.project0.io.sharedmemory;

import edu.utd.dc.project0.core.Process;
import edu.utd.dc.project0.io.sharedmemory.domain.Message;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Channel {

  public final Process process;
  private final Queue<Message> messageQueue;

  public Channel(Process process) {
    this.process = process;
    this.messageQueue = new ConcurrentLinkedQueue<>();
  }

  public void add(Message message) {
    this.messageQueue.add(message);
  }

  public ArrayList<Message> read(int clockValue) {
    ArrayList<Message> deliverableMessages = new ArrayList<>();
    //    while (!messageQueue.isEmpty() && messageQueue.peek().timeStamp <= clockValue) {
    //      deliverableMessages.add(this.messageQueue.poll());
    //    }
    return deliverableMessages;
  }
}
