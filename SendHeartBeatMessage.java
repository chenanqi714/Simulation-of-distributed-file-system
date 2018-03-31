import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SendHeartBeatMessage implements Runnable {
	
	   HashMap<String, List<ChunkNode>> map;
	   int port;
	   String host;
	   Socket socket = null;
	   PrintWriter out = null;
	   BufferedReader in = null;
	   
	   SendHeartBeatMessage(HashMap<String, List<ChunkNode>> map, int port, String host) 
	   {
	      this.map = map;
	      this.port = port;
	      this.host = host;
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
	   
	   public void run()
	   {
		   listenSocket(host, port);
		   boolean flag = true;
		   long time = 5000;
		   while(flag) {
			   out.println("H");
			   System.out.println("Send heart messasge");

			   try {
				   Thread.sleep(time);
			   } 
			    catch (InterruptedException e) {
				   e.printStackTrace();
			   }	
		   }		      
	   }

}
