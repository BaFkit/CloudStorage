# CloudStorage
<h2>Functions:</h2>

 * Authenticate
 * Registration
 * Upload
 * Download
 * Copy
 * Paste
 * Cut
 * Create Dir
 * Remove
 * There is a standard sort and definition of folder or file
 * Users have their own limited file space
 * Search
 * File info >> being completed

 ## The project consists of two modules:

 ### Client
*JDK 1.8, JavaFX, Java IO, NIO;*
 ### Server
*JDK 11, io.netty;* 

*Data base: SQlite3*
____
To start, you need the SD directory and the user_cloud.db database file in the root of the project.
 ###### Database creation: 
```
CREATE TABLE users_cloud.db (
id INTEGER PRIMARY KEY AUTOINCREMENT,
login TEXT NOT NULL UNIQUE,
pass INTEGER NOT NULL,
root TEXT NOT NULL UNIQUE,
spaces INTEGER NOT NULL);
```
