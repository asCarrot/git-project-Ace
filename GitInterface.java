public interface GitInterface {
    /**
     * Stages a file for the next commit.
     * 
     * @param filePath The path to the file to be staged.
     */
    void stage(String filePath);

    /**
     * Creates a commit with the given author and message
     * Should capture the current state of repo, update head, and return the commit hash.
     * 
     * @param author The name of the author making this commit.
     * @param message The commit's message describing the changes.
     * @return the SHA1 hash of the newly created commit.
     */
    String commit(String author, String message);

    /**
     * EC: Checks out specfic commit when given its hash.
     * Should update working directory to match the state of the repo at that commit
     * 
     * @param commitHash The SHA1 hash of the commit to check out.
     */
    void checkout(String commitHash);
}