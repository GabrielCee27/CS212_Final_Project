import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Driver {
	
	private static WordIndex wordIndex = new WordIndex();
	
	
	public static Path retrievePath(ArgumentMap argMap) {	
		Path p = Paths.get(argMap.getString("-path"));
		p = p.toAbsolutePath().normalize();
		
		//FIX: Should check if path is valid?
		
		return p;
	}
	
	
	public static Path retrieveIndexPath(ArgumentMap argMap) {
		
		Path defaultPath = Paths.get(".", "index.json");
		Path p = Paths.get(argMap.getString("-index", defaultPath.toString()));
		
		p = p.toAbsolutePath().normalize();
		
		return p;
	}

	public static File createIndexFile(Path indexPath) {
		File indexFile = new File(indexPath.toString());
		
		if(indexFile.exists() && indexFile.delete()) {
			System.out.println("Deleted exsistng file.");
		} else {
			
			try {
				indexFile.createNewFile();
				System.out.println("Created a new index file.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} //else
		
		return indexFile;
	}
	
	
	public static String readFile(File file) throws IOException {
		try(
				BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
				){
			
			String str = null;
			String txt = "";
			
			while((str = reader.readLine()) != null) {
				
				txt += " ";
				
				txt += str;
			}
			
			// Clean up text
			txt = HTMLCleaner.stripHTML(txt);
			
			//System.out.println("Cleaned txt: \n" + txt);
			
			//String [] txtSplit = txt.split(" ");
			
//			for(int i=0; i < txtSplit.length; i++) {
//				System.out.println("i = " + i + ": " + txtSplit[i]);
//			}
			
			//System.out.println("txtSplit: " + txtSplit[]);
			
			return txt;
		}
	}
	
	
	public static void buildIndex(File file, String path) {
		
		String cleanedTxt;
		
		
		try {
			
			cleanedTxt = readFile(file);
			
			//System.out.println("cleaned text:" + cleanedTxt + ".");
			
			if(!cleanedTxt.equals("")) {
				wordIndex.addAll(cleanedTxt.split(" "), path, 0);
			}
			
			System.out.println("wordIndex: \n" + wordIndex.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String convertIndexToJSONstring() {
		
		String str = "{\n";
		
		List <String> words = wordIndex.copyWords();
		
		for(int i=0; i < words.size(); i++) {
			
			str += "\t";
			
			str += String.format("\"%s\"", words.toArray()[i]) + ": {\n";
			
			List <String> paths = wordIndex.copyPaths(words.toArray()[i].toString());
			
			for(int j=0; j < paths.size(); j++) {
				
				str += "\t\t";
				
				str += String.format("\"%s\"", paths.toArray()[j]) + ": [\n";
				
				
				List <Integer> positions = wordIndex.copyPositions(words.toArray()[i].toString(), paths.toArray()[j].toString());
				
				for(int k = 0; k < positions.size(); k++) {
					
					str += "\t\t\t";
					
					str += positions.toArray()[k].toString();
						
					if(k != positions.size()-1) {
						str += ",";
					}
						
					str += "\n";
				}
				
				str += "\t\t]";
				
				if(j != paths.size()-1) {
					str += ",";
				}
				
				str += "\n";
				
			}
			
			str += "\t}";
			
			if(i != words.size()-1) {
				str += ",";
			}
			
			str += "\n";
		}
		
		str += "}";
		
		//System.out.println("JSON String: \n" + str);
		return str;
	}
	
	
	public static void writeToIndexFile(String str, File file) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			writer.write(str);
			
			writer.close();
			
			System.out.println("Index File Created at " + file.toPath().toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
	}
	
	public static boolean isHTML(File f) {
	
		String name = f.getName();
		
		//int i = name.lastIndexOf(".");
	
		//System.out.println("extention: " + name.substring(name.lastIndexOf(".") + 1));
		
		String ext = name.substring(name.lastIndexOf(".") + 1);
		
		ext = ext.toLowerCase();
		
		//System.out.println("ext: " + ext);
		
		if(ext.equals("html") || ext.equals("htm")) {
			return true;
		}
		
		
		return false;
	}
	
	
	/* Recursively traverses through directory to find all files in any sub-directories.
	 * Should be populating wordIndex
	 * 
	 * 
	 * 
	 */
	public static void recTraverse (File f) {
		
		if(f.isFile()) {
			
			
			System.out.println("\nFile: " + f.toPath().toString());
//			System.out.println(f.getName());
			
			if(isHTML(f)) {
				System.out.println("Is an HTML file");
				buildIndex(f, f.toPath().toString());
			}
				
		} else if(f.isDirectory()) {
			
			for(File recF : f.listFiles()) {
				recTraverse(recF);
			}
			
		} else {
			return;
		}
		
	}
	

	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		System.out.println("args: " + Arrays.toString(args));
		//System.out.println("numFlags(): " + argMap.numFlags());
		
		Path p = null;
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) {
			
			p = Paths.get(argMap.getString("-path"));
			
			File pFile = new File(p.normalize().toString());
			
			// populate wordIndex
			recTraverse(pFile);
				
		} else {
			System.out.println("No path given.");
		}
		
		//Covert wordIndex TO JSON String
		
		String jsnStr = convertIndexToJSONstring();
		System.out.println("JSON String: \n" + jsnStr);

		//Creating the index file
		
		if(argMap.hasFlag("-index")) {
		
			Path indexPath = retrieveIndexPath(argMap); //FIX: Make argMap global?
			File indexFile = createIndexFile(indexPath);
			
			writeToIndexFile(jsnStr, indexFile);
			
		} else {
			System.out.println("No index file created.");
		}

	} // main

}
