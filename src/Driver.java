import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;


/**
 * Driver to build a word index from HTML/HTM files
 */
public class Driver {

	/**
	 * Tries to create a new file for the given path.
	 *
	 * @param indexPath
	 * 			Path to create file at
	 *            
	 * @see File#createNewFile()
	 * 
	 * @return new File that represents the indexFile
	 */
	public static File createIndexFile(Path indexPath) {
		
		File indexFile = new File(indexPath.toString());
			
		try {
			indexFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return indexFile;
	}
	
	/**
	 * Reads a file line by line.
	 * 
	 * @param file
	 * 			File to read
	 * 
	 * @throws IOException
	 *            
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see BufferedReader#readLine()
	 * 
	 * @return a String representation of the file
	 */
	public static String readFile(File file) throws IOException {
		try(
				BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
		){
			
			String str = null;
			String txt = "";
			
			while((str = reader.readLine()) != null) {
				
				// Put a space b/w two words originally separated by a new-line
				txt += (" " + str);
				
			}
			
			return txt;
		}
	}
	
	
	public static void readFile(Path queryPath) throws IOException {
		try(
				BufferedReader reader = Files.newBufferedReader(queryPath, StandardCharsets.UTF_8);
				){
			
			String str = null;
			String txt = "";
			
			while((str = reader.readLine()) != null) {
				
				// Put a space b/w two words originally separated by a new-line
				txt += (" " + str);
				
			}
			
			//return txt;
			
			System.out.println("txt: " + txt);
			
		}
	}

	/**
	 * Populates wordIndex.
	 * 
	 * @param wordIndex
	 * 			WordIndex to populate
	 * @param file
	 * 			File used to populate wordIndex
	 * 
	 * @see Driver#readFile(File)       
	 * @see HTMLCleaner#stripHTML(String)
	 * @see WordIndex#addAll(String[], String)
	 */
	public static void buildIndex(WordIndex wordIndex, File file) {
		
		String txt;
		
		try {
			
			txt = readFile(file);
			
			txt = HTMLCleaner.stripHTML(txt);
			
			// Avoid empty files
			if(!txt.equals("")) { 
				
				wordIndex.addAll(txt.split(" "), file.toPath().toString());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Indicates whether a file is an HTML or HTM file.
	 *
	 * @param f
	 * 			File to check
	 *            
	 * @see String#lastIndexOf(int)
	 * @see String#substring(int)
	 */
	public static boolean isHTMLorHTM(File f) {
	
		String name = f.getName();
		
		String ext = name.substring(name.lastIndexOf(".") + 1);
		
		ext = ext.toLowerCase();
		
		//System.out.println("ext: " + ext);
		
		return (ext.equals("html") || ext.equals("htm"));
	}
	
	
	/**
	 * Recursively traverses through current directory and any sub-directories 
	 * passing valid files to buildIndex function.
	 *
	 * @param wordIndex
	 *            WordIndex to populate.
	 * @param f
	 * 			File to traverse if a directory or pass to buildIndex if a 
	 * 			valid file.
	 * 
	 * @see File#isFile()
	 * @see File#isDirectory()
	 */
	public static void recTraverse (WordIndex wordIndex, File f) {
		
		if(f.isFile() && isHTMLorHTM(f)) {
			
			buildIndex(wordIndex, f);
				
		} else if(f.isDirectory()) {
			
			for(File recF : f.listFiles()) {
				recTraverse(wordIndex, recF);
			}
			
		} else {
			return;
		}
	}
	
	//FIX: Don't create files if passing to readFile
	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		
		WordIndex wordIndex = new WordIndex();
		
		QueryHelper queryHelper = new QueryHelper();
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) { 
			
			Path p = Paths.get(argMap.getString("-path"));
			
			File file = new File(p.normalize().toString());
			
			recTraverse(wordIndex, file);
				
		} else {
			System.out.println("No path given.");
		}

		
		if(argMap.hasFlag("-index")) {
		
			String defaultPath = Paths.get(".", "index.json").toString();
			Path indexPath = Paths.get(argMap.getString("-index", defaultPath));
			indexPath = indexPath.toAbsolutePath().normalize();
				
			try {
				JSONWriter.asWordIndex(wordIndex, indexPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println("No index path given.");
		}
		
		if(argMap.hasFlag("-query") && argMap.hasValue("-query")) {
			
			Path queryPath = Paths.get(argMap.getString("-query"));
			
			/** Because search is being called inside the queryHelper */
			if(argMap.hasFlag("-exact")) {
				queryHelper.exactSearchOn();
			}
			
			try {
				queryHelper.parseAndSearchFile(queryPath, wordIndex);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		} else {
			System.out.println("No query path given.");
		}
		
		
		if(argMap.hasFlag("-results")) {
			
			String resultsDefaultPathStr = Paths.get(".", "results.json").toString();
			Path resultsPath = Paths.get(argMap.getString("-results", resultsDefaultPathStr));
			resultsPath = resultsPath.toAbsolutePath().normalize();
			
			try {
				//TODO: Write query results to file
				
				queryHelper.writeToFile(resultsPath);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	} // main

}
