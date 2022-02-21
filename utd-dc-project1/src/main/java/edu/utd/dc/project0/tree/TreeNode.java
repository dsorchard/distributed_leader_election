package edu.utd.dc.project0.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {

  public T parentId;
  public boolean isLeaf;
  public List<T> children;

  public TreeNode() {
    this.parentId = null;
    this.isLeaf = false;
    this.children = new ArrayList<>();
  }
}
