package edu.utd.dc.project2.core.support;

import java.util.Objects;

/**
 * At present, we only have id. But this could be further extended to have id + MAC Address to break
 * the ties.
 */
public class ProcessId {

  private final int id;

  public ProcessId(int id) {
    this.id = id;
  }

  public int getID() {
    return id;
  }

  @Override
  public String toString() {
    return "ProcessId{" + "id=" + id + '}';
  }
}
