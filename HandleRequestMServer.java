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
				   System.out.println("       serverId "+n.serverId+" chunkId "+n.chunkId+" space "+n.space+ " outofdate "+n.outofdate);
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
	            		sem.release();
	            		line = "File exists";
	            		out.println(line);	
	            		//read bytes
	            		line = in.readLine();
	            		int bytes = Integer.parseInt(line);
	            		try {
							sem.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	            		List<ChunkNode[]> list = map.get(filename);
	            		if(!list.isEmpty()) {
	            			ChunkNode[] chunk_ary = list.get(list.size() - 1);
	            			StringBuilder builder_serverId = new StringBuilder();
	            			StringBuilder builder_chunkId = new StringBuilder();
	            			List<ChunkNode> down_list = new ArrayList<ChunkNode>();
	            			for(int i = 0; i < numOfCopy; ++i) {
	            				try {
            					    sem_time.acquire();
            				    } catch (InterruptedException e1) {
            					    e1.printStackTrace();
            				    }
            	            	long interval = System.currentTimeMillis() - times[chunk_ary[i].serverId];
            				    sem_time.release();
            				    if(interval > max_interval && chunk_ary[i].space != 0) {
            				    	down_list.add(chunk_ary[i]);
            				    	//chunk_ary[i].outofdate = true;
            				    }
            				    else {
            				    	if(chunk_ary[i].space >= bytes && chunk_ary[i].chunkId != -1) {
            				    		line = "Enough space";
            				    		builder_serverId.append(chunk_ary[i].serverId+",");
            				    		builder_chunkId.append(chunk_ary[i].chunkId+",");
    	            				}
    	            				else if(chunk_ary[i].space < bytes && chunk_ary[i].chunkId != -1){   	            					   	            				    
    	            				    line = "Not enough space";
    	            				    builder_serverId.append(chunk_ary[i].serverId+",");
        	            				builder_chunkId.append(chunk_ary[i].chunkId+",");
    	            				}
    	            				else {
    	            					line = "Mapping has not been updated, cannot write to chunk file";
    	            					break;
    	            				}
            				    	
            				    }
	            				
	            			}
	            			if(down_list.size() == numOfCopy) {
	            				line = "All three copies are unavailable";
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
		        					line = in.readLine();
		        					if(line.equals("Commit")) {
		        						
		        						for(ChunkNode n: down_list) {
			            					n.outofdate = true;
			            				}
		        						
		        						String[] serverIds_old = builder_serverId.toString().split(",");
		        						String[] chunkIds_old = builder_chunkId.toString().split(",");
		        						for(int i = 0; i < serverIds_old.length; ++i) {
		        							for(ChunkNode n: chunk_ary) {
		        								if(n.serverId == Integer.parseInt(serverIds_old[i]) && n.chunkId ==  Integer.parseInt(chunkIds_old[i])) {
		        									n.space = 0;
		        									break;
		        								}
		        							}
		        						}
		        						
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
	                            	    System.out.println("New server ids are: "+line);
	            		                out.println(line);    		
		        					}        				    			    	
	        				    }
	            			}
	            			else if(line.equals("Enough space")){
	            				out.println(line);
		            			out.println(builder_serverId.toString());
	        					out.println(builder_chunkId.toString());   
	        					
	        					line = in.readLine();
	        					
                                if(line.equals("Commit")) {	        						
	        						for(ChunkNode n: down_list) {
		            					n.outofdate = true;
		            				}
	        					}
	        					System.out.println(line);
	            			}
	            			else {//server is down
	            				out.println(line);
	            			}
            				    
	            		}
	            		sem.release();
                    }
                    else {
                    	sem.release();
                    	line = "File name does not exist";
		                System.out.println(line);
		                out.println(line);	
                    }
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
	            	try {
					    sem_time.acquire();
				    } catch (InterruptedException e1) {
					    e1.printStackTrace();
				    }
	            	times[serverId] = System.currentTimeMillis();
	            	System.out.println("Server" + serverId+ " last updated: "+times[serverId]);
				    sem_time.release();
	            	System.out.println("End of heart beat message");
	            	break;
	            case 'R':
	            	line = in.readLine();
	            	serverId = Integer.parseInt(line);
	            	System.out.println("Get recovery message from server"+serverId);
	            	StringBuilder builder_chunkid_outdate = new StringBuilder();
	            	StringBuilder builder_serverid_recover = new StringBuilder();
	            	StringBuilder builder_chunkid_recover = new StringBuilder();
	            	HashMap<Integer, ChunkNode> recover_map = new HashMap<Integer, ChunkNode>();
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            	for(String fname: map.keySet()) {
	            		List<ChunkNode[]> list = map.get(fname);
        			    for(ChunkNode[] chunk_ary: list) {
        			       int chunkid = -1;
        				   for(ChunkNode node: chunk_ary) { 					  
        					  if(node.serverId == serverId && node.outofdate == true ) {
        					     chunkid = node.chunkId;
        					     recover_map.put(chunkid, null);
        					     break;
        				      }
        				   }
        				   if(chunkid != -1) {
        					   for(ChunkNode node: chunk_ary) {
        						  
        						  try {
                  					    sem_time.acquire();
                  				  } catch (InterruptedException e1) {
                  					    e1.printStackTrace();
                  				  }
                  	              long interval = System.currentTimeMillis() - times[node.serverId];
                  				  sem_time.release();
        						  
             					  if(node.serverId != serverId && node.outofdate == false && interval < max_interval) {
             						 recover_map.put(chunkid, node);
             					     break;
             				      }
             				   }
        				   }
        			    }
	            	}        				            			
        			sem.release();
        			boolean success = true;
	            	for(int chunkid: recover_map.keySet()) {
	            		if(recover_map.get(chunkid) != null) {
	            			builder_chunkid_outdate.append(chunkid+",");
	            		    builder_serverid_recover.append(recover_map.get(chunkid).serverId+",");
	            		    builder_chunkid_recover.append(recover_map.get(chunkid).chunkId+",");
	            		}
	            		else {
	            			success = false;
	            			break;
	            		}
	            		
	            	}
	            	if(success) {
	            		out.println("success");
	            		out.println(builder_chunkid_outdate.toString());
	            	    out.println(builder_serverid_recover.toString());
	            	    out.println(builder_chunkid_recover.toString());
	            	}
	            	else {
	            		out.println("fail");
	            	}           	
	            	
	            	break;
	            case 'F':
	            	line = in.readLine();
	            	serverId = Integer.parseInt(line);
	            	System.out.println("Server"+serverId+" recovery finished");
	            	
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            	for(String fname: map.keySet()) {
	            		List<ChunkNode[]> list = map.get(fname);
        			    for(ChunkNode[] chunk_ary: list) {
        			       ChunkNode temp = null;
        				   //set outofdate flag to false after chunk file is recovered
        			       for(ChunkNode node: chunk_ary) {
        					  if(node.serverId == serverId && node.outofdate == true) {
        					     node.outofdate = false;
        					     temp = node;
        					     break;
        				      }
        				   }
        				   //update space after chunk file is recovered
        				   if(temp != null) {
        					   for(ChunkNode node: chunk_ary) {
             					  if(node.serverId != serverId && node.outofdate == false) {             					     
             					     temp.space = node.space;
             					     break;
             				      }
             				   }
        				   }
        			    }
	            	}        				            			
        			sem.release();
	            	
	            	
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