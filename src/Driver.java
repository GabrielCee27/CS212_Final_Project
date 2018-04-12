import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;


/**
 * Driver to build a word index from HTML/HTM files and handle search queries
 */
public class Driver {

	
	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		
		WordIndex wordIndex = new WordIndex();
		
		IndexHelper idxHelper = new IndexHelper();
		
		QueryHelper queryHelper = new QueryHelper();
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) { 
			
			//Normalizing
//			Path p = Paths.get(argMap.getString("-path"));	
//			File file = new File(p.normalize().toString());
			
			String pathStr = argMap.getString("-path");
			File file = new File(pathStr);
			
			idxHelper.recTraverse(wordIndex, file);
		}

		
		if(argMap.hasFlag("-index")) {
			/** Indication that the wordIndex needs to be written to an output file */
		
			// retrieving path
			String defaultPath = Paths.get(".", "index.json").toString();
			Path indexPath = Paths.get(argMap.getString("-index", defaultPath));
			indexPath = indexPath.toAbsolutePath().normalize();
				
			try {
				JSONWriter.asWordIndex(wordIndex, indexPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
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
		
		
		if(argMap.hasFlag("-results")) {
			
			String resultsDefaultPathStr = Paths.get(".", "results.json").toString();
			Path resultsPath = Paths.get(argMap.getString("-results", resultsDefaultPathStr));
			resultsPath = resultsPath.toAbsolutePath().normalize();
			
			try {
				
				JSONWriter.asQueriesResults(queryHelper, resultsPath);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/** Getting number of threads */
		int numOfThreads = 1;
		if(argMap.hasFlag("-threads")) {
			numOfThreads = argMap.getInt("-threads", 5);
		}
		//System.out.println("numOfThreads: " + numOfThreads);

	} // main

}
