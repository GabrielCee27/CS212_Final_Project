import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses and builds a WordIndex 
 */
public class IndexHelper {
	
	private final WorkQueue queue;
	
	/** Shared index */
	private ThreadSafeWordIndex idx;
	
	/**
	 * Initializes with index to populate and the queue to use.
	 * 
	 * @param idx
	 * 			WordIndex to populate
	 * @param queue
	 * 			WorkQueue to use
	 */
	public IndexHelper(ThreadSafeWordIndex idx, WorkQueue queue) {
		this.idx = idx;
		this.queue = queue;
	}
	
	/**
	 * Reads a file line by line.
	 * 
	 * @param filePath
	 * 			Path of file to read
	 * @throws IOException    
	 *      
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see BufferedReader#readLine()
	 * 
	 * @return a String representation of the file
	 */
	public static String readFile(Path filePath) throws IOException {
		try(
				BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
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
	
	
	/**
	 * Populates wordIndex by parsing the file given.
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
	public void buildIndex(WordIndex wordIndex, File file) {
		
		String txt;
		
		try {
			
			txt = readFile(file.toPath());
			
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
	public boolean isHTMLorHTM(File f) {
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
	public void recTraverse (WordIndex wordIndex, File f) {
		
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
	
	/**
	 * If initial file is a directory, will create tasks for the queue.
	 * If not a directory, will parse and build index without the queue.
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
	public void dirTraverse(File file) {

		if(file.isFile() && isHTMLorHTM(file)) 
			queue.execute(new ParseFileTask(file));	
		else if(file.isDirectory())
			for(File f : file.listFiles())
				dirTraverse(f);	
		
		return;
	}
	
	
	/**
	 * A class that details what task is being placed in the work queue.
	 * Populates a temporary WordIndex and updates shared WordIndex when done.
	 * 
	 * @see Runnable
	 * @see WorkQueue#execute(Runnable)
	 *
	 */
	private class ParseFileTask implements Runnable{
		
		/** File to parse and update index with */
		private File file;
		
		/** Temporary index that will merge with shared index when done being populated */
		private WordIndex tempIdx;
		
		/**
		 * Initializes ParseFileTask with file to parse.
		 * 
		 * @param file
		 * 			file to parse and update shared index with
		 */
		public ParseFileTask(File file) {
			this.file = file;
			this.tempIdx = new WordIndex();
		}
		
		@Override
		public void run() {
			//parse file and populate tempIndex
			buildIndex(tempIdx, file);
			
			/** Update shared index */
			idx.mergeWith(tempIdx);
		}
		
	}
	
}
