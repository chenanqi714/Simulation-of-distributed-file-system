import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SocketServer {
	
	   ServerSocket server = null;
	   HashMap<String, List<ChunkNode>> map = new HashMap<String, List<ChunkNode>>();
	   MaxChunkId id = new MaxChunkId();
	   Semaphore sem = new Semaphore(1);
	   List<Semaphore> sem_files = new ArrayList<Semaphore>();
	   ServerStatus status = new ServerStatus(false, false, false, 0);
	   String[] hostnames = new String[5];

	   public void listenSocket(String hostname, int Mport, int port, int serverId)
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
	      
	      File file = new File("server"+serverId+"/");
	      for (File f : file.listFiles()) {
	            f.delete();
	      }
	      

	      SendHeartBeatMessage h;
		  h = new SendHeartBeatMessage(map, Mport, hostname, serverId, sem, status);
		  Thread heart = new Thread(h);
		  heart.start();
          
		  ShutdownServer s;
		  s = new ShutdownServer(status);
		  Thread shutDown = new Thread(s);
		  shutDown.start();
		  
		  sendRecoverMessage r;
		  r = new sendRecoverMessage(map, Mport, hostname, serverId, sem, status, hostnames, port);
		  Thread recover = new Thread(r);
		  recover.start();
	      
	      while(true)
	      {
	         HandleRequestServer w;
	         try
	         {
	            w = new HandleRequestServer(server.accept(), map, serverId, id, sem, sem_files, status);
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
	
	
	public static void main(String[] args) {
		if (args.length != 4)
	      {
	         System.out.println("Usage: java SocketServer MserverHostname MserverPort port serverId");
		     System.exit(1);
	      }

	      SocketServer server = new SocketServer();
	      String Mhostname = args[0];
	      int Mport = Integer.valueOf(args[1]);
	      int port = Integer.valueOf(args[2]);
	      int serverId = Integer.valueOf(args[3]);
	      server.hostnames[0] = "dc01.utdallas.edu";
	      server.hostnames[1] = "dc02.utdallas.edu";
	      server.hostnames[2] = "dc03.utdallas.edu";
	      server.hostnames[3] = "dc04.utdallas.edu";
	      server.hostnames[4] = "dc05.utdallas.edu";
	      server.listenSocket(Mhostname, Mport, port, serverId);

	}

}
