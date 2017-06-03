package com.company; /**
 * Created by Kishan_Rao on 6/3/17.
 */
import java.util.*;
public class FileTree<String> {
    String data;
    FileTree<String> parent;
    List<FileTree<String>> children;
    String dependency=null;
    String owners=null;

    public FileTree(String data) {
        this.data = data;
        this.children = new LinkedList<FileTree<String>>();
    }

    public FileTree<String> addChild(String child) {
        FileTree<String> childNode = new FileTree<String>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

}