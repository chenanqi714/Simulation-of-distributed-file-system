import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

class HandleRequestMServer implements Runnable 
{
   private Socket client;
   HashMap<String, List<ChunkNode[]>> map;
   int numOfServer;
   Semaphore sem;
   long[] times;
   Semaphore sem_time;
   int max_interval;
   int numOfCopy;
   
   HandleRequestMServer(Socket client, HashMap<String, List<ChunkNode[]>> map, Semaphore sem, int numOfServer, long[] times, Semaphore sem_time, int max_interval, int numOfCopy) 
   {
      this.client = client;
      this.map = map;
      this.numOfServer = numOfServer;
      this.sem = sem;
      this.times = times;
      this.sem_time = sem_time;
      this.max_interval = max_interval;
      this.numOfCopy = numOfCopy;
   }
   
   public void printMap() {
	   for(String filename: map.keySet()) {
		   List<ChunkNode[]> list = map.get(filename);
		   System.out.print(filename+": \n");
		   for(ChunkNode[] chunk_ary: list) {
			   for(ChunkNode n : chunk_ary) {
				   System.out.println("       serverId "+n.serverId+" chunkId "+n.chunkId+" space "+n.space);
			   }
			   System.out.println();
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
                    	
                    	List<Integer> serverIds = new ArrayList<Integer>();
                    	try {
    					    sem_time.acquire();
    				    } catch (InterruptedException e1) {
    					    e1.printStackTrace();
    				    }
                    	for(int i = 0; i < numOfServer; ++i) {
                    		long interval = System.currentTimeMillis() - times[i];
                    		System.out.println("interval is "+interval);
                    		System.out.println("Maxinterval is "+max_interval);
                    		if(interval < max_interval) {
                    			serverIds.add(i);
                    		}
                    	}
    				    sem_time.release();
    				    if(serverIds.size() < 3) {
    				    	line = "More than two servers are down, cannot create new file";
    				    	System.out.println(line);
    		                out.println(line);
    				    }
    				    else {
    				    	StringBuilder builder = new StringBuilder();
    				    	ChunkNode[] chunk_ary = new ChunkNode[numOfCopy];
    				    	Collections.shuffle(serverIds);
    				    	for(int i = 0; i < numOfCopy; ++i) {
    				    		builder.append(serverIds.get(i)+",");
    				    		ChunkNode chunknode = new ChunkNode(-1, serverIds.get(i));
    				    		chunk_ary[i] = chunknode;
    				    	}
                        	List<ChunkNode[]> list = new ArrayList<ChunkNode[]>();
                        	list.add(chunk_ary);

    					    try {
    							sem.acquire();
    						} catch (InterruptedException e) {
    							e.printStackTrace();
    						}
    		
                        	map.put(line, list);
                        	sem.release();
                        	line = builder.toString();
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
	            		List<ChunkNode[]> list = map.get(filename);
	            		if(!list.isEmpty()) {
	            			List<ChunkNode> list_selected = new ArrayList<ChunkNode>();
	            		    for(ChunkNode[] chunk_ary: list) {
	            				List<Integer> set = new ArrayList<Integer>();
	            				for(int i = 0; i < numOfCopy; ++i) {
	            					try {
		            					sem_time.acquire();
		            				} catch (InterruptedException e1) {
		            					e1.printStackTrace();
		            				}
		            	            long interval = System.currentTimeMillis() - times[chunk_ary[i].serverId];
		            				sem_time.release();
		            				if(interval <= max_interval && chunk_ary[i].chunkId != -1) {
		            				   set.add(i);
		            				}	            						
	            			    }
	            				if(set.size() > 0) {
	            					int i = rand.nextInt(set.size());
	            					list_selected.add(chunk_ary[set.get(i)]);
	            				}
	            				else {
	            					line = "All three servers are down or mapping has not been updated, cannot read file";
	            					break;
	            				}
	            			}
	            				
	            		    out.println(line);
	            				
	            		    if(line.equals("File exists")){
	            				for(ChunkNode n: list_selected) {
		            				out.println(n.chunkId);
		            				out.println(n.serverId);
		            			}
		            			out.println("E");
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
	            		List<ChunkNode[]> list = map.get(filename);
	            		if(!list.isEmpty()) {
	            			ChunkNode[] chunk_ary = list.get(list.size() - 1);
	            			StringBuilder builder_serverId = new StringBuilder();
	            			StringBuilder builder_chunkId = new StringBuilder();
	            			for(int i = 0; i < numOfCopy; ++i) {
	            				try {
            					    sem_time.acquire();
            				    } catch (InterruptedException e1) {
            					    e1.printStackTrace();
            				    }
            	            	long interval = System.currentTimeMillis() - times[chunk_ary[i].serverId];
            				    sem_time.release();
            				    if(interval > max_interval && chunk_ary[i].space != 0) {
            				    	line = "Server"+chunk_ary[i].serverId+" is down, cannot write to chunk file";
            				    	break;
            				    }
            				    else {
            				    	if(chunk_ary[i].space >= bytes && chunk_ary[i].chunkId != -1) {
            				    		if(i == 0) {
            				    			line = "Enough space";
            				    		}
            				    		builder_serverId.append(chunk_ary[i].serverId+",");
            				    		builder_chunkId.append(chunk_ary[i].chunkId+",");
    	            				}
    	            				else if(chunk_ary[i].space < bytes && chunk_ary[i].chunkId != -1){   	            					
    	            					chunk_ary[i].space = 0;
    	            				    
    	            				    if(i == 0) {
    	            				    	line = "Not enough space";
                				        }
    	            				    builder_serverId.append(chunk_ary[i].serverId+",");
        	            				builder_chunkId.append(chunk_ary[i].chunkId+",");
    	            					
    	                            	
    	            				}
    	            				else {
    	            					line = "Mapping has not been updated, cannot write to chunk file";
    	            					break;
    	            				}
            				    	
            				    }
	            				
	            			}
	            			if(line.equals("Not enough space")) {
	            				List<Integer> serverIds = new ArrayList<Integer>();
	                        	try {
	        					    sem_time.acquire();
	        				    } catch (InterruptedException e1) {
	        					    e1.printStackTrace();
	        				    }
	                        	for(int i = 0; i < numOfServer; ++i) {
	                        		long interval = System.currentTimeMillis() - times[i];
	                        		if(interval < max_interval) {
	                        			serverIds.add(i);
	                        		}
	                        	}
	        				    sem_time.release();
	        				    if(serverIds.size() < 3) {
	        				    	line = "More than two servers are down, cannot create new chunk";
	        				    	System.out.println(line);
	        		                out.println(line);
	        				    }
	        				    else {
	        				    	out.println(line);
	        				    	out.println(builder_serverId.toString());
		        					out.println(builder_chunkId.toString()); 
		        					
	        				    	StringBuilder builder_serverId_new = new StringBuilder();
	        				    	ChunkNode[] chunk_ary_new = new ChunkNode[numOfCopy];
	        				    	Collections.shuffle(serverIds);
	        				    	for(int i = 0; i < numOfCopy; ++i) {
	        				    		builder_serverId_new.append(serverIds.get(i)+",");
	        				    		ChunkNode chunknode = new ChunkNode(-1, serverIds.get(i));
	        				    		chunk_ary_new[i] = chunknode;
	        				    	}

	        					    list.add(chunk_ary_new);
	                            	map.put(filename, list);
	                            	
	                            	line = builder_serverId_new.toString();

	            		            out.println(line);    				    	
	        				    }
	            			}
	            			else if(line.equals("Enough space")){
	            				out.println(line);
		            			out.println(builder_serverId.toString());
	        					out.println(builder_chunkId.toString());         				
	            			}
	            			else {//server is down
	            				out.println(line);
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
	            	try {
					    sem_time.acquire();
				    } catch (InterruptedException e1) {
					    e1.printStackTrace();
				    }
	            	times[serverId] = System.currentTimeMillis();
	            	System.out.println("Server" + serverId+ " last updated: "+times[serverId]);
				    sem_time.release();
	            	boolean end = false;
	            	while(!end) {
	            		line = in.readLine();
	            		if(line.equals("E")) {
	            			end = true;
	            			continue;
	            		}
	            		else {
	            			filename = line;

	            			line = in.readLine();

	            			int chunkId = Integer.parseInt(line);
	            			line = in.readLine();
	            			int space = Integer.parseInt(line);
	            			
	            			try {
	    						sem.acquire();
	    					} catch (InterruptedException e) {
	    						e.printStackTrace();
	    					}
	            			List<ChunkNode[]> list = map.get(filename);
	            			if(!list.isEmpty()) {
	            				ChunkNode[] chunk_ary = list.get(list.size()-1);
	            				for(ChunkNode node: chunk_ary) {
	            					if(node.serverId == serverId) {
	            					   node.chunkId = chunkId;
	            					   node.space = space;
	            				    }
	            				}
	            			}	            			
	            			sem.release();
	            		}
	            	}
	            	
				    
				    
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