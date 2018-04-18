import java.util.Scanner;

public class ShutdownServer implements Runnable{
	ServerStatus status;
	public ShutdownServer(ServerStatus status) {
		this.status = status;
	}
	@Override
	public void run() {
		while(true) {
			Scanner sc = new Scanner(System.in);
			if(sc.hasNext()) {
				String line = sc.nextLine();
				if(line.equals("down")) {
					status.down = true;
					System.out.println("Server is down");
				}
				else if(line.equals("recover")) {
					status.recover = true;
					System.out.println("Server is recovered");
				}
				else if(line.equals("abort")) {
					status.abort = true;
					System.out.println("Server will abort commit");
				}
				else if(line.equals("agree")) {
					status.abort = false;
					System.out.println("Server will agree commit");
				}
			}
			else {
				continue;
			}
			
		}
		
	}

}
