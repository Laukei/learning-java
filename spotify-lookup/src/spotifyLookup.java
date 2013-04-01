import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;



class Song {
	URL spotifyUrl;
	URL groovesharkUrl;
	String songTitle;
	boolean localFile = false;
	String[] artists;
	String album;
	int trackNumber = 0;
	float length = 0;
	
	public void lookup(String rawInputUrl) {
		String apiBaseUrl = "http://ws.spotify.com/lookup/1/?uri=spotify:track:";
		String[] inputUrlSplit;
		String inputUrl;
		inputUrlSplit = rawInputUrl.split("/");
		if (inputUrlSplit[3].contains("track")) {
			inputUrl = (apiBaseUrl + inputUrlSplit[inputUrlSplit.length-1]);	
			boolean incomplete = true;
			int maxTries = 3;
			int tries = 0;
			while ((incomplete) || (tries < maxTries)) {
				try {
					tries++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					spotifyUrl = new URL(rawInputUrl);
					URI uri = new URI(inputUrl);
		
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(uri.toString());
					doc.getDocumentElement().normalize();
								
					NodeList nList = doc.getElementsByTagName("artist");
					artists = new String[nList.getLength()];
					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						artists[temp] = nNode.getTextContent().trim();				
					}
					songTitle = doc.getElementsByTagName("name").item(0).getTextContent().trim();
					album = doc.getElementsByTagName("album").item(0).getChildNodes().item(1).getTextContent();
					trackNumber = Integer.parseInt(doc.getElementsByTagName("track-number").item(0).getTextContent());
					length = Float.parseFloat(doc.getElementsByTagName("length").item(0).getTextContent());
					incomplete = false;
					
		
				} catch (MalformedURLException mue) {
					mue.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (URISyntaxException use) {
					//stuff
				} catch (Exception e) {
					//stuff... fml
				}
			}
		} else if (inputUrlSplit[3].contains("local")) {
			try {
				URI uri = new URI(rawInputUrl);
				spotifyUrl = new URL(uri.toString());
			} catch (Exception e) {
				//
			}
			localFile = true;
			artists = new String[1];
			artists[0] = inputUrlSplit[4];
			album = inputUrlSplit[5];
			songTitle = inputUrlSplit[6];
			//System.out.println("Oh my God JC, A BOOOOOOO- local file.");
		}
		
	}
	public void groovesharkFeelingLuckyLookup() {
		String baseUrl = "http://tinysong.com/a/"; //uses /a/ method - compares no fields, just takes it as gospel. Potentially shite results...
		String keyUrl = "?format=json&key=";
		String queryUrl = baseUrl;
		for (String artist : artists) {
			queryUrl = queryUrl + artist + "+";
		}
		queryUrl = queryUrl + songTitle;
		queryUrl = queryUrl + keyUrl;
		List<String> queryUrls = new ArrayList<String>();
		queryUrls.add(queryUrl);
		try {
			queryUrls.add(baseUrl + artists[0] +"+"+ songTitle + keyUrl);
		} catch (ArrayIndexOutOfBoundsException e) {
			//
		}
		queryUrls.add(baseUrl + album + "+" + songTitle + keyUrl);
		queryUrls.add(baseUrl + songTitle + keyUrl);
		
		
		
		for (String url : queryUrls) {
			//System.out.println(url);
			String line = groovesharkQuery(url);
			if (line.contains("[]")) {
				//System.out.println("Grooveshark lookup failed in this instance");
			} else {
				try {
					groovesharkUrl = new URL(line.split("\"")[1].trim());
					//System.out.println(line);
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			try {
				groovesharkUrl = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String groovesharkQuery(String queryUrl) {
		InputStream is = null;
		BufferedReader br;
		String line = null;
		try {
			System.out.println("URL: "+queryUrl);
			//URI uri = new URI(queryUrl.split("://")[0],queryUrl.split("://")[1].split("/")[0],"/"+queryUrl.split(queryUrl.split("://")[1].split("/")[0])[1],null);
			//URL url = uri.toASCIIString();
			URL url = new URL(queryUrl);
			is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			line = br.readLine();

		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			//stuff... fml
		}
		return line;
	}
	
	public void properties() {
		try{
			for (String artist : artists) {
				System.out.println("Artist: "+artist);
			}
		} catch (NullPointerException npe) {
			System.out.println("Artist: undefined");
		}
		System.out.println("Title : "+songTitle);
		System.out.println("Album : "+album);
		System.out.println("Track#: "+trackNumber);
		System.out.println("length: "+length);	
		System.out.println("Spotfy: "+spotifyUrl.toString());
		try {
			System.out.println("GrvShk: "+groovesharkUrl.toString());
		} catch (NullPointerException npe) {
			System.out.println("GrvShk: undefined");
		}
	}
	
	public void writeCsv(String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName,true);
			writer.append("\""+songTitle+"\"");
			writer.append(',');
			writer.append("\""+album+"\"");
			writer.append(',');
			writer.append(String.valueOf(trackNumber));
			writer.append(',');
			writer.append(String.valueOf(length));
			writer.append(',');
			writer.append(String.valueOf(localFile));
			writer.append(',');
			writer.append("\""+String.valueOf(spotifyUrl)+"\"");
			writer.append(',');
			writer.append("\""+String.valueOf(groovesharkUrl)+"\"");
			writer.append(',');
			for (int i = 0; i<artists.length; i++) {
				if (i<(artists.length-1)) {
					writer.append("\""+artists[i]+"\"");
					writer.append(',');
				} else {
					writer.append("\""+artists[i]+"\"");
					writer.append('\n');
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public class spotifyLookup {
	public static void main(String[] args) {
		String outputFileName = "src/spotify-playlist.csv";
		
		String[] inputUrls = {}; 
		
		try (BufferedReader br = new BufferedReader(new FileReader("src/playlist.txt"))) {
			List<String> lines = new ArrayList<String>();
			String line = null;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
			System.out.println("Read "+lines.size()+" lines.");
			inputUrls = lines.toArray(new String[lines.size()]);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		String[] inputUrls = {
				"http://open.spotify.com/track/37IuqAoH08yjX1zcbxs1ip",
				"http://open.spotify.com/track/7LAJWSKK8JMIZAcblgUMS6",
				"http://open.spotify.com/track/78LWZ7WT4s7QDa9zzTLH5Y",
				"http://open.spotify.com/local/Howard+Shore%2c+Emiliana+Torrini/The+Two+Towers%3a+The+Complete+Recordings/%27%27Long+Ways+To+Go+Yet%27%27/485"
		};*/
		
		Song[] songList = new Song[inputUrls.length];
		//iterate through the list, creating Songs
		for (int i = 0; i<inputUrls.length; i++) {
			songList[i] = new Song();
			songList[i].lookup(inputUrls[i]);
			//songList[i].groovesharkFeelingLuckyLookup();
			songList[i].properties();
			songList[i].writeCsv(outputFileName);
		}
	}
}
