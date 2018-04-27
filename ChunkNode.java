
public class ChunkNode {
	int chunkId;
	int serverId;
	int space;
	boolean outofdate;
	public ChunkNode(int chunkId, int serverId) {
		this.chunkId = chunkId;
		this.serverId = serverId;
		this.space = 8192;
		this.outofdate = false;
	}

}
