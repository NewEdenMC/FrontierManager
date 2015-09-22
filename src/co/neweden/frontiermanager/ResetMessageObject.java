package co.neweden.frontiermanager;

public class ResetMessageObject {
	
	public World world;
	public long timeStamp;
	public String humanReadable;
	
	public ResetMessageObject(World world, Long timeStamp, String humanReadable) {
		this.world = world;
		this.timeStamp = timeStamp;
		this.humanReadable = humanReadable;
	}
	
}
