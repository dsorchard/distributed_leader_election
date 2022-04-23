package edu.utd.dc.project2.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * This will maintain the BFS Tree in our network
 *
 * @param <T> you can pass ProcessId or Integer depending upon the context
 */
public class TreeNode<T> {

  public T parentId;
  public boolean isLeaf;
  public boolean isRoot;

  public List<T> children;

  public TreeNode() {
    this.parentId = null;
    this.isLeaf = false;
    this.isRoot = false;
    this.children = new ArrayList<>();
  }
}
