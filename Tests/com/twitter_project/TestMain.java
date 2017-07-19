package com.twitter_project;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.*;



/**
 * Created by Kishan_Rao on 6/7/17.
 */

public class TestMain
{



    static Path currentRelativePath=Paths.get("");
    static String absoluteRootPath=currentRelativePath.toAbsolutePath().toString();
    static String rootPath = "Tests/test/com";



    @Test
    public void runValidationTestInsufficientApprovals() throws Exception
    {
        assertEquals("Insufficient Approvals",Main.runValidation("kishan","user/user.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("testOwner","Valid/valid.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("kishan","user/user.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("rao,kishan","follow/follow.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("kishan","user/user.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("tesOwner","message/message.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("kishan","user/user.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("rao,kishan","follow/follow.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("testOwner","message/message.java",rootPath));
        assertEquals("Insufficient Approvals",Main.runValidation("kishan","Valid/valid.java",rootPath));
        System.out.println("Test Insufficient Approvals test successful");
    }




    @Test
    public void runValidationTestApprovals() throws Exception
    {
        assertEquals("Approved",Main.runValidation("twitter,kishan","user/user.java",rootPath));
        assertEquals("Approved",Main.runValidation("twitter,testOwner","Valid/valid.java",rootPath));
        assertEquals("Approved",Main.runValidation("rao,kishan","user/user.java",rootPath));
        assertEquals("Approved",Main.runValidation("twitter,testOwner","follow/follow.java",rootPath));
        assertEquals("Approved",Main.runValidation("testOwner,kishan","user/user.java",rootPath));
        assertEquals("Approved",Main.runValidation("twitter,rao","message/message.java",rootPath));
        assertEquals("Approved",Main.runValidation("rao,kishan","user/user.java",rootPath));
        assertEquals("Approved",Main.runValidation("testOwner,twitter","follow/follow.java",rootPath));
        assertEquals("Approved",Main.runValidation("rao","user/user.java",rootPath));
        assertEquals("Approved",Main.runValidation("twitter","Valid/valid.java",rootPath));
        System.out.println("Test Approvals test successful");
    }


    @Test
    public void buildUpwardDependenciesTest() throws Exception
    {

        Main.init();
        Main.buildUpwardDependencies(rootPath);


        //test map size
        assertEquals(7,Main.mappings.size());

        //test dependencies
        assert(Main.mappings.get("user").dependencies.contains(new String("test/com/Valid/valid.java")));
        assert(Main.mappings.get("Valid").dependencies.contains(new String("test/com/user/user.java")));
        assert(Main.mappings.get("message").dependencies.contains(new String("")));

        //test owners
        assert(Main.mappings.get("follow").owners.contains(new String("twitter")));
        assert(Main.mappings.get("message").owners.contains(new String("twitter")));
        assert(Main.mappings.get("user").owners.contains(new String("twitter")));
        assert(Main.mappings.get("user").owners.contains(new String("rao")));
        assert(Main.mappings.get("user").owners.contains(new String("testOwner")));
        assert(Main.mappings.get("valid").owners.contains(new String("twitter,rao")));
        System.out.println("Build dependencies test successful");
 }

}