import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Handler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Driver to build a word index from HTML/HTM files and handle search queries.
 */
public class Driver {

	public static ThreadSafeWordIndex wordIndex = new ThreadSafeWordIndex();
	
	public static void main(String[] args) {
		
		ArgumentMap argMap = new ArgumentMap(args);
		
		//TODO: If no flag, run without multi-threading
		int threads = 1;
		if(argMap.hasFlag("-threads")) {
			threads = argMap.getInt("-threads", 5);
		}
		//System.out.println("Number of threads: " + threads);
		
		WorkQueue queue = new WorkQueue(threads);
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) { 
			Path p = Paths.get(argMap.getString("-path"));	
			File file = new File(p.normalize().toString());
			
			IndexHelper idxHelper = new IndexHelper(wordIndex, queue);
			/** Start building the index with file/dir */
			idxHelper.dirTraverse(file);
			
			// Project 2
			//idxHelper.recTraverse(wordIndex, file);
		}
		
		if(argMap.hasFlag("-url") && argMap.hasValue("-url")) {
			try {
				URL url = new URL(argMap.getString("-url"));
				int limit = argMap.getInt("-limit", 50);
				WebCrawler webCrawler = new WebCrawler(wordIndex, queue, url, limit);
				webCrawler.crawl();	
			} catch (MalformedURLException e) {
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
				System.out.println("index results can be found at: " + indexPath.toString());
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
				System.out.println("query results can be found at: " + resultsPath.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(argMap.hasFlag("-port")) {
			Integer port = argMap.getInt("-port", 8080);
			try {
				//Change main arguments to int?
				SearchEngineServer.main(new String[] {port.toString()});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		queue.shutdown();
	} // main

}
