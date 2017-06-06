package com.company; /**
 * Created by Kishan_Rao on 6/3/17.
 */
import java.util.*;
public class FileTree<String>
{
    String name;
    HashSet<String> files;
    HashSet<String> dependencies;
    HashSet<String> owners;     //An empty owners Set indicates we haven't indexed the folder yet.

    public FileTree(String name)
    {
        this.name = name;
        files = new HashSet<>();
        dependencies = new HashSet<>();
        owners = new HashSet<>();
    }

    public FileTree(String mName, HashSet<String> mFiles, HashSet<String> mDeps, HashSet<String> mOwners)
    {
        name = mName;
        files = mFiles;
        dependencies = mDeps;
        owners = mOwners;
    }

}