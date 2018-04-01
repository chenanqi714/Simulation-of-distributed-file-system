First, set working directory to project2 using command: cd project2
Second, compile java files:
   javac *.java

There are three directories: server0, server1, server2, each corresponding to each server with serverId 0, 1, 2 respectively, which will be used to store the chunk files on each server

Run MServer using command (we can run it on csgrads1.utdallas.edu): 
java SocketMServer [Mport] (Mport is the port number where Mserver is running), for example: 
java SocketMServer 3304

Run three servers respectively on dc01 ~ dc03 using command: 
java SocketServer [Mhostname] [Mport] [port] [serverId]
(Mhostname is the hostname for Mserver, Mport is the port number for Mserver, port is the port number for the current server, serverId is the id for current server (0, 1, 2)), for example:
java SocketServer csgrads1.utdallas.edu 3304 3305 0

Run two clients respectively on any machine using command:
java SocketClient [Mhostname] [Mport] [port]
(Mhostname is the hostname for Mserver, Mport is the port number for Mserver, port is the port number for the current server), for example:
java SocketClient csgrads1.utdallas.edu 3304 3305

Note:
All of the three servers will be running in the same port number;
Mserver and server are multithreaded, so semaphores are used to protect any data that can be accessed by multiple threads. For example, the hashtable that used to store the metadata must be protected by semaphore;
Each chunk file will has a chunk id as its filename. Each chunk file can be uniquely identified by a combination of serverId and chunkId. No two chunk files will have the same serverId and chunkId.

Users can choose any option:
1. Create a new file
2. List currently existing files
3. Read a file
4. Write to a file
5. Exit

To create a file, user needs to enter the filename, and the Mserver will randomly select a serverId where the first chunk file will be created, client will send request to the server to create that chunk file;

To read a file, user needs to enter the filename, and the Mserver will provide a list of chunk files that belong to that file, and client will read each chunk files from the servers;

To write a file, user needs to enter the filename and the text to be written to that file, the Mserver will locate the last chunk of the file if there are enough space to append the text to it or Mserver will ask the client to create a new chunk file on the server if there is not enough space on the last chunk file. In this case, Mserver will also ask the client to append null character to the last chunk file before it creates a new chunk file;

User can shut down the server by enter "down" on the server's terminal. After the server is down, user will not be allowed to read any file that has chunk files located on that server. Also, if the last chunk file is located on that server, user will not be allowed to write on that chunk. And when randomly selecting a server to create a new file, user will not be able to create a new file on that server.

User can recover the server by enter "recover" on the server's terminal. After the server is recovered, user can create, read and write files on that server.



