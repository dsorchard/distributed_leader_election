package edu.utd.dc.common.design_pattern.listener;

import edu.utd.dc.common.domain.io.Message;

/** Used by {@link edu.utd.dc.sync.flood_max.core.SyncProcess} to enable Observer nature. */
public interface Listener {
  void onReceive(Message payload);
}
