import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendCommitRequest implements Runnable{
	
	   int port;
	   String hostname;
	   int serverId;
	   Socket socket = null;
	   PrintWriter out = null;
	   BufferedReader in = null;
	   
	   SendCommitRequest(String hostname, int port, int serverId) 
	   {
	      this.hostname = hostname;
		  this.port = port;
		  this.serverId = serverId;
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

		    listenSocket(hostname, port);
		    out.println("C");
		    System.out.println("Send commit request to server"+serverId);
				   
				   
	      
	   }

}
