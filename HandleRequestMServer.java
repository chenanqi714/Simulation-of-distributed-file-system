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
import java.util.concurrent.Semaphore;

class HandleRequestMServer implements Runnable 
{
   private Socket client;
   HashMap<String, List<ChunkNode>> map;
   int numOfServer;
   Semaphore sem;
   long[] times;
   Semaphore[] sem_time;
   
   HandleRequestMServer(Socket client, HashMap<String, List<ChunkNode>> map, Semaphore sem, int numOfServer, long[] times, Semaphore[] sem_time) 
   {
      this.client = client;
      this.map = map;
      this.numOfServer = numOfServer;
      this.sem = sem;
      this.times = times;
      this.sem_time = sem_time;
      for(int i = 0; i < sem_time.length; ++i) {
    	  sem_time[i] = new Semaphore(1);
      }
   }
   
   public void printMap() {
	   for(String filename: map.keySet()) {
		   List<ChunkNode> list = map.get(filename);
		   System.out.print(filename+": \n");
		   for(ChunkNode n: list) {
			   System.out.println("       serverId "+n.serverId+" chunkId "+n.chunkId+" space "+n.space);
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
    	  
    	
    	    Random rand = new Random();
    	    
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
                    	int serverId = rand.nextInt(numOfServer);
                    	
                    	try {
    					    sem_time[serverId].acquire();
    				    } catch (InterruptedException e1) {
    					    e1.printStackTrace();
    				    }
    	            	long interval = System.currentTimeMillis() - times[serverId];
    				    sem_time[serverId].release();
    				    if(interval > 15000) {
    				    	line = "Server is down";
    				    	System.out.println(line);
    		                out.println(line);
    				    }
    				    else {
    				    	ChunkNode chunknode = new ChunkNode(-1, serverId);
                        	List<ChunkNode> list = new ArrayList<ChunkNode>();
                        	list.add(chunknode);

    					    try {
    							sem.acquire();
    						} catch (InterruptedException e) {
    							e.printStackTrace();
    						}
    		
                        	map.put(line, list);
                        	sem.release();
                        	line = String.valueOf(serverId);
        		            //System.out.println(line);
        		            out.println(line);    				    	
    				    }
                    }
                    else {
                    	line = "File exists";
		                System.out.println(line);
		                out.println(line);	
                    }
		            	 
		            break;
	            case '2':
	            	line = "";
	            	
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	
		            for(String filename: map.keySet()) {
		            	line = line + filename + " ";
		            }
		            sem.release();
		            System.out.println(line);
		            out.println(line);		 
		            break;
	            case '3':
	            	line = in.readLine();
	            	String filename = line;
	            	
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            	if(map.containsKey(line)) {
	            		line = "File exists";
	            		//out.println(line);
	            		List<ChunkNode> list = map.get(filename);
	            		if(!list.isEmpty()) {
	            			ChunkNode lastChunk = list.get(list.size() - 1);
	            			//wait until chunkId is updated by server
	            			while(lastChunk.chunkId == -1) {
	            				continue;
	            			}
	            			if(lastChunk.chunkId != -1) {
	            				for(ChunkNode n: list) {
	            					try {
	            					    sem_time[n.serverId].acquire();
	            				    } catch (InterruptedException e1) {
	            					    e1.printStackTrace();
	            				    }
	            	            	long interval = System.currentTimeMillis() - times[n.serverId];
	            				    sem_time[n.serverId].release();
	            				    if(interval > 15000) {
	            				    	line = "Server is down";
	                                    break;
	            				    }
	            				}
	            				
	            				out.println(line);
	            				
	            				if(line.equals("File exists")){
	            					for(ChunkNode n: list) {
		            					out.println(n.chunkId);
		            					out.println(n.serverId);
		            				}
		            				out.println("E");
	            				}
	            				
	            			}
	            		}
	            		
                    }
	            	
                    else {
                    	line = "File name does not exist";
		                System.out.println(line);
		                out.println(line);	
                    }
	            	sem.release();
	            	break;
	            case '4':
	            	line = in.readLine();
	            	filename = line;
	            	
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
	            				
	            				try {
            					    sem_time[lastChunk.serverId].acquire();
            				    } catch (InterruptedException e1) {
            					    e1.printStackTrace();
            				    }
            	            	long interval = System.currentTimeMillis() - times[lastChunk.serverId];
            				    sem_time[lastChunk.serverId].release();
            				    if(interval > 15000) {
            				    	out.println("Server is down");
            				    }
            				    else {
            				    	if(lastChunk.space >= bytes) {
    	            					out.println("Enough space");
    	            					out.println(String.valueOf(lastChunk.serverId));
    	            					out.println(String.valueOf(lastChunk.chunkId));
    	            				}
    	            				else {   	            					
    	            					lastChunk.space = 0;   	            					
    	            					int serverId = rand.nextInt(numOfServer);
    	            					
    	            					try {
    	            					    sem_time[serverId].acquire();
    	            				    } catch (InterruptedException e1) {
    	            					    e1.printStackTrace();
    	            				    }
    	            	            	interval = System.currentTimeMillis() - times[serverId];
    	            				    sem_time[serverId].release();
    	            				    if(interval > 15000) {
    	            				    	out.println("Server is down");
    	            				    }
    	            				    else {
    	            				    	out.println("Not enough space");
        	            					out.println(String.valueOf(lastChunk.serverId));
        	            					out.println(String.valueOf(lastChunk.chunkId));
    	            				    	         				    	
    	            				    	ChunkNode chunknode = new ChunkNode(-1, serverId);
        	                            	list.add(chunknode);
        	                            	
        	            					out.println(String.valueOf(serverId));
        	            					out.println(String.valueOf(-1));
    	            				    }
    	            					
    	                            	
    	            				}
            				    	
            				    }
	            				
	            				
	            			}
	            		}
                    }
                    else {
                    	line = "File name does not exist";
		                System.out.println(line);
		                out.println(line);	
                    }
	            	sem.release();
	            	break;
	            case 'H':
	            	line = in.readLine();
	            	int serverId = Integer.parseInt(line);
	            	System.out.println("Get heart beat message from server"+serverId);
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
	            			
	            			try {
	    						sem.acquire();
	    					} catch (InterruptedException e) {
	    						e.printStackTrace();
	    					}
	            			List<ChunkNode> list = map.get(filename);
	            			if(!list.isEmpty()) {
	            				ChunkNode node = list.get(list.size()-1);
	            				if(node.serverId == serverId) {
	            					node.chunkId = chunkId;
	            					node.space = space;
	            					//System.out.println("Update chunkId "+chunkId);
	            				}
	            			}
	            			
	            			sem.release();
	            		}
	            	}
	            	
				    try {
					    sem_time[serverId].acquire();
				    } catch (InterruptedException e1) {
					    e1.printStackTrace();
				    }
	            	times[serverId] = System.currentTimeMillis();
	            	System.out.println("Server" + serverId+ " last updated: "+times[serverId]);
				    sem_time[serverId].release();
				    
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            	printMap();
	            	sem.release();
	            	System.out.println("End of heart beat message");
	            	break;
                default:
            	    line = "Invalid option";
            	    out.println(line);	
    	            break;
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