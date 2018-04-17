import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

public class SendCommitRequest implements Runnable{
	
	   int port;
	   String hostname;
	   int serverId;
	   Socket socket = null;
	   PrintWriter out = null;
	   BufferedReader in = null;
	   ServerStatus status;
	   Semaphore sem_abort;
	   
	   SendCommitRequest(String hostname, int port, int serverId, ServerStatus status, Semaphore sem_abort) 
	   {
	      this.hostname = hostname;
		  this.port = port;
		  this.serverId = serverId;
		  this.sem_abort = sem_abort;
		  this.status = status;
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
		    out.println("R");
		    System.out.println("Send commit request to server"+serverId);
		    try {
				String line = in.readLine();
				if(line.equals("Agree")) {
					System.out.println("Get agree from server"+serverId);
					try {
					    sem_abort.acquire();
					    status.numOfAgree++;
				    } catch (InterruptedException e1) {
					    e1.printStackTrace();
				    }
					sem_abort.release();
				}
				else {
					System.out.println("Get abort from server"+serverId);
					try {
					    sem_abort.acquire();
					    status.abort = true;
				    } catch (InterruptedException e1) {
					    e1.printStackTrace();
				    }
					sem_abort.release();
				}
			} catch (IOException e) {
    
				e.printStackTrace();
			}   
				   
	      
	   }

}
