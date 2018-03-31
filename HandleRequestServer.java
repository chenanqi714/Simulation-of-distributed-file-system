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

class HandleRequestServer implements Runnable 
{
   private Socket client;
   HashMap<String, List<ChunkNode>> map;
   int numOfServer;
   int maxChunkId;
   int serverId;
   
   HandleRequestServer(Socket client, HashMap<String, List<ChunkNode>> map, int serverId) 
   {
      this.client = client;
      this.map = map;
      this.numOfServer = 3;
      this.maxChunkId = 0;
      this.serverId = serverId;
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
                    	createFileUseJavaIO("server"+serverId+"/"+line);
                    	ChunkNode chunknode = new ChunkNode(maxChunkId, serverId);
                    	List<ChunkNode> list = new ArrayList<ChunkNode>();
                    	list.add(chunknode);
                    	map.put(line, list);
                    	line = "New file has been created";
    		            System.out.println(line);
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
		            	line = line + filename + "\n";
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
	            		line = in.readLine();
	            		Writer output;
	            		output = new BufferedWriter(new FileWriter(filename, true));  //clears file every time
	            		output.append(line);
	            		output.close();
	            		System.out.println("Write to file "+filename+" succeed");
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