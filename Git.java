import java.io.File;
import java.io.IOException;

public class Git {
    public Git() {
        boolean newDirectoriesOrFilesCreated = false; // true if any new directories and/or files were created w/ this constructor method.
        if (new File("git").mkdir()) {
            newDirectoriesOrFilesCreated = true;
        } if (new File("git/objects").mkdir()) {
            newDirectoriesOrFilesCreated = true;
        }
        File indexFile = new File("git/index");
        try {
            if (indexFile.createNewFile()) {
                newDirectoriesOrFilesCreated = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (newDirectoriesOrFilesCreated) {
            System.out.println("Git Repository successfully created!");
        } else {
            System.out.println("Git Repository already exists.");
        }
    }
}