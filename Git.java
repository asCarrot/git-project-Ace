import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Git {
    public boolean compressionOn = true;

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

    // createBlob(String targetFile): creates a BLOB with a unique name holding targetFile's contents in the objects folder.
    // If compressionOn = true, targetFile's contents will be compressed before copying to the BLOB file.
    public void createBlob(String targetFile) {
        if (compressionOn) {
            zipCompressBlob(targetFile);
        }
        String uniqueHashName = generateUniqueFileName(targetFile);
        File blob = new File("git/objects/" + uniqueHashName);
        File source = new File(targetFile);
        try {
            blob.createNewFile();
            FileOutputStream writer = new FileOutputStream(blob);
            Files.copy(source.toPath(), writer);
            FileWriter writerOfIndex = new FileWriter("git/index");
            writerOfIndex.write(uniqueHashName + "  " + source.getName() + "\n");
            writerOfIndex.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    // zipCompressBlob(String filePath): zips the file from filePath.
    public void zipCompressBlob(String filePath) {
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
    public String generateUniqueFileName(String inputFileContents) {
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

    // if the nested if statements are too annoying, I can change them.
    // preexistingFiles() returns a String length 3, with each letter being either "f" or "t".
    // "f" stands for false, meaning that the file or directory DNE. "t" stands for true, meaning that it does exist.
    public String preexistingFiles() {
        File currentDirectory = new File("git");
        if (currentDirectory.exists()) {
            currentDirectory = new File("git/objects");
            if (currentDirectory.exists()) {
                currentDirectory = new File("git/index");
                if (currentDirectory.exists()) {
                    return "ttt";
                } else {
                    return "ttf";
                }
            } else {
                return "tff";
            }
        }
        return "fff";
    }

    public void deleteAllDirectoriesAndFiles() {
        File fileToBeDeleted = new File("git");
        if (fileToBeDeleted.exists()) {
            recursivelyDeleteDirectoriesAndFiles(fileToBeDeleted, 0);
            
            System.out.println("Files and directories successfully deleted.");
        } else {
            System.out.println("Can't find the given file.");
        }
    }

    // recursivelyDeleteDirectoriesAndFiles(File targetFile, int index): should do what the name suggests. This is a
    // private "helper" method for deleteAllDirectoriesAndFiles().
    // The files/directories that will be deleted are only the ones that are inside of targetFile, if targetFile is a directory.
    // Probably not my greatest piece of work
    private void recursivelyDeleteDirectoriesAndFiles(File targetFile, int index) {
        File filePath = new File(targetFile.getAbsolutePath());
        String[] fileAndDirectoryList = filePath.list();

        if (targetFile.isFile()) { // if the current pathname is a file
            filePath.delete();
        } else if (targetFile.isDirectory() && (fileAndDirectoryList == null || fileAndDirectoryList.length == 0)) { // if the current pathname is a directory that is empty
            filePath.delete();
        } else {
            for (int i = 0; i < fileAndDirectoryList.length; i++) {
                File nextFile = new File(filePath.getPath() + "/" + fileAndDirectoryList[i]);
                recursivelyDeleteDirectoriesAndFiles(nextFile, i);
            }
            filePath.delete();
        }
    }
    public void resetTestFiles() {
        
    }
}