import java.io.IOException;

public class GitCommitTester {
    public static void main(String[] args) {
        // reset
        Git.deleteAllGitFiles();
        Git.deleteAllTestFiles();
        try {
            Git.createTestFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Git testGit = new Git(false, true, "tester");

        System.out.println("\nTESTING PART 3 OF GIT CLASS");

        System.out.println("\n\nTesting commit(String, String)...");
        System.out.println("\nTEST 1: Standard Test (initial commit)... " + testGit.commit("Ace", "Initial commit!"));

        try {
            Git.addExtraTestFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nTEST 2: Added Files Test... " + testGit.commit("Ace", "Added new Files!"));

        try {
            Git.modifyTestFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nTEST 3: Added Files + Modified Test... " + testGit.commit("Ace", "Fixed text!"));

    }
}