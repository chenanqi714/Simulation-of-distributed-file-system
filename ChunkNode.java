
public class ChunkNode {
	int chunkId;
	int serverId;
	int space;
	boolean outofdate;
	public ChunkNode(int chunkId, int serverId) {
		this.chunkId = chunkId;
		this.serverId = serverId;
		this.space = 80;
		this.outofdate = false;
	}

}
