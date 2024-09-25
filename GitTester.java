import java.io.*;

public class GitTester {
    public static void main(String[] args) {
        System.out.println("Testing Git class...");

        System.out.println("\nTesting constructor...");
        System.out.println("Test 1: standard test (none of the directories nor files exist before)...");
        Git testGit = new Git();
        System.out.println("Sub-test a: preexistingFiles() -> When all 3 exist... " + testGit.preexistingFiles() + " should be: ttt");
        
        System.out.println("\nTest 2: calling Git() when all 3 directories and files exist...");
        testGit = new Git();
        System.out.print(" -> Should say: Git Repository already exists.");


        System.out.println("\nTesting deleteAllDirectoriesAndFiles()...");
        System.out.println("Test 1: all directories and files exist...");
        testGit.deleteAllDirectoriesAndFiles();
        System.out.print(" -> Should say: Files and directories successfully deleted.");

        System.out.println("\nTest 2: none of the directories and files exist...");
        testGit.deleteAllDirectoriesAndFiles();
        System.out.print(" -> Should say: Files and directories successfully deleted.\n");
        System.out.println("Now resetting the git repository:\n");
        testGit = new Git();

        //File testFile = new File("testingFile.txt");
        try {
            FileWriter fileWrite = new FileWriter("testingFile.txt");
            BufferedWriter writer = new BufferedWriter(fileWrite);
            writer.write("Hello world!");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        testGit.createBlob("testingFile.txt");
        testGit.deleteAllDirectoriesAndFiles();
    }

}