import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;

public class SocketServer {
	
	   ServerSocket server = null;
	   HashMap<String, List<ChunkNode>> map = new HashMap<String, List<ChunkNode>>();

	   public void listenSocket(int port, int serverId)
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
	      

	      SendHeartBeatMessage h;
		  String host = "csgrads1.utdallas.edu";
		  h = new SendHeartBeatMessage(map, port, host, serverId);
		  Thread heart = new Thread(h);
		  heart.start();

	      
	      while(true)
	      {
	         HandleRequestServer w;
	         try
	         {
	            w = new HandleRequestServer(server.accept(), map, serverId);
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
		if (args.length != 2)
	      {
	         System.out.println("Usage: java SocketServer port serverId");
		 System.exit(1);
	      }

	      SocketServer server = new SocketServer();
	      int port = Integer.valueOf(args[0]);
	      int serverId = Integer.valueOf(args[1]);
	      server.listenSocket(port, serverId);

	}

}
