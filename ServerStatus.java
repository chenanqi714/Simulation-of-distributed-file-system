
public class ServerStatus {
	boolean recover;
    boolean down;
    boolean abort;
    int numOfAgree;
    ServerStatus(boolean recover, boolean down, boolean abort, int numOfAgree){
    	this.down = down;
    	this.abort = abort;
    	this.numOfAgree = numOfAgree;
    	this.recover = recover;
    }
}
