package com.twitter_project;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;
import java.io.File;

public class Main
{
    //Global program variables.
    //public static boolean flag=true;

    //  SET THIS TO TRUE TO PRINT PROGRAM LOGS
    public static final boolean PRINT_LOGS = true;
    //------------------------------

    private static String rootDir;

    protected static HashSet<String> approvers = new HashSet<>();
    protected static HashMap<String, FileTree> mappings = new HashMap<>();
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





        //Get all changed files
        String changedFiles = args[3];
        if(changedFiles == null)
            throw new Exception("NO FILES PASSED IN ARGUMENTS");    //TODO: Mention in docs



        //Get the root path from command line
        rootDir = args[5];

        runValidation(approversString,changedFiles,rootDir);
        //bigfunctiontocheckapprovers(approversString, changedFiles, rootDir);
        //Build upward dependencies for each folder within the root.


        if(PRINT_LOGS)
            System.out.println("=================================");

        //Checks all approvals, and exits /w notification if any approval is missing.
        //checkAllApproved();




    }


    protected static void runValidation(String approversList, String filesToBeChecked, String rootPath) throws Exception{

        //We have approvers if we reach here.
        for(String app : approversList.split(",")){
            approvers.add(app);
        }

        //We have files if we reach here
        files = filesToBeChecked.split(",");


        //Traverse directory root and build dependency hashmap for Upward dependencies
        buildUpwardDependencies(rootDir);



        //If we reach here, everyone is approved.
        checkAllApproved();

        System.out.println("Approved");



    }

    protected static void buildUpwardDependencies(String dir) throws IOException
    {
        //Create an empty object for the folder.
        FileTree current;
        if(mappings.containsKey(dir))
            current = mappings.get(dir);
        else
            current = new FileTree(dir);

        //Add object to mappings.
        mappings.put(dir, current);

        // Add all owners of the current directory to the object
        if(new File(dir + "/OWNERS").exists())
        {
            //Add owners from file to object's set
            HashSet<String> currentOwners = new HashSet<>(Files.readAllLines(Paths.get(dir+"/OWNERS")));

            current.owners = currentOwners;
            //parentOwners=currentOwners;
            current.parents=parentOwners;
            parentOwners = currentOwners;
        }

        //If there's no owners for the current folder we're looking at..
        else
        {
            //Add parent's owners to current owner set
            current.owners = parentOwners;
        }

        //Read all dependent folders.
        //For each dependency, create an object for the dependency and add CURRENT folder as THAT folder's dependency
        //We do this to create UPWARD dependencies.
        if(new File(dir + "/DEPENDENCIES").exists())
        {
            //Check if object exists

            List<String> currentDeps = Files.readAllLines(Paths.get(dir+"/DEPENDENCIES"));
            if(PRINT_LOGS)
            {
                System.out.println("Current directory is "+dir);
                System.out.println("Dependencies are "+Arrays.toString(currentDeps.toArray()));

            }

            for(String temp : currentDeps)
            {
                temp = rootDir+ "/" +temp;

                //If yes, add current folder to THAT folder.
                //If no, create new object and add current folder to THAT folder.
                if(mappings.containsKey(temp))
                {
                    if(PRINT_LOGS)
                    {
                        System.out.println("File " + temp + " exists");
                        System.out.println("----------- ");
                        System.out.println("add " + dir);
                        System.out.println("----------- ");
                    }

                    FileTree existingFolder=mappings.get(temp);
                    existingFolder.dependencies.add(dir);
                }
                else
                {
                    if(PRINT_LOGS)
                        System.out.println("File "+temp+" is new");

                    FileTree newFolder = new FileTree(temp);
                    newFolder.dependencies.add(dir);
                    mappings.put(temp, newFolder);
                }
            }
        }

        //Recursively process ALL the child folders of the folder
        //ONLY if they aren't DEPENDECIES, OWNERS or a hidden file/folder.
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir)))
        {
            for (Path path : stream)
            {
                //Ignore all that we need to ignore.
                if(path.getFileName().toString().startsWith(".") ||
                        path.getFileName().toString().startsWith("OWNERS")||
                        path.getFileName().toString().startsWith("DEPENDENCIES"))
                {
                    continue;   //To the next child.
                }

                if(path.toFile().isDirectory())
                {
                    buildUpwardDependencies(path.toFile().toString());
                    //current.owners.addAll(parentOwners);
                    current.parents=parentOwners;
                    parentOwners=current.owners;
                }
                //For files - required to check the existence of a file.
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

    //Function for checking all changed files.
    //Delegates actual task to a recursive FOLDER checker after if confirms existence of the file.
    public static void checkAllApproved() throws Exception
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

    //Recursive FOLDER checker.
    //Approves the folder in the following order:
    //  Checks itself first
    //  Then checks all (UPWARD) DEPENDENCIES -- RECURSIVELY
    //  Then checks all parent folders upto the root
    /////////
    //Uses Set to cache previously approved folders to avoid redundant checks

    private static void validateFolder(String file)
    {
        //If a basic dependency has been approved, return from here!
        if(approvedFolders.contains(file))
        {
            if(PRINT_LOGS)
                System.out.println("Already approved:\t\t\t" + file);
            return;
        }

        //Split path into successively smaller portions until we reach the root directory
        //We do this to check parents.
        int rootParts = rootDir.split("/").length;
        int skip = 0;   //The number of levels we want to skip
        String[] pathPieces = file.split("/");

        //Parent checker i.e after approving a current level, validate approval for level above till root.
        parentChecker: while(pathPieces.length - (skip + rootParts) + 1 > 0)    //Add 1 for the root folder
        {
            int check=pathPieces.length - (skip + rootParts);

            //Build the path we want to check.
            StringBuilder checkPathSB = new StringBuilder(rootDir);
            for(int i=0; i<(pathPieces.length - rootParts - skip); i++)
            {

                checkPathSB.append("/" + pathPieces[rootParts + i]);

            }
            String checkPath = checkPathSB.toString();

            if(approvedFolders.contains(checkPath))
            {
                if(PRINT_LOGS)
                    System.out.println("Already approved:\t\t\t" + checkPath);
                break parentChecker;
            }

            if(PRINT_LOGS)
                System.out.println("Approving " + checkPath);

            FileTree checkFolderObj = mappings.get(checkPath);

            if(PRINT_LOGS)
            {
                Iterator test = checkFolderObj.dependencies.iterator();

                while (test.hasNext())
                {
                        System.out.println("Dependencies of checkObj are " + test.next());
                }
            }

            //Check approvals for given folder object
            boolean approved = false;
            appcheck: for(String app : approvers)
            {
                if(checkFolderObj.owners.contains(app))
                {
                    approved = true;
                    approvedFolders.add(checkPath);
                    break appcheck;
                }
            }
            if(!approved)
            {
                //flag=false;
                System.out.println("Insufficient Approvals");
                System.exit(0);
            }

            //check if dependencies are approved
            Iterator iterator = checkFolderObj.dependencies.iterator();
            while(iterator.hasNext())
            {
                validateFolder((String) iterator.next());
            }

            //increment the number of levels to skip by one
            skip++;

        }   //Ends Parent checker WHILE loop.
    }


}
