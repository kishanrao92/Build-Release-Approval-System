package com.company;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;
import java.io.File;

public class Main {
    private static Map<String,FileTree> map=new LinkedHashMap<String,FileTree>();

    public static void main(String[] args) throws IOException {
	// write your code here
        Path input=Paths.get("/Users/Kishan_Rao/Downloads/repo_root");
        List<String> res=getFileNames(new ArrayList<String>(),input);



    }

    private static boolean approveFiles(){
        return false;
    }

    private static FileTree getFileNames(List<String> fileNames, Path dir) throws IOException{
        FileTree root=null;
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {

            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    root=new FileTree(path.getFileName());
                    root.dependency="";
                    root.owners="";
                    File[] files=path.toFile().listFiles();
                    for(File temp : files){
                        root.addChild(temp);
                    }
                    map.put(String.valueOf(path.getFileName()),root);
                    getFileNames(fileNames, path);
                } else {
                    //root.addChild(new FileTree(path.getFileName()));
                    fileNames.add(path.toAbsolutePath().toString());
                    System.out.println(path.getFileName());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return root;
    }


}
