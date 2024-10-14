import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TreeTester {
    public static void main(String[] args) throws IOException {
        Git.deleteAllTestFiles();
        Git.deleteAllGitFiles();
        Git.createTestFiles();

        //Tests to make sure objects were input properly
        try {
            Git git = new Git();
            git.createTree("","tester");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    private static void resetRecur (File file)
    {
        if (file.isDirectory())
        {
            String [] ls = file.list();
            for (String fileName : ls) {
                resetRecur(new File(file.getPath() + "/" + fileName));
                System.out.println("deleted " + fileName);
            }
        }
        file.delete();
    }
}
