import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Parses queries and saves the results into a HashSet.
 */
public class QueryHelper {
	
	//TODO: make thread-safe
	public Map<String, HashSet<Word>> queriesResults;
	
	//Need volatile?
	private volatile Boolean exactSearch;
	
	private final WorkQueue queue;
	
	//private ThreadSafeWordIndex idx;
	
	/**
	 * Exact search is off by default.
	 */
	public QueryHelper(WorkQueue queue) {
		this.queriesResults = new HashMap<>();
		this.exactSearch = false;
		this.queue = queue;
	}
	
	/**
	 * Turns exact search on.
	 */
	public void exactSearchOn() {
		this.exactSearch = true;
	}
	
	/**
	 * Turns exact search off.
	 */
	public void exactSearchOff() {
		this.exactSearch = false;
	}
	
	/**
	 * Sorts a string of queries alphabetically.
	 * 
	 * @param str
	 * 			String to sort
	 * @see Arrays#sort(Object[])
	 * @see String#join(CharSequence, Iterable)
	 * @return sorted String
	 */
	public String sortQueries(String str) {
		
		String[] strArr = str.split(" ");
		
		Arrays.sort(strArr);
		
		return String.join(" ", strArr);
	}

	/**
	 * Parses file and searches line by line.
	 * 
	 * @param path
	 * 			Query file location
	 * @param wordIndex
	 * 			WordIndex to search from
	 * @throws IOException
	 */
	public void parseAndSearchFile(Path path, WordIndex wordIndex) throws IOException {
		
		try(
				BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
		){
			String str = null;
			
			while((str = reader.readLine()) != null) {
				
				//System.out.println("Original query: " + str);
				
				String cleanedTxt = cleanTxt(str);
				
				if(!cleanedTxt.isEmpty()) {
					String sortedQueries = sortQueries(cleanedTxt);
					if(!queriesResults.containsKey(sortedQueries))
						search(sortedQueries, wordIndex);
				}	
					
			}
			
		}
		
	}
	
public void parseAndSearchFile(Path path, ThreadSafeWordIndex wordIndex) throws IOException {
		
		try(
				BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
		){
			String str = null;
			
			while((str = reader.readLine()) != null) {
				
				String cleanedTxt = cleanTxt(str);
				
				if(!cleanedTxt.isEmpty()) {
					String sortedQueries = sortQueries(cleanedTxt);
					if(!queriesResults.containsKey(sortedQueries)) {
						queue.execute(new SearchTask(sortedQueries, wordIndex));
					}
				}	
					
			}
			
		}
		
	}
	
	/** 
	 * Use appropriate search from WordIndex and save results.
	 * 
	 * @param queriesStr
	 * 			String of queries
	 * @param wordIndex
	 * 			WordIndex to search from
	 * @see WordIndex#exactSearch(List)
	 * @see WordIndex#partialSearch(List)
	 */
	private void search(String queriesStr, WordIndex wordIndex){
			
		List<String> queriesList = Arrays.asList(queriesStr.split(" "));
		
		HashSet<Word> resultsHashSet = new HashSet<>();
			
		if(exactSearch)	
			resultsHashSet.addAll(wordIndex.exactSearch(queriesList));
				
		else	
			resultsHashSet.addAll(wordIndex.partialSearch(queriesList));


		queriesResults.put(queriesStr, resultsHashSet);
	}

	private class SearchTask implements Runnable{
		
		ThreadSafeWordIndex idx;
		
		String queriesStr;
		
		List<String> queriesList;
		
		HashSet<Word> resultsHashSet;
		
		public SearchTask(String queriesStr, ThreadSafeWordIndex wordIndex) {
			this.queriesStr = queriesStr;
			this.queriesList = Arrays.asList(queriesStr.split(" "));
			this.resultsHashSet = new HashSet<>();
			this.idx = wordIndex;
		}
	
		@Override
		public void run() {
			
			if(exactSearch) {
				resultsHashSet.addAll(idx.exactSearch(queriesList));
			}			
			else	{
				resultsHashSet.addAll(idx.partialSearch(queriesList));	
			}
			
			//safely update global results
			synchronized(queriesResults) {
				queriesResults.put(queriesStr, resultsHashSet);
				//System.out.println("queriesResults now: " + queriesResults.toString());
			}
			
		}
		
	}

	/**
	 * Cleans string.
	 * 
	 * @param str
	 * 			String to clean
	 * @see HTMLCleaner
	 * @return cleaned string
	 */
	private String cleanTxt(String str) {
		String txt = HTMLCleaner.stripPunctuations(str);
		txt = HTMLCleaner.stripNumbers(txt);
		txt = HTMLCleaner.cleanLines(txt);
		txt = txt.toLowerCase();
		txt = txt.trim();
		return txt;
	}
	
	
	/**
	 * Returns a sorted list of the queries from queriesResults.
	 * 
	 * @see Collections#sort(List)
	 * 
	 * @return sorted list of queries
	 */
	public List<String> copyQueries(){
		
		List <String> list = new ArrayList<>();
		
		list.addAll(queriesResults.keySet());
		
		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * Returns a list of results from queriesResults
	 * 
	 * @param query
	 * 			String of query
	 * @return list of results
	 */
	public List<Word> copyResults(String query){
		
		if(queriesResults.get(query).equals(null)) {
			return null;
		}
		
		List <Word> list = new ArrayList<>();
	
		list.addAll(queriesResults.get(query));
		
		return list;
	}
	
}
