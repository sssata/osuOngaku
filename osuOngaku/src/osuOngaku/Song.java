package osuOngaku;

public class Song {
	
	String [] data = new String [7];
	String [] label = {
			"Title",
			"TitleUnicode",
			"Artist",
			"ArtistUnicode",
			"Creator",
			"Source",
			"Tags"};
	String songFolderName;
	String audioFilename;
	String bgFilename;
	boolean isManiaTaiko;
	
	public Song() {
		for (int i=0; i<data.length; i++) {
			data[i] = null;
		}
		isManiaTaiko = false;
	}
	
	public String[] getData() {
		return data;
	}

	public void setData(String[] data) {
		this.data = data;
	}

	public String[] getLabel() {
		return label;
	}

	public void setLabel(String[] label) {
		this.label = label;
	}

	public String getSongFolderName() {
		return songFolderName;
	}

	public void setSongFolderName(String songFolderName) {
		this.songFolderName = songFolderName;
	}

	public String getAudioFilename() {
		return audioFilename;
	}

	public void setAudioFilename(String audioFilename) {
		this.audioFilename = audioFilename;
	}

	public String getBgFilename() {
		return bgFilename;
	}

	public void setBgFilename(String bgFilename) {
		this.bgFilename = bgFilename;
	}

	public boolean isManiaTaiko() {
		return isManiaTaiko;
	}

	public void setManiaTaiko(boolean isManiaTaiko) {
		this.isManiaTaiko = isManiaTaiko;
	}


	
}

