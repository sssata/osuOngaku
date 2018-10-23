package osuOngaku;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;


public class MainClass extends JPanel implements ActionListener{

	static JFrame frame;
	
	JButton browseInputButton;
	JButton browseOutputButton;
	JButton startButton;
	
	JCheckBox useRenameFileCheckbox;
	JCheckBox useOsuMetadataCheckbox;
	JCheckBox useUnicodeCheckbox;
	JCheckBox useAlbumArtCheckbox;
	JCheckBox useRemoveDuplicatesCheckbox;
	JCheckBox checkExistingCheckbox;

	JTextField inputPathTF;
	JTextField outputPathTF;

	JLabel inputLabel;
	JLabel outputLabel;
	JLabel creditsLabel;

	String inputFolderPath; //osu! Song Folder Path
	String outputFolderPath; //Output Folder Path
	
	JProgressBar progressBar;
	
	BufferedImage background;
	ArrayList<BufferedImage> iconList;

	JFileChooser fc;
	
	FileWriter fw;
	OutputStreamWriter outputWriter;
	BufferedWriter bw;
	
	boolean useMetadataWasEnabled;

	ArrayList<Song> SongList; // List of songs including metadata and paths
	ArrayList<Song> ExistingSongList;
	
	String [] imageFormats = {
		"jpg",
		"png",
		"jpeg"
	};
	
	int albumArtDimension = 500;
	
	
	
	public static void main(String[] args) {
		System.out.println("nice");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
					        UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				createAndShowGUI(); // where it all starts
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(background, (this.getWidth()-background.getWidth())/2, -300, this);
	}

	public MainClass () {
		
		// UI Components
		browseInputButton = new JButton ("Browse");
		browseOutputButton = new JButton ("Browse");
		startButton = new JButton ("Start!");
		
		useRenameFileCheckbox = new JCheckBox("Rename file to beatmap folder name");
		useRenameFileCheckbox.setSelected(true);
		useRenameFileCheckbox.setOpaque(false);
		useRenameFileCheckbox.setToolTipText("Keep this enabled, otherwise songs with the same file name will overwrite each other");
		
		useOsuMetadataCheckbox = new JCheckBox("Overwrite MP3 metadata with osu! metadata");
		useOsuMetadataCheckbox.setSelected(true);
		useMetadataWasEnabled = true;
		useOsuMetadataCheckbox.setOpaque(false);
		
		useUnicodeCheckbox = new JCheckBox("Use unicode metadata");
		useUnicodeCheckbox.setSelected(true);
		useUnicodeCheckbox.setOpaque(false);
		
		useAlbumArtCheckbox = new JCheckBox("Set beatmap background as MP3 album art");
		useAlbumArtCheckbox.setSelected(true);
		useAlbumArtCheckbox.setOpaque(false);
		
		useRemoveDuplicatesCheckbox = new JCheckBox("Ignore duplicate songs with same title and artist");
		useRemoveDuplicatesCheckbox.setSelected(false);
		useRemoveDuplicatesCheckbox.setOpaque(false);
		useRemoveDuplicatesCheckbox.setToolTipText("Sorry, this feature doesn't work yet...");
		
		checkExistingCheckbox = new JCheckBox("Don't process songs already in folder");
		checkExistingCheckbox.setSelected(false);
		checkExistingCheckbox.setOpaque(false);
		//checkExistingCheckbox.setToolTipText("");

		inputPathTF = new JTextField();
		outputPathTF = new JTextField();

		inputLabel = new JLabel("osu! Song Folder:");
		outputLabel = new JLabel("Destination Folder:");
		creditsLabel = new JLabel("by sssata/poiuyos");

		useOsuMetadataCheckbox.addActionListener(this);
		browseInputButton.addActionListener(this);
		browseOutputButton.addActionListener(this);
		startButton.addActionListener(this);
		
		progressBar = new JProgressBar(0);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setString("Ready");
		progressBar.setEnabled(false);
		
		// Variables and Data
		inputFolderPath = "";
		outputFolderPath = "";
		
		fc = new JFileChooser();
		
		SongList = new ArrayList<Song>();
		
		
		// JPanel Group Layout bullshittery
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.CENTER) // overall
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING) // top half
						.addGroup(layout.createSequentialGroup() // input selection
								.addComponent(inputLabel)
								.addComponent(inputPathTF, 350, 350, 350)
								.addComponent(browseInputButton))
						.addGroup(layout.createSequentialGroup() //output selection
								.addComponent(outputLabel)
								.addComponent(outputPathTF, 350, 350, 350)
								.addComponent(browseOutputButton)))
				
				.addGroup(layout.createSequentialGroup() // bottom half
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // checkboxes
								.addComponent(useRenameFileCheckbox)
								.addComponent(useOsuMetadataCheckbox)
								.addGroup(layout.createSequentialGroup()
										.addGap(20)
										.addComponent(useUnicodeCheckbox))
								.addGroup(layout.createSequentialGroup()
										.addGap(20)
										.addComponent(useAlbumArtCheckbox))
								.addComponent(useRemoveDuplicatesCheckbox)
								.addComponent(checkExistingCheckbox))
						.addGap(20)
						.addComponent(startButton, 60, 80, 80))
				.addComponent(progressBar)
		);
		
		
		layout.setVerticalGroup(
			layout.createSequentialGroup() // overall
				.addGap(155)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE) // input selection
						.addComponent(inputLabel)
						.addComponent(inputPathTF)
						.addComponent(browseInputButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE) // output selection
						.addComponent(outputLabel)
						.addComponent(outputPathTF)
						.addComponent(browseOutputButton))
				
				.addGap(20)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER) // bottom half
						.addGroup(layout.createSequentialGroup() // checkboxes
								.addComponent(useRenameFileCheckbox)
								.addComponent(useOsuMetadataCheckbox)
								.addComponent(useUnicodeCheckbox)
								.addComponent(useAlbumArtCheckbox)
								.addComponent(useRemoveDuplicatesCheckbox)
								.addComponent(checkExistingCheckbox))
						.addComponent(startButton, 30, 60, 60))
				.addGap(20)
				.addComponent(progressBar)
		);

		
		// BG Image
		try {
			background = ImageIO.read(getClass().getResource("/images/background.png"));
			
		} catch (IOException e) {
			System.out.println("Can't load background image...");
			e.printStackTrace();
		}
		
		
		// Icon Image
		applyIconImage();

	}



	public void start() throws IOException {
		
		// Disable start Button
		setAllButtonsEnabled(false);
		
		// Test valid in/output folders
		File testInput = new File (inputFolderPath);
		File testOutput = new File (outputFolderPath);
		
		if (!testInput.exists() || !testInput.isDirectory() || !testOutput.exists() || !testOutput.isDirectory()) {
			JOptionPane.showMessageDialog(this,
				    "Please ensure source and destination folders exist.",
				    "Folder path invalid",
				    JOptionPane.WARNING_MESSAGE);
			return;
		}
		// delet this
		testInput = null;
		testOutput = null;
		
		
		// Create log file
		File log = new File (outputFolderPath+"\\!log.txt");
		try {
			log.createNewFile();
			//fw = new FileWriter(log);
			outputWriter = new OutputStreamWriter (new FileOutputStream(log), "UTF8");
			bw = new BufferedWriter(outputWriter);
		} catch (IOException e) {
			System.out.println("! log.txt could not be created");
			e.printStackTrace();
		}
		
		
		// Start Progress bar
		progressBar.setEnabled(true);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Searching input folder for songs...");
		
		System.out.println("searching folder: " + inputFolderPath);
		logLine("Searching folder: " + inputFolderPath);
		
		// SEARCH INPUT FOLDER
		SongList = searchFolder(new File(inputFolderPath)); // Execute search method
		
		// PRINT SONG LIST TO LOG
		printSongList(SongList);
		
		if (checkExistingCheckbox.isSelected()) {
			// SEARCH OUTPUT FOLDER
			progressBar.setString("Searching outputfolder for existing songs...");
			ExistingSongList = searchDestinationFolder(new File(outputFolderPath));
		}
		
		// REMOVE DUPLICATE AND EXISTING SONGS
		progressBar.setIndeterminate(false);
		progressBar.setMaximum(SongList.size());
		progressBar.setString("Removing Duplicate and/or Existing Songs");
		removeDuplicates(SongList);
		
		// SET PROGRESS BAR
		progressBar.setIndeterminate(false);
		progressBar.setMaximum(SongList.size());
		
		// Apply tags to song in song list and save
		applyTagsAndSave(SongList, new File(outputFolderPath));
		
		
		System.out.println("done");
		logLine("done");
		
		// close log file
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Re-enable start button and reset progress bar
		setAllButtonsEnabled(true);
		progressBar.setValue(progressBar.getMinimum());
		progressBar.setEnabled(false);
		progressBar.setString("Done!");

	}
	
	
	private ArrayList<Song> searchDestinationFolder(File destinationFolder) {
		ArrayList <Song> songList = new ArrayList<Song>();
		
		File destFolderDir = destinationFolder;
		
		for (final File songFile : destFolderDir.listFiles()) {
			System.out.println("Checking destination folder file: " + songFile.getName());
			logLine("Checking destination folder file: " + songFile.getName());
			if (getExtension(songFile).toLowerCase().equals("mp3")) {
				Song song = new Song();
				song.setSongFolderName(songFile.getName());
				songList.add(song);
			}
		}
		
		return songList;
		
	}
	
	private void removeDuplicates(ArrayList<Song> songList) {
		
		ArrayList<Song> duplicateSongList = new ArrayList<Song>();
		
		int progress = 0;
		if (useRemoveDuplicatesCheckbox.isSelected()) {
			// FOR EVERY SONG IN LIST
			for (Song targetSong : songList) {
				progress ++;
				progressBar.setValue(progress);

				String targetID = targetSong.getData()[0] + targetSong.getData()[2];
				String targetIDUnicode = targetSong.getData()[1] + targetSong.getData()[3];

				ArrayList<Song> currentDuplicateSongList = new ArrayList<Song>();
				currentDuplicateSongList.add(targetSong);

				// SEARCH THROUGH SONG LIST
				for (Song currentSong : songList) {
					if (targetSong.equals(currentSong)) {
						continue;
					}

					String currentID = currentSong.getData()[0] + targetSong.getData()[2];
					String currentIDUnicode = currentSong.getData()[1] + targetSong.getData()[3];

					// CHECK IF SONG TITLE + ARTIST MATCHES
					if (currentID.equals(targetID) || currentIDUnicode.equals(targetIDUnicode)) {

						System.out.println("Found Duplicate: " + currentID);
						currentDuplicateSongList.add(currentSong);
					}

				}

				// GO TO NEXT SONG IF NO DUPLICATES FOUND
				if (currentDuplicateSongList.size() > 1) {

					// GET DURATION OF DUPLICATE SONGS
					for (int i = 0; i < currentDuplicateSongList.size(); i++) {
						Song duplicateSong = currentDuplicateSongList.get(i);
						File songFile = new File (duplicateSong.getSongFolderName() + "\\" + duplicateSong.getAudioFilename());
						try {
							Mp3File songMp3 = new Mp3File(songFile);
							duplicateSong.setDuration((int)songMp3.getLengthInSeconds());
						} catch (UnsupportedTagException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvalidDataException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}


					}

					// FIND LONGEST SONG
					int longestIndex = 0;
					for (int i = 0; i < currentDuplicateSongList.size(); i++) {
						if (currentDuplicateSongList.get(i).getDuration() > currentDuplicateSongList.get(longestIndex).getDuration()) {
							longestIndex = i;
						}
					}

					// REMOVE LONGEST SONG FROM LIST OF DUPLICATE SONGS
					currentDuplicateSongList.remove(longestIndex);

					// ADD TO MASTER LIST OF DUPLICATE SONGS
					for (Song song : currentDuplicateSongList) {
						duplicateSongList.add(song);
					}

				}




			}
		}
		
		// REMOVE ALL SONGS IN DUPLICATE SONG LIST
		for (int i=0; i < songList.size(); i++) {
			Song song  = songList.get(i);
			
			// REMOVE DUPLICATES
			if (useRemoveDuplicatesCheckbox.isSelected()) {
				for (Song dupSong : duplicateSongList) {
					if (song.getSongFolderName().equals(dupSong.getSongFolderName())) {
						songList.remove(i);
						duplicateSongList.remove(dupSong);
					}
				}
			}
			
			
			// REMOVE EXISTING
			if (checkExistingCheckbox.isSelected()) {
				for (Song existSong : ExistingSongList) {
					String existingSongFileName = existSong.getSongFolderName().toLowerCase();
					existingSongFileName = existingSongFileName.replace(".mp3", "");
					
					System.out.println(existingSongFileName);
					
					if (new File(song.getSongFolderName()).getName().toLowerCase().equals(existingSongFileName)) {
						songList.remove(i);
						//ExistingSongList.remove(existSong);
					}
				}
			}
		}
		
		
	}
	
	/**
	 * Returns 0 if normal
	 * Returns 1 if no song folders found
	 * @return int state
	 * @throws IOException 
	 */
	public ArrayList<Song> searchFolder(File inputFolder) throws IOException {
		
		ArrayList<Song> SongArrayList = new ArrayList<Song>();
		
		File inputFolderDir = inputFolder;
		boolean hasSongFolders = false;
		
		for (final File songFolderDir : inputFolderDir.listFiles()) { // for every folder in the input folder

			System.out.println("Searching song folder: " + songFolderDir.getPath());
			logLine("Searching song folder: " + songFolderDir.getPath());
			
			if(!songFolderDir.isDirectory()) { // check that song folder is a folder
				continue;
			}
			
			// If it gets here, then the folder is valid
			
			hasSongFolders = true;
			
			boolean hasOsuFile = false;
			
			
			Song songTaiko = null;
			Song songStd = null;
			
			for (final File osuFile : songFolderDir.listFiles()) { // For every file in the song folder
				
				
				if (!osuFile.isFile() || !getExtension(osuFile).equals("osu")) { // check that file is an .osu file
					continue;
				}
				
				
				// If it gets here, than the file is a valid .osu file
				hasOsuFile = true;
				System.out.println("Found osu file:" + osuFile.getName());
				logLine("Found osu file:" + osuFile.getName());
				
				
				Song song = new Song();
				readOsuMetadata(song, osuFile);
				
				// Sort as either taiko or std song
				if (song.isManiaTaiko()) {
					songTaiko = song;
				} else {
					songStd = song;
				}

				song = null; //dispose
				
			}
			
			// Choose std if it exists, otherwise choose taiko
			if (songStd != null) {
				SongArrayList.add(songStd);
			} else if (songTaiko != null) {
				SongArrayList.add(songTaiko);
			} else {
				
			}
			
			if (hasOsuFile) {
				progressBar.setString("Searching input folder for songs... found: " + SongArrayList.size());
			} else {
				System.out.println("No osu file found in: " + songFolderDir.getAbsolutePath());
				logLine("No osu file found in: " + songFolderDir.getAbsolutePath());
			}


		}
		
		
		if (!hasSongFolders){
			return null;
		}
		if (SongArrayList.isEmpty()) {
			return null;
		}
		
		
		
		return SongArrayList;
	}
	
	
	/**
	 * Reads the metadata of osuFile and outputs it to Song
	 * @param osuFile
	 * @throws IOException 
	 */
	public void readOsuMetadata(Song song, File osuFile) {
		
		//Song song = new Song();
		
		song.setSongFolderName(osuFile.getParent());
		
		FileInputStream stream = null;
		final FileChannel channel;
		final MappedByteBuffer buffer;
		final int fileSize;
		byte [] byteArray = null;
		
		try {
			stream = new FileInputStream(osuFile);
			channel  = stream.getChannel();
			buffer   = channel.map(MapMode.READ_ONLY, 0, osuFile.length());
			fileSize = (int)osuFile.length();
			byteArray = new byte[(int)channel.size()];
			buffer.get(byteArray);
			
 		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		// Begin reading file
		BufferedReader reader = null;
		try {
			
			// Initialize reader that supports UTF-8
			/*reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(osuFile), "UTF8")); // Read .osu file in UTF-8
			*/
			if (byteArray != null) {
				reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArray), "UTF8"));
			} else {
				System.out.println("Failed to read with FileChannel!");
				logLine("Failed to read with FileChanel!");
				reader = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(osuFile), "UTF8")); // Read .osu file in UTF-8
			}
			
			// Loop through each line of the osu file
			String line;
			boolean foundBG = false;
			boolean foundGameMode = false;
			while ((line = reader.readLine()) != null) {
			    
				
				// Find game mode
				if (!foundGameMode && line.startsWith("Mode:")) {
					line = line.replaceFirst("Mode:", "");
					line = line.trim();
					if (line.equals("1") || line.equals("3")) {
						song.setManiaTaiko(true);
						foundGameMode = true;
					}
				}
				
				// Find metadata labels
				for (int index = 0; index < song.getLabel().length; index++) { 
					String currentLabel = song.getLabel()[index] + ":";
					
					if (line.startsWith(currentLabel)){
						line = line.replaceFirst(currentLabel, "");
						String [] newData = song.getData();
						newData[index] = line;
						song.setData(newData);
					}

				}
				
				// Find Audiofile name
				if (line.startsWith("AudioFilename:")) { 
					line = line.replaceFirst("AudioFilename:", "");
					line = line.trim();
					song.setAudioFilename(line);
				}
				
				// Find BG image name
				if (!foundBG && line.contains("\"")) { 
					boolean isBG = false;
					line = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
					//System.out.println(line);
					for (String imageExtension : imageFormats) {
						String extension = getExtension(line).toLowerCase();
						if (extension.equals(imageExtension.toLowerCase())) {
							isBG = true;
							break;
						}
					}
					if (isBG) {
						song.setBgFilename(line);
						foundBG = true;
					}
				}
			}
			
			// Fill In missing unicode data from older osu files
			String [] newData = song.getData();
			if (newData[1] == null || newData[1].equals("")) {
				newData[1] = newData[0];
			}
			if (newData[3] == null || newData[3].equals("")) {
				newData[3] = newData[2];
			}
			song.setData(newData);
			
			
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			System.out.println("cannot open .osu file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("cannot read .osu file line");
			e.printStackTrace();
		}
		
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println("failed to close file??");
					e.printStackTrace();
				}
			}
		}
		
		//return song;
	}
	
	
	private void applyTagsAndSave(ArrayList<Song> SongArrayList, File destFolder){
		
		if (SongArrayList == null || SongArrayList.isEmpty()) {
			logLine("Song array list is empty or null!");
			return;
		}
		
		int counter = 0;
		
		// Loop through Song List
		for (Song song : SongArrayList) {
			
			progressBar.setString("Processing: " + song.getSongFolderName() + " (" + (counter+1) + "/" + progressBar.getMaximum() + ")");
			
			// Create file and mp3 file
			File songFile = new File (song.getSongFolderName() + "\\" + song.getAudioFilename());
			
			// Prepare filename according to setting
			String audioFileName;
			if (useRenameFileCheckbox.isSelected()) {
				audioFileName = new File(song.getSongFolderName()).getName() + "." + getExtension(song.getAudioFilename().toLowerCase());
			} else {
				audioFileName = songFile.getName();
			}
			
			System.out.println("Processing: " + songFile.getAbsolutePath());
			logLine("Processing: " + songFile.getAbsolutePath());
			logLine();
			
			
			// CHECK IF FILE EXISTS
			if (songFile == null || !songFile.exists()) {
				logLine("MP3 File specified in osu file (\"" + song.getAudioFilename() + "\") does not exist!");
			}
			

			
			if (useOsuMetadataCheckbox.isSelected() && song.getAudioFilename().toLowerCase().endsWith(".mp3")) {
				
				// Create mp3 file with modifiable tags;
				Mp3File mp3file = null;
				

				
				try {
					mp3file = new Mp3File(songFile);
				} catch (UnsupportedTagException e) {
					logLine("Opening mp3: UnsupportedTagException" + songFile);
					e.printStackTrace();
				} catch (InvalidDataException e) {
					logLine("Opening mp3: InvalidDataException" + songFile);
					e.printStackTrace();
				} catch (IOException e) {
					logLine("Opening mp3: UnsupportedTagException" + songFile);
					e.printStackTrace();
				}
				if (mp3file == null) {
					
					// If MP3 FAILS TO OPEN, DIRECT COPY FILE
					logLine("Failed to read mp3 file, directly copying file.");
					copyFile(songFile,new File(destFolder.getAbsolutePath() + "//" + audioFileName));
					
					continue;
				}

				applyTags(mp3file, song);
				

				// SAVE MP3

				try {
					mp3file.save(destFolder.getAbsolutePath() + "\\" + audioFileName);
				} catch (NotSupportedException e) {
					logLine("Saving: NotSupportedException");
					e.printStackTrace();
				} catch (IOException e) {
					logLine("Saving: IOException");
					e.printStackTrace();
				}


			} else { // DIRECT COPY NON-MP3 AUDIO FILE
				
				logLine("Audio file is not mp3 file, directly copying file.");
				copyFile(songFile,new File(destFolder.getAbsolutePath() + "//" + audioFileName));
				
			}
			
			//SET PROGRESS BAR
			counter++;
			progressBar.setValue(counter);

		}
	}
	
	
	private void applyTags (Mp3File mp3file, Song song) {
		
		// REMOVE EXISTING TAGS
		ID3v2 id3v2Tag = new ID3v24Tag();
		/*if (mp3file.hasId3v2Tag()) {
			id3v2Tag = mp3file.getId3v2Tag();
		} else {
			id3v2Tag = new ID3v24Tag();
			mp3file.removeCustomTag();
			mp3file.removeId3v1Tag();
		}*/

		mp3file.removeCustomTag();
		mp3file.removeId3v1Tag();
		mp3file.removeId3v2Tag();


		// SET TAGS
		if (useUnicodeCheckbox.isSelected()) {
			id3v2Tag.setTitle(song.getData()[1]);
			id3v2Tag.setArtist(song.getData()[3]);
		} else {
			id3v2Tag.setTitle(song.getData()[0]);
			id3v2Tag.setArtist(song.getData()[2]);
		}
		id3v2Tag.setComposer(song.getData()[4]);
		id3v2Tag.setPublisher(song.getData()[5]);
		id3v2Tag.setComment(song.getData()[6]);
		//id3v2Tag.setAlbumArtist(song.getData()[5]);
		
		if (useAlbumArtCheckbox.isSelected() && song.getBgFilename() != null && !song.getBgFilename().equals("")) {


			try {
				applyAlbumArt(id3v2Tag, song);
			} catch (Exception e) {
				logLine("Failed to set album art.");
				System.out.println("Failed to set album art.");
				e.printStackTrace();
			}
		}
		
		
		mp3file.setId3v2Tag(id3v2Tag);
		
		id3v2Tag = null;
		
		return;
	}
	
	private void applyAlbumArt(ID3v2 id3v2Tag, Song song) throws Exception {
		
		File bgFile = new File(song.getSongFolderName() + "\\" + song.getBgFilename());
		
		if (!bgFile.exists() || !bgFile.isFile()) {
			logLine("BG image file specified in osu file not found.");
			System.out.println("BG image file specified in osu file not found.");
			return;
		}
		
		// LOAD IMAGE FILE
		BufferedImage bg = ImageIO.read(bgFile);
		
		// PREPARE IMAGE FILE
		bg = prepareImage(bg);
		
		// GET IMAGE BYTE STREAM
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bg, "png", baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		
		// APPLY IMAGE TO TAG
		id3v2Tag.setAlbumImage(imageInByte, "image/jpg");
	}
	
	
	
	private BufferedImage prepareImage(BufferedImage input) {
		
		// SET VARS
		int targetSize = albumArtDimension;
		int width = input.getWidth();
		int height = input.getHeight();
		
		// CALCULATE CROP DIMENSIONS
		int size = Math.min(width, height);
		int Xmin = (width - size)/ 2;
		int Ymin = (height - size) / 2;
		
		// CROP
		input = input.getSubimage(Xmin, Ymin, size, size);
		
		// RESIZE
		ResampleOp resizeOp = new ResampleOp(targetSize, targetSize);
		resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
		BufferedImage scaledImage = resizeOp.filter(input, null);
		
		return scaledImage;
	}
	
	private void printSongList(ArrayList<Song> songList) {
		
		logLine("[Song List]");
		
		for (Song song : songList) {
			System.out.println(song.getSongFolderName());
			System.out.println(song.getAudioFilename());
			System.out.println(song.getBgFilename());
			
			logLine(song.getSongFolderName());
			logLine(song.getAudioFilename());
			logLine(song.getBgFilename());
			for (String data : song.getData()) {
				System.out.println(data);
				logLine(data);
			}
			
			logLine("   ---   ---   ---");
		}
		
		logLine("[End Song List]");
	}
	
	private void setAllButtonsEnabled(boolean isEnabled) {
		startButton.setEnabled(isEnabled);
		useRenameFileCheckbox.setEnabled(isEnabled);
		useOsuMetadataCheckbox.setEnabled(isEnabled);
		useRemoveDuplicatesCheckbox.setEnabled(isEnabled);
		
		if (useMetadataWasEnabled) {
			useUnicodeCheckbox.setEnabled(isEnabled);
			useAlbumArtCheckbox.setEnabled(isEnabled);
		} else {
			useUnicodeCheckbox.setEnabled(false);
			useAlbumArtCheckbox.setEnabled(false);
		}
	}
	
	private void applyIconImage() {
		iconList = new ArrayList<BufferedImage>();
		
		
		try {
			iconList.add(ImageIO.read(getClass().getResource("/images/icon128.png")));
			iconList.add(ImageIO.read(getClass().getResource("/images/icon64.png")));
			iconList.add(ImageIO.read(getClass().getResource("/images/icon32.png")));
			iconList.add(ImageIO.read(getClass().getResource("/images/icon16.png")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		frame.setIconImages(iconList);
	}
	
	/**
	 * Print to bw log
	 * @param s
	 */
	private void logLine(String s) {
		if (s != null) {
			try {
				bw.write(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				bw.write("NULL");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try { // new line
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	private void copyFile(File source, File dest) {
		try {
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logLine("Failed to Direct Copy audio file");
			e.printStackTrace();
		}
	}

	
	private void logLine() {
		logLine("");
	}

	/**
	 * Returns the extension of the given File
	 * @param file
	 * @return String extension of given file
	 */
	private String getExtension(File file) {
		return getExtension(file.getName());
	}
	
	/**
	 * Returns the extension of the given filename/path without the dot
	 * @param fileName
	 * @return String extension of given file
	 */
	private String getExtension(String fileName) {
		int index = fileName.lastIndexOf(".");
		if (index > 0) {
			return fileName.substring(index+1);
		}
		return "";
	}

	private static void createAndShowGUI() {
		frame = new JFrame("osu!ongaku");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MainClass());
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseInputButton) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File chosenFile = fc.getSelectedFile();
				inputFolderPath = chosenFile.getAbsolutePath();
				inputPathTF.setText(inputFolderPath);
			} else {
				// nothing
			}
		}
		else if (e.getSource() == browseOutputButton) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File chosenFile = fc.getSelectedFile();
				outputFolderPath = chosenFile.getAbsolutePath();
				outputPathTF.setText(outputFolderPath);
			} else {
				// nothing
			}
		}

		else if (e.getSource() == startButton) {
			System.out.println("yeet");
			new Thread(new Runnable() {
			    @Override public void run() {
			        try {
						start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}).start();
		}
		else if (e.getSource() == useOsuMetadataCheckbox) {
			System.out.println("clicked");
			if (useOsuMetadataCheckbox.isSelected()) {
				useAlbumArtCheckbox.setEnabled(true);
				useUnicodeCheckbox.setEnabled(true);
				useMetadataWasEnabled = true;
			} else {
				useAlbumArtCheckbox.setEnabled(false);
				useUnicodeCheckbox.setEnabled(false);
				useMetadataWasEnabled = false;
			}
		}

	}

}