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

import org.json.JSONArray;
// External lib
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

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
				//System.out.println("Created a new index file.");
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
				
				//System.out.println(str);
				txt += str;
				
			} // While
			
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
			
			wordIndex.addAll(cleanedTxt.split(" "), path, 0);
			
			//System.out.println(wordIndex.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static JSONObject convertIndexToJSON() {
		
		JSONObject json = new JSONObject();
			
		//System.out.println("copyWords: " + index.copyWords());	
		
		for( String word : wordIndex.copyWords() ) {
			
			//System.out.println("word: " + word);
			
			for(String p : wordIndex.copyPaths(word)) {
				//System.out.println("Path:" + p);
				
				JSONObject subObj = new JSONObject();
				
				List <Integer> list = wordIndex.copyPositions(word, p);
				
				try {
					
					subObj.put(p, list);
					
					json.put(word, subObj);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		//System.out.println("json: \n" + json.toString());
		
		return json;
	}
	
	//FIX: need to indent the inner objects
	//BUG: there should be no NULLs passed
	public static void writeJSONtoIndexFile(JSONObject obj, File file) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			try {
				
				writer.write(obj.toString(4));
				
				System.out.println("JSON Object Written:");
				System.out.println(obj.toString(4));
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			writer.close();	
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public static boolean isHTML(File f) {
	
		String name = f.getName();
		
		//int i = name.lastIndexOf(".");
	
		//System.out.println("extention: " + name.substring(name.lastIndexOf(".") + 1));
		
		String ext = name.substring(name.lastIndexOf(".") + 1);
		
		//System.out.println("ext: " + ext);
		
		if(ext.toLowerCase().equals(ext)) {
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
			
			System.out.println(f.toPath().toString());
			System.out.println(f.getName());
			
			buildIndex(f, f.toPath().toString());
				
		} else if(f.isDirectory()) {
			
			for(File recF : f.listFiles()) {
				recTraverse(recF);
			}
			
		} 
		
		return;
	}
	

	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		System.out.println("args: " + Arrays.toString(args));
		//System.out.println("numFlags(): " + argMap.numFlags());
		
		Path p = null;
		
		JSONObject obj = null;
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) {
			
			p = Paths.get(argMap.getString("-path"));
			
			File pFile = new File(p.normalize().toString());
			
			recTraverse(pFile);
				
		} else {
			System.out.println("No path given.");
		}
		
		//CONVERT WORDINDEX TO JSON
		
		obj = convertIndexToJSON();
		
		//CREATING THE INDEX FILE
		
		if(argMap.hasFlag("-index")) {
		
			Path indexPath = retrieveIndexPath(argMap); //FIX: Make argMap global?
			File indexFile = createIndexFile(indexPath);
			
			if(obj != null) {
				//Populate indexFile with wordIndex in JSON format
				writeJSONtoIndexFile(obj, indexFile);
			}
			
		} else {
			System.out.println("No index file created.");
		}

	} // main
	

}
