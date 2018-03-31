import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class HandleRequestMServer implements Runnable 
{
   private Socket client;
   HashMap<String, List<ChunkNode>> map;
   int numOfServer;
   
   HandleRequestMServer(Socket client, HashMap<String, List<ChunkNode>> map) 
   {
      this.client = client;
      this.map = map;
      this.numOfServer = 3;
   }
   
   public void printMap() {
	   for(String filename: map.keySet()) {
		   List<ChunkNode> list = map.get(filename);
		   System.out.print(filename+": ");
		   for(ChunkNode n: list) {
			   System.out.println("serverId "+n.serverId+" chunkId "+n.chunkId + " space "+n.space);
		   }
	   }
   }
	

   public void run()
   {
      String line;
      BufferedReader in = null;
      PrintWriter out = null;
      try 
      {
	     in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	     out = new PrintWriter(client.getOutputStream(), true);
      } 
      catch (IOException e) 
      {
	     System.out.println("in or out failed");
	     System.exit(-1);
      }

      try 
      {
    	  
    	boolean flag = true;
    	Random rand = new Random();
    	while(flag) {
    		// Receive text from client
	        line = in.readLine();
	        if(line.isEmpty()) {
		       System.out.println("Invalid option");
		       System.exit(-1);
	        }
	        char option = line.charAt(0);
	        // Send response back to client
	        switch(option) {
	            case '1':
	            	line = in.readLine();
                    if(!map.containsKey(line)) {
                    	//createFileUseJavaIO(line);
                    	//int serverId = rand.nextInt(numOfServer);
                    	int serverId = 0;
                    	ChunkNode chunknode = new ChunkNode(-1, serverId);
                    	List<ChunkNode> list = new ArrayList<ChunkNode>();
                    	list.add(chunknode);
                    	map.put(line, list);
                    	line = String.valueOf(serverId);
    		            //System.out.println(line);
    		            out.println(line);	
                    }
                    else {
                    	line = "File name already exists";
		                System.out.println(line);
		                out.println(line);	
                    }
		            	 
		            break;
	            case '2':
	            	line = "";
		            for(String filename: map.keySet()) {
		            	line = line + filename + " ";
		            }
		            System.out.println(line);
		            out.println(line);		 
		            break;
	            case '3':
	            	line = in.readLine();
	            	String filename = line;
	            	String content = "";
	            	if(map.containsKey(line)) {
	            		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
	            		    while ((line = br.readLine()) != null) {
	            		       content += line;
	            		    }
	            		}
	            		out.println(content);	
                    }
                    else {
                    	line = "File name does not exist";
		                System.out.println(line);
		                out.println(line);	
                    }
	            	break;
	            case '4':
	            	line = in.readLine();
	            	filename = line;
	            	if(map.containsKey(line)) {
	            		line = "File exists";
	            		out.println(line);	
	            		//read bytes
	            		line = in.readLine();
	            		int bytes = Integer.parseInt(line);
	            		List<ChunkNode> list = map.get(filename);
	            		if(!list.isEmpty()) {
	            			ChunkNode lastChunk = list.get(list.size() - 1);
	            			//wait until chunkId is updated by server
	            			while(lastChunk.chunkId == -1) {
	            				continue;
	            			}
	            			if(lastChunk.chunkId != -1) {
	            				if(lastChunk.space >= bytes) {
	            					out.println(String.valueOf(lastChunk.serverId));
	            					out.println(String.valueOf(lastChunk.chunkId));
	            				}
	            				else {
	            					
	            					int serverId = 0;
	                            	ChunkNode chunknode = new ChunkNode(-1, serverId);
	                            	list.add(chunknode);
	                            	
	            					out.println(String.valueOf(serverId));
	            					out.println(String.valueOf(-1));
	            				}
	            			}
	            		}
                    }
                    else {
                    	line = "File name does not exist";
		                System.out.println(line);
		                out.println(line);	
                    }
	            	
	            	break;
	            case '5':
	            	flag = false;
	            	break;
	            case 'H':
	            	System.out.println("Get heart beat message from server");
	            	line = in.readLine();
	            	int serverId = Integer.parseInt(line);
	            	boolean end = false;
	            	while(!end) {
	            		line = in.readLine();
	            		if(line.equals("E")) {
	            			end = true;
	            			continue;
	            		}
	            		else {
	            			filename = line;
	            			//System.out.println("Get filename "+filename);
	            			line = in.readLine();
	            			//System.out.println("Get chunkId "+line);
	            			int chunkId = Integer.parseInt(line);
	            			line = in.readLine();
	            			int space = Integer.parseInt(line);
	            			List<ChunkNode> list = map.get(filename);
	            			if(!list.isEmpty()) {
	            				ChunkNode node = list.get(list.size()-1);
	            				if(node.serverId == serverId) {
	            					node.chunkId = chunkId;
	            					node.space = space;
	            					//System.out.println("Update chunkId "+chunkId);
	            				}
	            			}
	            		}
	            	}
	            	printMap();
	            	System.out.println("End of heart beat message");
	            	break;
                default:
            	    line = "Invalid option";
            	    out.println(line);	
    	            break;
	        }
    	}	    
      } 
      catch (IOException e) 
      {
	      System.out.println("Read failed");
	      System.exit(-1);
      }

      try 
      {
	      client.close();
      } 
      catch (IOException e) 
      {
	      System.out.println("Close failed");
	      System.exit(-1);
      }
   }
}