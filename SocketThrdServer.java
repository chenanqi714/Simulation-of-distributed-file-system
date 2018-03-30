// By Greg Ozbirn, University of Texas at Dallas
// Adapted from example at Sun website:
// http://java.sun.com/developer/onlineTraining/Programming/BasicJava2/socket.html
// 11/07/07


import java.io.*;
import java.net.*;
import java.util.HashSet;

class SocketThrdServer 
{
   ServerSocket server = null;
   HashSet<String> filenames = new HashSet<String>();

   public void listenSocket(int port)
   {
      try
      {
	 server = new ServerSocket(port); 
	 System.out.println("Server running on port " + port + 
	                     "," + " use ctrl-C to end");
      } 
      catch (IOException e) 
      {
	 System.out.println("Error creating socket");
	 System.exit(-1);
      }
      while(true)
      {
         ClientWorker w;
         try
         {
            w = new ClientWorker(server.accept(), filenames);
            Thread t = new Thread(w);
            t.start();
	 } 
	 catch (IOException e) 
	 {
	    System.out.println("Accept failed");
	    System.exit(-1);
         }
      }
   }

   protected void finalize()
   {
      try
      {
         server.close();
      } 
      catch (IOException e) 
      {
         System.out.println("Could not close socket");
         System.exit(-1);
      }
   }

   public static void main(String[] args)
   {
      if (args.length != 1)
      {
         System.out.println("Usage: java SocketThrdServer port");
	 System.exit(1);
      }

      SocketThrdServer server = new SocketThrdServer();
      int port = Integer.valueOf(args[0]);
      server.listenSocket(port);
   }
}