# git-project-Ace

1. This code's stage() method updates the index and creates a new tree from the current working directory.
   I tested this method through testing commit().

2. This code's commit() method uses stage() to update the index, and then writes a new commit file to objects.
   I tested this by committing a working directory. I then added files to the tester and committed, and my third
   test, I edited some of the files and committed. I think it works well, and all of the commit files' parents
   are accurate.

3. 

4. 

Create a Git object with its working directory set to the project you want to commit and call commit on that object.