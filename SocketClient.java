// By Greg Ozbirn, University of Texas at Dallas
// Adapted from example at Sun website: 
// http://java.sun.com/developer/onlineTraining/Programming/BasicJava2/socket.html
// 11/07/07

import java.io.*;
import java.util.Scanner;
import java.net.*;

public class SocketClient
{
   Socket socket = null;
   PrintWriter out = null;
   BufferedReader in = null;
   String[] hostname = new String[3];
   
   public void printMenu() {
	   System.out.println("\nPlease select an option:\n"
	   		+ "1. Create a new file\n"
	   		+ "2. List all existing files\n"
	   		+ "3. Read a file\n"
	   		+ "4. Write to a file\n"
	   		+ "5. Exit\n");
   }
   
   public void communicate(String host, int port)
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
                     int serverId = Integer.parseInt(line);
                     System.out.println("ServerId is: " + serverId);
                     this.listenSocket(hostname[serverId], port);
                     out.println(option);
                     out.println(filename);
                     line = in.readLine();
                     System.out.println(line);
                     this.listenSocket(host, port);
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
                     System.out.println("Text received: " + line);
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
            	  //Send filename over socket
            	  out.println(line);
            	  //Receive text from server
                  try
                  {
                     line = in.readLine();
                     System.out.println("Text received: " + line);
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
            	  //Send filename over socket
            	  out.println(line);
            	  //Receive text from server
                  try
                  {
                     line = in.readLine();
                     if(line.equals("File exists")) {
                    	 System.out.println("Enter the text you want to write:");
                   	     line = sc.nextLine();
                   	     out.println(line);
                     }
                     else {
                    	 System.out.println("Text received: " + line);
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
      if (args.length != 2)
      {
         System.out.println("Usage:  client hostname port");
	 System.exit(1);
      }

      SocketClient client = new SocketClient();
      client.hostname[0] = "dc01.utdallas.edu";
      client.hostname[1] = "dc02.utdallas.edu";
      client.hostname[2] = "dc03.utdallas.edu";

      String host = args[0];
      int port = Integer.valueOf(args[1]);
      client.listenSocket(host, port);
      client.communicate(host, port);
   }
}