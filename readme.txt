First, set working directory to project2 using command: cd project2
Second, compile java files:
   javac *.java

There are five directories: server0, server1, server2, server3, server4 each corresponding to each server with serverId 0, 1, 2, 3, 4 respectively, which will be used to store the chunk files on each server

Run MServer using command (we can run it on csgrads1.utdallas.edu): 
java SocketMServer [Mport] (Mport is the port number where Mserver is running), for example: 
java SocketMServer 3304

Run five servers respectively on dc01 ~ dc05 using command: 
java SocketServer [Mhostname] [Mport] [port] [serverId]
(Mhostname is the hostname for Mserver, Mport is the port number for Mserver, port is the port number for the current server, serverId is the id for current server (0, 1, 2, 3, 4)), for example:
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

To create a file, user needs to enter the filename, and the Mserver will randomly select three serverIds where three copies of the first chunk file will be created, client will send request to the servers to create that three copies of chunk file. If more than two servers are down, the new file will not be created.

To read a file, user needs to enter the filename, and the Mserver will random select any copy of each chunkfile and provide a list of chunk file copies that belong to that file, and client will read each chunk file copy from the servers; If there exist a chunk file that all three copies are not avaliable, the read operation will fail.

To write a file, user needs to enter the filename and the text to be written to that file, the Mserver will locate the three copies of the last chunk file if there are enough space to append the text to it. If all copies are not avaliable, the write operaion will fail. If at least one copy is avaiable, the write operation will be performed on all live copies and the unavaliable copies will be marked as out of date. For all live copies, the write operation is performed using two-phase commit protocol. User can change server's decision of agree or abort by typing "agree" or "abort" in the server's terminal. 
Mserver will ask the client to create copies of new chunk file on the servers if there is not enough space on the last chunk file. In this case, if more than two servers are down, the write operation will fail. Mserver will also ask the client to append null character to all live copies of last chunk file before it creates copies of new chunk file.

User can shut down the server by enter "down" on the server's terminal. After the server is down, user will not be allowed to read any file that has chunk files located on that server. Also, if the last chunk file is located on that server, user will not be allowed to write on that chunk. And when randomly selecting a server to create a new file, user will not be able to create a new file on that server.

User can recover the server by enter "recover" on the server's terminal. After the server is recovered, the Mserver will check if the server has any copies of chunk file that are out of date. If true, then it will check if other copies that are updated are available. If at least one copy is available, it will copy that one to get updated. Otherwise, the recovery of the server will fail. It will try to recover until at lease one copy of the most updated one becomes available.

User can let the server agree or abort to the commit request by enter "agree" or "abort". If use enters "agree", the server will agree to any commit request sent to it by the client. Otherwise, it will abort all the commit request.



