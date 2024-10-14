import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Git implements GitInterface {
    public static boolean compressionOn = false;
    public boolean showPrintedTestResults = false; // when true, prints results of any method that's called.
    public String workingDirectoryPath = "tester";

    public Git() {
        boolean newDirectoriesOrFilesCreated = false; // true if any new directories and/or files are newly created.
        
        if (new File("git").mkdir()) {
            newDirectoriesOrFilesCreated = true;
        } if (new File("git/objects").mkdir()) {
            newDirectoriesOrFilesCreated = true;
        }
        File temporaryFile = new File("git/index");
        try {
            if (temporaryFile.createNewFile()) {
                newDirectoriesOrFilesCreated = true;
            }
            temporaryFile = new File("git/HEAD");
            if (temporaryFile.createNewFile()) {
                newDirectoriesOrFilesCreated = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (showPrintedTestResults) {
            if (newDirectoriesOrFilesCreated) {
                System.out.println("Git Repository successfully created!");
            } else {
                System.out.println("Git Repository already exists.");
            }
        }
    }
    public Git (boolean compressionOn, boolean showPrintedTestResults, String workingDirectoryPath) {
        Git.compressionOn = compressionOn;
        this.showPrintedTestResults = showPrintedTestResults;
        this.workingDirectoryPath = workingDirectoryPath;
        new Git();
    }

    public void setShowPrintedTestResults(boolean showPrintedTestResults) {
        this.showPrintedTestResults = showPrintedTestResults;
    }
    public void setWorkingDirectoryPath(String workingDirectoryPath) {
        this.workingDirectoryPath = workingDirectoryPath;
    }

    // createBlob(String targetFile): creates a BLOB with a unique name holding targetFile's contents in the objects folder.
    // If compressionOn = true, targetFile's contents will be compressed before copying to the BLOB file.
    public void createBlob(String targetFile) throws IOException {
        if (compressionOn) {
            zipCompressBlob(targetFile);
        }

        FileReader readSource = new FileReader(targetFile);
        StringBuilder sourceText = new StringBuilder();
        while(readSource.ready()) {
            sourceText.append(readSource.read());
        }
        readSource.close();

        String uniqueHashName = generateUniqueFileName(sourceText.toString());
        File blob = new File("git/objects/" + uniqueHashName);
        File originalSource = new File(targetFile);

        try {
            blob.createNewFile();
            FileOutputStream writer = new FileOutputStream(blob);
            Files.copy(originalSource.toPath(), writer);
            FileWriter writerOfIndex = new FileWriter("git/index",true);
            writerOfIndex.write("blob " + uniqueHashName + " " + originalSource.getPath() + "\n");
            writerOfIndex.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //createTree (String dirName, String path): Creates Tree from directory name and it's path
    public String createTree (String path, String dirName) throws IOException
    {
        File dir = new File (path + dirName);
        if (!dir.exists())
            throw new IOException("This path does not exist.");

        //System.out.println(dir.getAbsolutePath());
        String hash = createTreeRecursive(dir);

        File indexFile = new File ("git/index");
        FileWriter indexWriter = new FileWriter(indexFile,true);
        indexWriter.append("tree "  + hash + " " + dir.getPath() + '\n');
        indexWriter.close();
        return hash;
    }

    // createTreeRecursive(File dir): Creates Tree recursively from directory and creates trees for subdirectories.
    // @param directory that will be base of tree created
    // @return Name of Tree File
    private String createTreeRecursive (File dir) throws IOException {
        StringBuilder treeData = new StringBuilder();

        File index = new File ("git/index");
        FileWriter indexWrite = new FileWriter(index,true);

        File [] list = dir.listFiles();
        for (File file : list) {
            if (file.isDirectory())
            {
                String treeHead = createTreeRecursive(file);
                treeData.append("tree "  + treeHead + " " + file.getPath() + '\n');              
                indexWrite.append("tree "  + treeHead + " " + file.getPath() + '\n');
            }
            else
            {
                createBlob(file.getPath());
                if (showPrintedTestResults) {
                    System.out.println("Blob Created: " + file.getName());
                }
                FileReader reader = new FileReader(file);
                StringBuilder fileData = new StringBuilder();
                while (reader.ready())
                {
                    fileData.append(reader.read());
                }
                
                treeData.append("blob " + generateUniqueFileName(fileData.toString()) + " " + file.getPath() + '\n');
                reader.close();
            }
        }
        indexWrite.close();

        String treeName = generateUniqueFileName(treeData.toString());
        File tree = new File ("git/objects/" + treeName);
        FileWriter write = new FileWriter(tree);
        write.append(treeData.toString());
        write.close();

        if (compressionOn) {
            zipCompressBlob(tree.getPath());
        }

        return treeName;
    }

    // zipCompressBlob(String filePath): zips the file from filePath.
    private static void zipCompressBlob(String filePath) {
        try {
            File originalFile = new File(filePath);
            String zipFileString = originalFile.getName().concat(".zip");
            FileOutputStream fileWriter = new FileOutputStream(zipFileString);
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(fileWriter);
            ZipOutputStream zipWriter = new ZipOutputStream(bufferedWriter);

            zipWriter.putNextEntry(new ZipEntry(originalFile.getName()));
            byte[] originalFileBytes = Files.readAllBytes(Paths.get(filePath));
            zipWriter.write(originalFileBytes);
            zipWriter.closeEntry();
            zipWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // generateUniqueFileName(String inputFile): generates a unique hash value from the contents of the file given.
    // the hash function algorithm is SHA-1.
    public static String generateUniqueFileName(String inputFileContents) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigestArray = md.digest(inputFileContents.getBytes());
            BigInteger no = new BigInteger(1, messageDigestArray);
            String hashOfMessage = no.toString(16);
            while (hashOfMessage.length() < 40) {
                hashOfMessage = "0" + hashOfMessage;
            }
            return hashOfMessage;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // uses FileWriter to clear the previous index contents, and then calls on createTree to update index.
    public String stage(File startingDirectory) {
        try {
            FileWriter indexClearing = new FileWriter("git/index", false);
            indexClearing.write("");
            indexClearing.close();
            return createTree(startingDirectory.getPath(), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void stage(String filePath) {
        File givenFile = new File(filePath);
        if (!givenFile.exists() || givenFile.isDirectory()) {
            System.out.println("This file path does not exist, or is a directory.");
            return;
        }
        stage(givenFile);
    }
    // creates a commit file in the objects folder and calls on stage() method.
    public String commit(String author, String message) {
        try {
            String hashOfWorkingDirectory = stage(new File(workingDirectoryPath));
            File currentCommitFile = new File("git/objects/TEMPORARYNAME");

            StringBuilder commitContents = new StringBuilder();
            commitContents.append("tree: " + hashOfWorkingDirectory + '\n');
            commitContents.append("parent: ");
            BufferedReader headReader = new BufferedReader(new FileReader("git/HEAD"));
            String headContent = headReader.readLine();
            if (headContent != null) {
                commitContents.append(headContent);
            } headReader.close();
            commitContents.append('\n' + "author: " + author + '\n');
            commitContents.append("date: " + LocalDateTime.now() + '\n');
            commitContents.append("message: " + message);
            BufferedWriter writerOfCommit = new BufferedWriter(new FileWriter(currentCommitFile));
            writerOfCommit.write(commitContents.toString());
            writerOfCommit.close();

            String hashOfCommit = generateUniqueFileName(commitContents.toString());
            currentCommitFile.renameTo(new File("git/objects/" + hashOfCommit));
            FileWriter writerOfHEAD = new FileWriter("git/HEAD");
            writerOfHEAD.write(hashOfCommit);
            writerOfHEAD.close();
            return hashOfCommit;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void commit() throws IOException {
        String hashOfCommit = createTree(workingDirectoryPath,"");
        FileWriter writerOfHEAD = new FileWriter(workingDirectoryPath);
        writerOfHEAD.write(hashOfCommit);
        writerOfHEAD.close();
    }

    public void checkout(String commitHash) {
        File targetCommit = new File("git/objects/" + commitHash);
        if (!targetCommit.exists()) {
            System.out.println("Target commit does not exist.");
            return;
        }
        try {
            BufferedReader commitReader = new BufferedReader(new FileReader(targetCommit.getPath()));
            traverse(targetCommit);

            commitReader.close();
            
            FileWriter headWriter = new FileWriter("git/HEAD");
            headWriter.write(commitHash);
            headWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void traverse (File commit) {
        
    }


    // METHODS FOR TESTING
    public static boolean deleteAllGitFiles() {
        File fileToBeDeleted = new File("git");
        if (fileToBeDeleted.exists()) {
            recursivelyDeleteDirectoriesAndFiles(fileToBeDeleted, 0);
            return true;
        }
        return false;
    }

    public static boolean deleteAllTestFiles() {
        File fileToBeDeleted = new File("tester");
        if (fileToBeDeleted.exists()) {
            recursivelyDeleteDirectoriesAndFiles(fileToBeDeleted, 0);
            return true;
        }
        return false;
    }

    // recursivelyDeleteDirectoriesAndFiles(File targetFile, int index): should do what the name suggests. This is a
    // The files/directories that will be deleted are only the ones that are inside of targetFile, if targetFile is a directory.
    private static void recursivelyDeleteDirectoriesAndFiles(File targetFile, int index) {
        File filePath = new File(targetFile.getAbsolutePath());
        String[] fileAndDirectoryList = filePath.list();

        if (targetFile.isFile()) {
            filePath.delete();
        } else if (targetFile.isDirectory() && (fileAndDirectoryList == null || fileAndDirectoryList.length == 0)) {
            filePath.delete();
        } else {
            for (int i = 0; i < fileAndDirectoryList.length; i++) {
                File nextFile = new File(filePath.getPath() + "/" + fileAndDirectoryList[i]);
                recursivelyDeleteDirectoriesAndFiles(nextFile, i);
            }
            filePath.delete();
        }
    }

    public static void createTestFiles() throws IOException {
        // creating all test directories
        new File("tester").mkdir();
        new File("tester/empty").mkdir();
        new File("tester/anotherEmptyDirectory").mkdir();
        new File("tester/coolFolder").mkdir();
        new File("tester/coolFolder/yippee").mkdir();
        
        FileWriter writer0 = new FileWriter("tester/test1.txt");
        writer0.write("1 fish");
        writer0.close();
        FileWriter writer1 = new FileWriter("tester/test2.txt");
        writer1.write("2 fish");
        writer1.close();
        FileWriter writer2 = new FileWriter("tester/coolFolder/testRed.txt");
        writer2.write("red fish");
        writer2.close();
        FileWriter writer3 = new FileWriter("tester/coolFolder/testBlue.txt");
        writer3.write("blue fish!");
        writer3.close();
        FileWriter writer4 = new FileWriter("tester/coolFOlder/yippee/fileOfWhimsyAndFun.txt");
        writer4.write("yippeeeeee!!! yippeeeeee!!! yippeeeeee!!! yippeeeeee!!! yippeeeeee!!! yippeeeeee!!! yippeeeeee!!! yippeeeeee!!! yippeeeeee!!!");
        writer4.close();
    }

    public static void modifyTestFiles() throws IOException {
        FileWriter writer0 = new FileWriter("tester/test1.txt");
        writer0.write("one fish,");
        writer0.close();
        FileWriter writer1 = new FileWriter("tester/coolFolder/testRed.txt");
        writer1.write("RED FISH,");
        writer1.close();
    }
    public static void addExtraTestFiles() throws IOException {
        new File("tester/uncoolFolder").mkdir();
        new File("tester/coolFolder/extraCoolFolder").mkdir();

        FileWriter writer0 = new FileWriter("tester/uncoolFolder/uncoolFile.txt");
        writer0.write("NOT cool :(");
        writer0.close();
        FileWriter writer1 = new FileWriter("tester/coolFolder/extraCoolFolder/unbelievablyCool.txt");
        writer1.write("SO COOL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        writer1.close();
        FileWriter writer2 = new FileWriter("tester/coolFolder/superCoolFranky.txt");
        writer2.write("suuuuuperrrrrr!!!");
        writer2.close();
    }
}