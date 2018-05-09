import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Driver to build a word index from HTML/HTM files and handle search queries.
 */
public class Driver {

	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		
		//TODO: If no flag, run without multi-threading
		int threads = 1;
		if(argMap.hasFlag("-threads")) {
			threads = argMap.getInt("-threads", 5);
		}
		System.out.println("Number of threads: " + threads);
		
		WorkQueue queue = new WorkQueue(threads);
		
		ThreadSafeWordIndex wordIndex = new ThreadSafeWordIndex();
		
		IndexHelper idxHelper = new IndexHelper(wordIndex, queue);
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) { 
			Path p = Paths.get(argMap.getString("-path"));	
			File file = new File(p.normalize().toString());
			
			/** Start building the index with file/dir */
			idxHelper.dirTraverse(file);
			
			// Project 2
			//idxHelper.recTraverse(wordIndex, file);
		}
		
		WebCrawler webCrawler;
		if(argMap.hasFlag("-url") && argMap.hasValue("-url")) {
			String urlStr = argMap.getString("-url");
			URL url;
			try {
				url = new URL(urlStr);
				
				System.out.println("url: " + argMap.getString("-url"));
				
				int limit = argMap.getInt("-limit", 50);
				System.out.println("limit: " + argMap.getString("-limit"));
				
				webCrawler = new WebCrawler(wordIndex, queue, url, limit);
				
				//TODO: Traverse with given URL
				//webCrawler.crawl();
				webCrawler.executeBase();
//				webCrawler.crawlRec(url);
				
			} catch (MalformedURLException e) {
				//TODO: Error handle
				e.printStackTrace();
			}
		}
		
		/** Waits until the index is done being built until moving on */
		queue.finish();
//		System.out.println("Index is done being built.");
//		System.out.println("wordIndex: \n" + wordIndex.toString());
		
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
		
		//QueryHelper queryHelper = new QueryHelper(queue);
		QueryHelper queryHelper;
		
		if(argMap.hasFlag("-query") && argMap.hasValue("-query")) {
			
			Path queryPath = Paths.get(argMap.getString("-query"));
			
			queryHelper = new QueryHelper(queue, argMap.hasFlag("-exact"));
			
			try {
				queryHelper.parseAndSearchFile(queryPath, wordIndex);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		else {
			queryHelper = new QueryHelper(queue);
		}
		
		queue.finish();
		if(argMap.hasFlag("-results")) {
			
			//As long as writing out results happens after finish(), it's safe.
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
