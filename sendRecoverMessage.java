import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

public class sendRecoverMessage implements Runnable {
	
	   HashMap<String, List<ChunkNode>> map;
	   int Mport;
	   int serverId;
	   String host;
	   Socket socket = null;
	   PrintWriter out = null;
	   BufferedReader in = null;
	   Semaphore sem;
	   ServerStatus status;
	   Semaphore sem_status;
	   String[] hostnames;
	   int port;
	   
	   sendRecoverMessage(HashMap<String, List<ChunkNode>> map, int Mport, String host, int serverId, Semaphore sem, ServerStatus status, String[] hostnames, int port) 
	   {
	      this.map = map;
	      this.Mport = Mport;
	      this.host = host;
	      this.serverId = serverId;
	      this.sem = sem;
	      this.status = status;
	      this.hostnames = hostnames;
	      this.port = port;
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
	   
	   public void createFileUseJavaIO(String filePath)
		{
			try 
			{
				/* First check filePath variable value. */
				if(filePath!=null && filePath.trim().length()>0)
				{
					File fileObj = new File(filePath);
					
					String absoluteFilePath = fileObj.getAbsolutePath();
					System.out.println("NewFile location is " + absoluteFilePath);
					
					/* If not exist. */
					if(!fileObj.exists())
					{		
						boolean result = fileObj.createNewFile();
						
						if(result)
						{
							System.out.println("File " + filePath + " create success. ");
						}
					}else
					{
						fileObj.delete();
						System.out.println("File " + filePath + " old copy deleted. ");
                        boolean result = fileObj.createNewFile();
						
						if(result)
						{
							System.out.println("File " + filePath + " new copy create success. ");
						}
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	   
	   public void run()
	   {
		   boolean flag = true;
		   long time = 5000;
		   while(flag) {
			   
			   if(status.recover) {
				   listenSocket(host, Mport);
				   out.println("R");
				   out.println(serverId);
				   System.out.println("Send recover messasge");

	               try {
					   String chunkid_outdate = in.readLine();
					   String serverid_recover = in.readLine();
		               String chunkid_recover = in.readLine();
		               String[] chunkid_outdate_ary = chunkid_outdate.split(",");
		               String[] serverid_recover_ary = serverid_recover.split(",");
		               String[] chunkid_recover_ary = chunkid_recover.split(",");
		               if(chunkid_outdate.isEmpty()) {
		            	   listenSocket(host, Mport);
		            	   out.println("F");
						   out.println(serverId);
						   System.out.println("Recovery finished");
		            	   status.recover = false;
		            	   status.down = false;
		            	   continue;
		               }
		               else {
		            	   // copy chunk file from other servers
		            	   for(int i = 0; i < chunkid_outdate_ary.length; ++i) {
		            		   int serverid = Integer.parseInt(serverid_recover_ary[i]);
		            		   listenSocket(hostnames[serverid], port);
		            		   out.println("T");
		            		   out.println(chunkid_recover_ary[i]);
		            		   String text = in.readLine();
		            		   createFileUseJavaIO("server"+serverId+"/"+chunkid_outdate_ary[i]);
		            		   Writer output = new BufferedWriter(new FileWriter("server"+serverId+"/"+chunkid_outdate_ary[i], true));  //clears file every time
		            		   output.append(text);
		            		   output.close();
		            		   //update space in hashmap
		            		   boolean found = false;
		            		   for(String filename: map.keySet()) {
		            			   for(ChunkNode n: map.get(filename)) {
		            				   if(n.chunkId == Integer.parseInt(chunkid_outdate_ary[i])) {
		            					   int bytes = text.getBytes("UTF-8").length;
		            					   ChunkNode newNode = new ChunkNode(-1, -1);
		            					   n.space = newNode.space - bytes;
		            					   found = true;
		            					   break;
		            				   }
		            			   }
		            			   if(found) {
		            				   break;
		            			   }
		            		   }
		            	   }
		            	   listenSocket(host, Mport);
		            	   out.println("F");
						   out.println(serverId);
						   System.out.println("Recovery finished");
						   status.recover = false;
						   status.down = false;
		               }
		            
				    } catch (IOException e) {
					   // TODO Auto-generated catch block
					  e.printStackTrace();
				    }
	            	   
	            }
			    try {
				   Thread.sleep(time);
			    } 
			    catch (InterruptedException e) {
				   e.printStackTrace();
			    }	

		   }
	   }
}
