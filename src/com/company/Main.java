package com.company;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;
import java.io.File;

public class Main
{
    //Global program variables.

    private static String rootDir;  // = "/Users/Kishan_Rao/Downloads/repo_root";

    private static HashSet<String> approvers = new HashSet<>();
    private static HashMap<String, FileTree> mappings = new HashMap<>();
    private static String[] files = null;

    //Store all Approved folders in a Set
    private static HashSet<String> approvedFolders=new HashSet<>();

    //Variable for keeping track of last recursion call's owners.
    private static HashSet<String> parentOwners= new HashSet<String>();

    public static void main(String[] args) throws Exception
    {

        //Get all approvers
        String approversString = args[1];
        if(approversString == null)
            throw new Exception("NO APPROVERS PASSED IN ARGUMENTS");
        //We have approvers if we reach here.
        for(String app : approversString.split(","))
            approvers.add(app);


        //Get all changed files
        String changedFiles=args[3];
        if(changedFiles==null)
            throw new Exception("NO FILES PASSED IN ARGUMENTS");    //TODO: Mention in docs
        //We have files if we reach here
        files = changedFiles.split(",");

        //Get the root path from command line
        rootDir=args[5];


        buildUpwardDependencies(rootDir);

        System.out.println("=================================");

        checkAllApproved(); //Checks all approvals, and exits /w notification if any approval is missing.

        //If we reach here, everyone is approved.
        System.out.println("Approved");

    }


    private static void buildUpwardDependencies(String dir) throws IOException
    {
        //Create an empty object for the folder.
        FileTree current;
        if(mappings.containsKey(dir))
            current = mappings.get(dir);
        else
            current = new FileTree(dir);

        mappings.put(dir, current);

        if(new File(dir + "/OWNERS").exists())
        {
            //Add owners from file to object's set
            HashSet<String> currentOwners = new HashSet<>(Files.readAllLines(Paths.get(dir+"/OWNERS")));

            current.owners = currentOwners;
            parentOwners = currentOwners;       //TODO: Possibly move towards the end.
        }

        else
        {
            //Add parent's owners
            current.owners = parentOwners;
        }

        if(new File(dir + "/DEPENDENCIES").exists())
        {
            //Check if object exists

            List<String> currentDeps = Files.readAllLines(Paths.get(dir+"/DEPENDENCIES"));
            System.out.println(dir + " has " + currentDeps.size());

            for(String temp : currentDeps)
            {
                temp = rootDir+"/"+temp;

                //If yes, add current folder to THAT folder.
                //If no, create new object and add current folder to THAT folder.
                System.out.println("checking currentDeps \t"+temp);

                if(mappings.containsKey(temp))
                {
                    FileTree existingFolder=mappings.get(temp);
                    existingFolder.dependencies.add(dir);
                }
                else
                {
                    FileTree newFolder=new FileTree(temp);
                    newFolder.dependencies.add(dir);
                    mappings.put(temp, newFolder);
                }
            }
        }


        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir)))
        {
            for (Path path : stream)
            {
                if(path.getFileName().toString().startsWith(".") ||
                        path.getFileName().toString().startsWith("OWNERS")||
                        path.getFileName().toString().startsWith("DEPENDENCIES"))
                {
                    continue;
                }

                if(path.toFile().isDirectory())
                {
                    buildUpwardDependencies(path.toString());
                    parentOwners=current.owners;
                }
                else
                {
                    //If file is found, add to current set
                    current.files.add(path.getFileName().toString());
                }
            }
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    private static void checkAllApproved() throws Exception
    {
        //For each given file, check if they exist and verify approvals
        for(String file : files)
        {
            file = rootDir + "/" + file;

            //Check if they exist in the given folder.
            if(!new File(file).exists())
                throw new Exception("FILE NOT FOUND" + file);

            int lastSlash = file.lastIndexOf("/");
            String folder = file.substring(0, lastSlash);

            validateFolder(folder);
        }
    }

    private static void validateFolder(String file)
    {
        if(approvedFolders.contains(file))
            return;

        System.out.println("Approving " + file);

        int rootParts = rootDir.split("/").length;

        int skip = 0;
        String[] pathPieces = file.split("/");

        while(pathPieces.length - (skip + rootParts) > 0)
        {
            StringBuilder checkPathSB = new StringBuilder(rootDir);
            for(int i=0; i<(pathPieces.length - rootParts); i++)
            {
                checkPathSB.append("/" + pathPieces[rootParts + i]);
            }
            String checkPath = checkPathSB.toString();

            FileTree checkFolderObj = mappings.get(checkPath);

            //Check approvals for given folder object
            boolean approved = false;
            appcheck: for(String app : approvers)
            {
                if(checkFolderObj.owners.contains(app))
                {
                    approved = true;
                    approvedFolders.add(file);
                    break appcheck;
                }
            }
            if(!approved)
            {
                System.out.println("Insufficient Approvals");
                System.exit(0);
            }

            //check if dependencies are approved
            Iterator iterator = checkFolderObj.dependencies.iterator();
            while(iterator.hasNext())
            {
                validateFolder((String) iterator.next());
            }

            skip++;
        }
    }


}
