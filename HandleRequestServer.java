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
import java.util.concurrent.Semaphore;

class HandleRequestServer implements Runnable 
{
   private Socket client;
   HashMap<String, List<ChunkNode>> map;
   int numOfServer;
   MaxChunkId maxId;
   int serverId;
   Semaphore sem;
   List<Semaphore> sem_files;
   
   HandleRequestServer(Socket client, HashMap<String, List<ChunkNode>> map, int serverId, MaxChunkId id, Semaphore sem, List<Semaphore> sem_files) 
   {
      this.client = client;
      this.map = map;
      this.numOfServer = 3;
      this.maxId = id;
      this.serverId = serverId;
      this.sem = sem;
      this.sem_files = sem_files;
   }
   
   public void printMap() {
	   for(String filename: map.keySet()) {
		   List<ChunkNode> list = map.get(filename);
		   System.out.print(filename+": \n");
		   for(ChunkNode n: list) {
			   System.out.println("       serverId "+n.serverId+" chunkId "+n.chunkId+" space "+n.space);
			   //System.out.println("MaxChunkId is "+maxId.id);
		   }
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
					System.out.println("File exist. ");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
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
    	  
    		// Receive text from client
	        line = in.readLine();
	        if(line.isEmpty()) {
		       System.out.println("Invalid option");
		       System.exit(-1);
	        }
	        char option = line.charAt(0);
	        // Send response back to client
	        switch(option) {
	            case '0':
	            	line = in.readLine();
	            	String filename = line;
	            	line = in.readLine();
	            	int chunkId = Integer.parseInt(line);
	            	
	            	try {
						sem.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            	List<ChunkNode> list = map.get(filename);
	            	ChunkNode node = null;
	            	for(ChunkNode n : list) {
		            	if(n.chunkId == chunkId) {
		            		 node = n;
		            		 break;
		            	}
		            }

	            	Writer output;
	            			
	            	Semaphore sem_file = sem_files.get(chunkId);
	            	try {
	    			    sem_file.acquire();
	    			} catch (InterruptedException e1) {
	    			    e1.printStackTrace();
	    		    }
	            	output = new BufferedWriter(new FileWriter("server"+serverId+"/"+chunkId, true));  //clears file every time
	                while(node.space > 0) {
	                	output.append('\0');
	                	node.space = node.space -1;
	                }
	                output.close();
	            	sem_file.release();
	            	sem.release();
	                  	
	            	break;
	            case '1':
	            	line = in.readLine();
	            	
				    try {
					   sem.acquire();
				    } catch (InterruptedException e) {
					   e.printStackTrace();
				    }
                    if(!map.containsKey(line)) {
                    	ChunkNode chunknode = new ChunkNode(maxId.id, serverId);
                    	createFileUseJavaIO("server"+serverId+"/"+maxId.id);
                    	list = new ArrayList<ChunkNode>();
                    	list.add(chunknode);
                    	map.put(line, list);
                    	maxId.id++;
                    	
                    	sem_file = new Semaphore(1);
                    	sem_files.add(sem_file);
                    	
                    	line = "New file has been created";
    		            System.out.println(line);
    		            out.println(line);	
                    }
                    sem.release();           	 
		            break;
	            case '3':
	            	line = in.readLine();
	            	String content = "";
	            	
	            	chunkId = Integer.parseInt(line);
	            	sem_file = sem_files.get(chunkId);
				
	            	try {
					    sem_file.acquire();
				    } catch (InterruptedException e1) {
					    e1.printStackTrace();
				    }
	            	try (BufferedReader br = new BufferedReader(new FileReader("server"+serverId+"/"+line))) {
	            		while ((line = br.readLine()) != null) {
	            		    content += line;
	            		}
	            	}
	            	sem_file.release();
	            	out.println(content);	
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
	            		line = in.readLine();
	            		chunkId = Integer.parseInt(line);
	            		line = in.readLine();
	            		if(chunkId != -1) {
	            			
	            			sem_file = sem_files.get(chunkId);
	            			try {
	    					    sem_file.acquire();
	    				    } catch (InterruptedException e1) {
	    					    e1.printStackTrace();
	    				    }
	            		    output = new BufferedWriter(new FileWriter("server"+serverId+"/"+chunkId, true));  //clears file every time
	            		    output.append(line);
	            		    output.close();
	            		    sem_file.release();
	            		    int bytes = line.getBytes("UTF-8").length;
	            		    list = map.get(filename);
	            		    for(ChunkNode n : list) {
	            		    	if(n.chunkId == chunkId) {
	            		    		n.space = n.space - bytes;
	            		    		break;
	            		    	}
	            		    }
	            		}
	            		else {
	            			chunkId = maxId.id;
	            			ChunkNode chunknode = new ChunkNode(maxId.id, serverId);
	                    	createFileUseJavaIO("server"+serverId+"/"+maxId.id);
	                    	list = map.get(filename);
	                    	list.add(chunknode);
	                    	maxId.id++;
	            			
	                    	sem_file = new Semaphore(1);
	                    	sem_files.add(sem_file);
	            			
	            			try {
	    					    sem_file.acquire();
	    				    } catch (InterruptedException e1) {
	    					    e1.printStackTrace();
	    				    }
	            		    output = new BufferedWriter(new FileWriter("server"+serverId+"/"+chunkId, true));  //clears file every time
	            		    output.append(line);
	            		    output.close();
	            		    sem_file.release();
	            		    int bytes = line.getBytes("UTF-8").length;
	            		    chunknode.space = chunknode.space - bytes;	            		    
	            		}
	            		
	            		line = "Write to file "+filename+" succeed";
	            		out.println(line);
	            		System.out.println(line);
                    }
                    else {
                    	line = in.readLine();
	            		chunkId = maxId.id;
                    	
                    	ChunkNode chunknode = new ChunkNode(maxId.id, serverId);
                    	createFileUseJavaIO("server"+serverId+"/"+maxId.id);
                    	list = new ArrayList<ChunkNode>();
                    	list.add(chunknode);
                    	map.put(filename, list);
                    	maxId.id++;
                    	
                    	sem_file = new Semaphore(1);
                    	sem_files.add(sem_file);
                    	                    	
	            		line = in.readLine();
	            		
	            		try {
    					    sem_file.acquire();
    				    } catch (InterruptedException e1) {
    					    e1.printStackTrace();
    				    }
	            		output = new BufferedWriter(new FileWriter("server"+serverId+"/"+chunkId, true));  //clears file every time
	            		output.append(line);
	            		output.close();
	            		sem_file.release();
	            		int bytes = line.getBytes("UTF-8").length;
	            		chunknode.space = chunknode.space - bytes;	            		    
	            		
	            		line = "Write to file "+filename+" succeed";
	            		out.println(line);
	            		System.out.println(line);
                    }
	            	sem.release();
	            	
	            	break;
                default:
            	    line = "Invalid option";
            	    out.println(line);	
    	            break;
	        }
	        //printMap();
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