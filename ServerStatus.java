
public class ServerStatus {
    boolean down;
    boolean abort;
    int numOfAgree;
    ServerStatus(boolean down, boolean abort, int numOfAgree){
    	this.down = down;
    	this.abort = abort;
    	this.numOfAgree = numOfAgree;
    }
}
