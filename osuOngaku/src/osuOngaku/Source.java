package osuOngaku;

public class Source {
	String sourceName = "";
	int noOfSongs;
	
	public Source (String newSource, int number) {
		sourceName = newSource;
		noOfSongs = number;
	}
	
	public Source (String newSource) {
		sourceName = newSource;
		noOfSongs = 1;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public int getNoOfSongs() {
		return noOfSongs;
	}
	public void setNoOfSongs(int noOfSongs) {
		this.noOfSongs = noOfSongs;
	}
	
}
