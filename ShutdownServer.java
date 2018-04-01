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
					status.down = false;
					System.out.println("Server is recovered");
				}
			}
			else {
				continue;
			}
			
		}
		
	}

}
