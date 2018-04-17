import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.net.*;

public class SocketClient
{
   Socket socket = null;
   PrintWriter out = null;
   BufferedReader in = null;
   String[] hostname = new String[5];
   int numOfCopy;
   
   public void printMenu() {
	   System.out.println("\nPlease select an option:\n"
	   		+ "1. Create a new file\n"
	   		+ "2. List all existing files\n"
	   		+ "3. Read a file\n"
	   		+ "4. Write to a file\n"
	   		+ "5. Exit\n");
   }
   
   public void communicate(String host, int Mport, int port, int numOfCopy)
   {
      
	  boolean flag = true;
	  while(flag) {
		  printMenu();
		  Scanner sc = new Scanner(System.in);
          String line = sc.nextLine();
          if(line.isEmpty()) {
        	  continue;
          }
          char option = line.charAt(0);
          this.listenSocket(host, Mport);
          switch(option) {
              case '1':
            	  //Send create new file option over socket
            	  out.println(option);
            	  System.out.println("Enter the file name you want to create:");
            	  line = sc.nextLine();
            	  //Send filename over socket
            	  out.println(line);
            	  //Receive text from server
                  try
                  {
                     String filename = line;
                	 line = in.readLine();
                	 if(!line.equals("File exists") && !line.equals("More than two servers are down, cannot create new file")) {
                		 System.out.println("ServerIds are: " + line);
                		 String[] serverId = line.split(",");
                         for(String id: serverId) {
                        	 this.listenSocket(hostname[Integer.parseInt(id)], port);
                        	 out.println(option);
                        	 out.println(filename);
                        	 line = in.readLine();
                        	 System.out.println(line);
                         }
                	 }
                	 else {
                		//file already exist or all servers are down
                    	 System.out.println(line);
                	 }
                     
                  } 
                  catch (IOException e)
                  {
                     System.out.println("Read failed");
                     System.exit(1);
                  }
    	          break;
              case '2':
            	  //Send list all files option over socket
            	  out.println(option);
            	  //Receive text from server
                  try
                  {
                     line = in.readLine();
                     System.out.println("Current existing files: " + line);
                  } 
                  catch (IOException e)
                  {
                     System.out.println("Read failed");
                     System.exit(1);
                  }
    	          break;
              case '3':
            	  //Send read file option over socket
            	  out.println(option);
            	  System.out.println("Enter the file name you want to read:");
            	  line = sc.nextLine();
            	  String filename = line;
            	  //Send filename over socket
            	  out.println(line);
            	  //Receive text from server
                  try
                  {
                     line = in.readLine();
                     if(line.equals("File exists")) {
                    	 boolean end = false;
                    	 List<ChunkNode> list = new ArrayList<ChunkNode>();
                    	 while(!end) {
                    		line = in.readLine();
     	            		if(line.equals("E")) {
     	            			end = true;
     	            			continue;
     	            		}
     	            		else {
     	            			int chunkId = Integer.parseInt(line);
     	            			line = in.readLine();
     	            			int serverId = Integer.parseInt(line);
     	            			ChunkNode n = new ChunkNode(chunkId, serverId);
     	            			list.add(n);
     	            		}
                    	 }
                    	 
                    	 for(ChunkNode n: list) {
                    		 this.listenSocket(hostname[n.serverId], port);
                    		 out.println(option);
                    		 out.println(String.valueOf(n.chunkId));
                    		 line = in.readLine();
                    		 
                    		 System.out.println("Read from server"+n.serverId+ " chunkfile"+n.chunkId);
                    		 System.out.println(line);
                    	 }
                    	 
                     }
                     else {
                    	 //file does not exist or server is down or mapping has not been updated
                    	 System.out.println(line);
                     }
                  } 
                  catch (IOException e)
                  {
                     System.out.println("Read failed");
                     System.exit(1);
                  }
    	          break;
              case '4':
            	  //Send write file option over socket
            	  out.println(option);
            	  System.out.println("Enter the file name you want to write:");
            	  line = sc.nextLine();
            	  filename = line;
            	  //Send filename over socket
            	  out.println(line);
            	  //Receive text from server
                  try
                  {
                     line = in.readLine();
                     if(line.equals("File exists")) {
                    	 System.out.println("Enter the text you want to write:");
                   	     line = sc.nextLine();
                   	     String text = line;
                   	     int bytes = line.getBytes("UTF-8").length;
                   	     if(bytes > 2048 ) {
                   	    	 System.out.println("Then size of input text is larger than 2048 bytes");
                   	    	 continue;
                   	     }
                   	     out.println(bytes);
                   	     
                   	     line = in.readLine();
                   	     if(line.equals("Enough space")) {                 	    	 
                   	    	 line = in.readLine();
                   	    	 System.out.println("ServerIds are: " + line);
                   	    	 String[] serverId = line.split(",");
                             
                   	    	 line = in.readLine();
                   	    	 System.out.println("ChunkIds are: " + line);
                  	    	 String[] chunkId = line.split(",");
                   	    	 
                  	    	//send commit request to servers
                  	    	 ServerStatus status = new ServerStatus(false, false, 0);
              	    	     Semaphore sem_abort = new Semaphore(1);
              	    	     Thread t[] = new Thread[numOfCopy];
                  	    	 for(int i = 0; i < serverId.length; ++i) {
                  	    		 SendCommitRequest c;
                  		         c = new SendCommitRequest(hostname[Integer.parseInt(serverId[i])], port, Integer.parseInt(serverId[i]), status, sem_abort);
                  		         Thread commit = new Thread(c);
                  		         t[i] = commit;
                  		         commit.start();
                  		         
                  	    	 }
                  	    	 for(int i = 0; i < serverId.length; ++i) {
                  	    		try {
									t[i].join();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}                  	    	 
                  	    	 }
                  	    	 
                  	    	 //send abort to Mserver and servers
                  	    	 if(status.abort) {
                  	    		out.println("Abort");
                  	    		for(int i = 0; i < serverId.length; ++i) {
                  	    			this.listenSocket(hostname[Integer.parseInt(serverId[i])], port);
                               	    out.println("A");
                  	    		}
                  	    	 }
                  	    	 
                  	    	 //send commit to Mserver and servers
                  	    	 else {
                  	    		out.println("Commit");
                  	    		for(int i = 0; i < serverId.length; ++i) {
                  	    			this.listenSocket(hostname[Integer.parseInt(serverId[i])], port);
                               	    out.println("C");
                  	    		}
                  	    		
                  	    		//append line to the last chunk file 
                   	    	    for(int i = 0; i < serverId.length; ++i) {
                           	        this.listenSocket(hostname[Integer.parseInt(serverId[i])], port);
                           	        out.println(option);
                           	        out.println(filename);
                           	        out.println(chunkId[i]);
                           	        out.println(text);
                           	        line = in.readLine();
                           	        System.out.println(line);
                                }
                  	    	 }
                   	     }
                   	     else if(line.equals("Not enough space")) {
                   	    	 line = in.readLine();
                   	    	 System.out.println("Old ServerIds are: " + line);
                  	    	 String[] serverId = line.split(",");
                  	         
                  	    	 line = in.readLine();
                  	    	 System.out.println("Old ChunkIds are: " + line);
                 	    	 String[] chunkId = line.split(",");
                   	     
                  	         /*line = in.readLine();
                  	         System.out.println("New ServerIds are: " + line);
               	    	     String[] serverId_new = line.split(",");  */ 
               	    	     
               	    	     
               	    	     //send commit request to servers
                  	    	 ServerStatus status = new ServerStatus(false, false, 0);
              	    	     Semaphore sem_abort = new Semaphore(1);
              	    	     Thread t[] = new Thread[numOfCopy];
                  	    	 for(int i = 0; i < serverId.length; ++i) {
                  	    		 SendCommitRequest c;
                  		         c = new SendCommitRequest(hostname[Integer.parseInt(serverId[i])], port, Integer.parseInt(serverId[i]), status, sem_abort);
                  		         Thread commit = new Thread(c);
                  		         t[i] = commit;
                  		         commit.start();
                  		         
                  	    	 }
                  	    	 for(int i = 0; i < serverId.length; ++i) {
                  	    		try {
									t[i].join();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}                  	    	 
                  	    	 }
                  	    	 
                  	    	 //send abort to Mserver and servers
                  	    	 if(status.abort) {
                  	    		out.println("Abort");
                  	    		for(int i = 0; i < serverId.length; ++i) {
                  	    			System.out.println("Abort sent to server"+serverId[i]);
                  	    			this.listenSocket(hostname[Integer.parseInt(serverId[i])], port);
                               	    out.println("A");
                  	    		}
                  	    	 }
                  	    	 
                  	    	 //send commit to Mserver and servers
                  	    	 else {
                  	    		 out.println("Commit");
                  	    		 
                  	    		
                  	    		 //get new server ids
                  	    	     line = in.readLine();
                    	         System.out.println("New ServerIds are: " + line);
                 	    	     String[] serverId_new = line.split(","); 
                  	    		 
                  	    		 for(int i = 0; i < serverId.length; ++i) {
                  	    			System.out.println("Commit sent to server"+serverId[i]);
                  	    			this.listenSocket(hostname[Integer.parseInt(serverId[i])], port);
                               	    out.println("C");
                  	    		 }
                  	    		
                  	    	     //append null character to the last chunk
                  	    	     for(int i = 0; i < serverId.length; ++i) {
                            	    this.listenSocket(hostname[Integer.parseInt(serverId[i])], port);
                            	    out.println("0");
                            	    out.println(filename);
                            	    out.println(chunkId[i]);
                                 }
                  	    	     
                  	    	     //create a new chunk and append line to it
                 	    	     for(int i = 0; i < serverId.length; ++i) {
                            	    this.listenSocket(hostname[Integer.parseInt(serverId_new[i])], port);
                            	    out.println(option);
                            	    out.println(filename);
                            	    out.println("-1");
                            	    out.println(text);
                            	    line = in.readLine();
                            	    System.out.println(line);
                                 }        
                  	    	 }             	                        	     
                   	     }
                   	     else {
                   	         //server is down
                        	 System.out.println(line);
                   	     }	     
                     }
                     else {
                    	 //file does not exist
                    	 System.out.println(line);
                     }
                  } 
                  catch (IOException e)
                  {
                     System.out.println("Read failed");
                     System.exit(1);
                  }
    	          break;
              case '5':
            	  out.println(option);
            	  flag = false;
    	          break;
          }
	  }
	  
   }
  
   public void listenSocket(String host, int port)
   {
      //Create socket connection
      try
      {
	 socket = new Socket(host, port);
	 out = new PrintWriter(socket.getOutputStream(), true);
	 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } 
      catch (UnknownHostException e) 
      {
	 System.out.println("Unknown host");
	 System.exit(1);
      } 
      catch (IOException e) 
      {
	 System.out.println("No I/O");
	 System.exit(1);
      }
   }

   public static void main(String[] args)
   {
      if (args.length != 3)
      {
         System.out.println("Usage:  java SocketClient MserverHostname MserverPort ServerPort");
	 System.exit(1);
      }

      SocketClient client = new SocketClient();
      client.hostname[0] = "dc01.utdallas.edu";
      client.hostname[1] = "dc02.utdallas.edu";
      client.hostname[2] = "dc03.utdallas.edu";
      client.hostname[3] = "dc04.utdallas.edu";
      client.hostname[4] = "dc05.utdallas.edu";

      String host = args[0];
      int Mport = Integer.valueOf(args[1]);
      int port = Integer.valueOf(args[2]);
      int numOfCopy = 3;
      client.communicate(host, Mport, port, numOfCopy);
   }
}