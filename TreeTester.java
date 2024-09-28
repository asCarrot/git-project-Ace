import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TreeTester {
    public static void main(String[] args) throws IOException {
        //Creates File Directory Test Structure
        File head = new File("head");
        head.mkdir();
        File subDir = new File ("head/subDir");
        subDir.mkdir();
        File testFile1 = new File ("head/TestFile1.txt");
        File testFile2 = new File ("head/TestFile2.txt");
        File testFile3 = new File ("head/subDir/TestFile3.txt");
        testFile1.createNewFile();
        testFile2.createNewFile();
        testFile3.createNewFile();
        FileWriter write1 = new FileWriter(testFile1,true);
        FileWriter write2 = new FileWriter(testFile2,true);
        FileWriter write3 = new FileWriter(testFile3,true);
        write1.append("Test File 1");
        write2.append("Test File 2");
        write3.append("Test File 3");
        write1.close();
        write2.close();
        write3.close();

        //Tests to make sure objects were input properly
        Git git = new Git();
        try {
            git.createTree(head);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
