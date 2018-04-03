import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

class SocketMServer 
{
   ServerSocket server = null;
   HashMap<String, List<ChunkNode>> map = new HashMap<String, List<ChunkNode>>();
   Semaphore sem = new Semaphore(1);
   int numOfServer = 3;
   int max_interval = 15000;
   long[] times = new long[numOfServer];
   Semaphore sem_time = new Semaphore(1);
   
   
   public void listenSocket(int port)
   {
      try
      {
	 server = new ServerSocket(port); 
	 System.out.println("MServer running on port " + port + 
	                     "," + " use ctrl-C to end");
      } 
      catch (IOException e) 
      {
	 System.out.println("Error creating socket");
	 System.exit(-1);
      }
      while(true)
      {
    	  HandleRequestMServer w;
         try
         {
            w = new HandleRequestMServer(server.accept(), map, sem, numOfServer, times, sem_time, max_interval);
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
         System.out.println("Usage: java SocketMServer port");
	 System.exit(1);
      }

      SocketMServer server = new SocketMServer();
      int port = Integer.valueOf(args[0]);
      server.listenSocket(port);
   }
}