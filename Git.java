import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.StringBuilder;


public class Git {
    public boolean compressionOn = false;

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
    public void createBlob(String targetFile) throws IOException {
        if (compressionOn) {
            zipCompressBlob(targetFile);
        }

        FileReader readSource = new FileReader(targetFile);
        StringBuilder sourceText = new StringBuilder();
        while(readSource.ready())
        {
            sourceText.append(readSource.read());
        }
        readSource.close();

        String uniqueHashName = generateUniqueFileName(sourceText.toString());
        File blob = new File("git/objects/" + uniqueHashName);
        File source = new File(targetFile);
        try {
            blob.createNewFile();
            FileOutputStream writer = new FileOutputStream(blob);
            Files.copy(source.toPath(), writer);
            FileWriter writerOfIndex = new FileWriter("git/index",true);
            writerOfIndex.write("blob " + uniqueHashName + " " + targetFile + "\n");
            writerOfIndex.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    //createTree (String dirName, String path): Creates Tree from directory name and it's path
    public String createTree (String path, String dirName) throws IOException
    {
        File dir = new File (path+dirName);
        if (!dir.exists())
            throw new IOException("This path does not work");

        System.out.println(dir.getAbsolutePath());
        String hash = createTreeRecursive(dir);
        File indexFile = new File ("git/index");
        FileWriter indexWriter = new FileWriter(indexFile,true);
        indexWriter.append("tree "  + hash +" " + dir.getPath() + '\n');
        indexWriter.close();
        return hash;
    }

    // createTreeRecursive(File dir): Creates Tree recursively from directory and creates trees for subdirectories.
    // @param directory that will be base of tree created
    // @return Name of Tree File
    private String createTreeRecursive (File dir) throws IOException
    {
        StringBuilder treeData = new StringBuilder();

        File index = new File ("git/index");
        FileWriter indexWrite = new FileWriter(index,true);

        File [] list = dir.listFiles();
        for (File file : list) {
            if (file.isDirectory())
            {
                String treeHead = createTreeRecursive(file);
                treeData.append("tree : "  + treeHead +" : " + file.getName() + '\n');              
                indexWrite.append("tree "  + treeHead +" " + file.getPath() + '\n');
            }
            else
            {
                createBlob(file.getPath());
                System.out.println("Blob Created: " + file.getName());
                FileReader reader = new FileReader(file);
                StringBuilder fileData = new StringBuilder();
                while (reader.ready())
                {
                    fileData.append(reader.read());
                }
                
                treeData.append("blob : " + generateUniqueFileName(fileData.toString()) + " : " + file.getName() + '\n');
                reader.close();
            }
        }
        indexWrite.close();

        String treeName = generateUniqueFileName(treeData.toString());
        File tree = new File ("git/objects/"+treeName);
        FileWriter write = new FileWriter(tree);
        write.append(treeData.toString());
        write.close();

        //zipCompressBlob(tree.getPath());
        return treeName;
    }

    // zipCompressBlob(String filePath): zips the file from filePath.
    public static void zipCompressBlob(String filePath) {
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
}