import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;


/**
 * Driver to build a word index from HTML/HTM files and handle search queries.
 */
public class Driver {

	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		
		/** Get the number of threads */
		int threads = 1;
		if(argMap.hasFlag("-threads")) {
			threads = argMap.getInt("-threads", 5);
		}
		//System.out.println("Number of threads: " + threads);
		
		WorkQueue queue = new WorkQueue(threads);
		
		ThreadSafeWordIndex wordIndex = new ThreadSafeWordIndex();
		
		IndexHelper idxHelper = new IndexHelper(wordIndex, queue);
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) { 
			//Normalizing
//			Path p = Paths.get(argMap.getString("-path"));	
//			File file = new File(p.normalize().toString());
			
			String pathStr = argMap.getString("-path");
			File file = new File(pathStr);
			
			/** Start building the index with file/dir */
			idxHelper.dirTraverse(file);
			
			// Project 2
			//idxHelper.recTraverse(wordIndex, file);
		}
		
		queue.finish();
		
		//System.out.println("Index is done being built.");
		//System.out.println(wordIndex.toString());
		
		if(argMap.hasFlag("-index")) {
			/** wordIndex needs to be written to an output file */
		
			String defaultPath = Paths.get(".", "index.json").toString();
			Path indexPath = Paths.get(argMap.getString("-index", defaultPath));
			indexPath = indexPath.toAbsolutePath().normalize();

			try {
				JSONWriter.asWordIndex(wordIndex, indexPath);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		QueryHelper queryHelper = new QueryHelper(queue);
		
		if(argMap.hasFlag("-query") && argMap.hasValue("-query")) {
			
			Path queryPath = Paths.get(argMap.getString("-query"));
			
			/** Because search is being called inside the queryHelper */
			if(argMap.hasFlag("-exact"))
				queryHelper.exactSearchOn();
			
			try {
				queryHelper.parseAndSearchFile(queryPath, wordIndex);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		queue.finish();

		//System.out.println("Queue results done being built.");
		
		if(argMap.hasFlag("-results")) {
			
			//TODO: Safely get query results info
			//synchronize?
			
			String resultsDefaultPathStr = Paths.get(".", "results.json").toString();
			Path resultsPath = Paths.get(argMap.getString("-results", resultsDefaultPathStr));
			resultsPath = resultsPath.toAbsolutePath().normalize();
			
			try {
				JSONWriter.asQueriesResults(queryHelper, resultsPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		queue.shutdown();
	} // main

}
