import java.io.*;

public class GitTester {
    public static void main(String[] args) {
        // resetting previous test's setup
        Git.deleteAllGitFiles();
        Git.deleteAllTestFiles();
        try {
            Git.createTestFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nTESTING GIT CLASS");


        // constructor testing
        System.out.println("\n\nTesting constructor (2 tests)...");
        System.out.println("\nTEST 1 - Standard Test (none of the directories nor files exist before):");
        Git testGit = new Git();
        System.out.println(" -> Should say: Git Repository successfully created!");
        
        System.out.println("\nTEST 2 - calling Git() when all 4 directories and files exist:");
        testGit = new Git();
        System.out.println(" -> Should say: Git Repository already exists.");


        // deletion testing
        System.out.println("\n\nTesting deleteAllDirectoriesAndFiles() (2 tests)...");
        System.out.println("\nTEST 1 - Standard Test (all directories and files exist): " + Git.deleteAllGitFiles());
        System.out.println(" -> should be: true");

        System.out.println("\nTEST 2 - none of the directories and files exist: " + Git.deleteAllGitFiles());
        System.out.println(" -> should be: false");

        // (reset)
        System.out.println("\n(Now resetting the git repository)\n");
        testGit.setShowPrintedTestResults(false);
        testGit = new Git();
        testGit.setShowPrintedTestResults(true);


        // blob testing
        System.out.println("\nTesting createBlob(String)...");

    }
}