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
				//System.out.println("Adding all...");
				wordIndex.addAll(txt.split(" "), file.toPath().toString());
				//System.out.println("Done adding all");
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
		
		if(file.isFile() && isHTMLorHTM(file)) {
			
			queue.execute(new DirectoryTask(file));
				
		} else if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				queue.execute(new DirectoryTask(f));
			}
			
		} else {
			return;
		}

	}
	
	
	/**
	 * A class that details what task is being placed in the work queue.
	 * Builds a temporary WordIndex to populate and populates this WordIndex after done.
	 * 
	 * @see Runnable
	 * @see WorkQueue#execute(Runnable)
	 *
	 */
	private class DirectoryTask implements Runnable{
		
		private File file;
		
		private WordIndex tempIdx;
		
		public DirectoryTask(File file) {
			this.file = file;
			this.tempIdx = new WordIndex();
		}
		
		@Override
		public void run() {
			
			if(file.isFile() && isHTMLorHTM(file)) {
				//parse file and update tempIndex
				//Safe to do unsynchronized
				buildIndex(tempIdx, file);
				
				//Alternative: update the index directly (slower)
				//buildIndex(file);
			}
			else if(file.isDirectory()) {	
				//add new tasks to queue
				for(File f : file.listFiles()) {
					queue.execute(new DirectoryTask(f));
				}
			} 
			else {
				return;
			}
			
			/** Update index */
			updateIndexWith(tempIdx);
		}
		
	}
	
	// Do I need 'synchronized' if idx is already thread-safe?
	/**
	 * Takes a WordIndex to update this.ThreadSafeWordIndex with
	 * 
	 * @param tempIdx
	 * 
	 * @see ThreadSafeWordIndex
	 */
	private void updateIndexWith(WordIndex tempIdx) {	
		for(String word : tempIdx.copyWords()) 
			for(String path : tempIdx.copyPaths(word)) 
				for(Integer position : tempIdx.copyPositions(word, path)) 
					this.idx.add(word, path, position);			
	}
	
}
