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


public class QueryHelper {
	
	public Map<String, HashSet<Word>> queriesResults;
	
	private Boolean exactSearch;
	
	public QueryHelper() {
		this.queriesResults = new HashMap<>();
		this.exactSearch = false;
	}
	
	public void exactSearchOn() {
		this.exactSearch = true;
	}
	
	public void exactSearchOff() {
		this.exactSearch = false;
	}
	
	
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
	
	public String sortQueries(String str) {
		
		String[] strArr = str.split(" ");
		
		Arrays.sort(strArr);
		
		return String.join(" ", strArr);
	}
	
	/** Use appropriate search from WordIndex and save results */
	private void search(String queriesStr, WordIndex wordIndex){
		
		//System.out.println("query: " + queriesStr);
			
		List<String> queriesList = Arrays.asList(queriesStr.split(" "));
		
		HashSet<Word> resultsHashSet = new HashSet<>();
			
		if(exactSearch)	
			resultsHashSet.addAll(wordIndex.exactSearch(queriesList));
				
		else	
			resultsHashSet.addAll(wordIndex.partialSearch(queriesList));


		queriesResults.put(queriesStr, resultsHashSet);
	}

	
	private String cleanTxt(String str) {
		String txt = HTMLCleaner.stripPunctuations(str);
		txt = HTMLCleaner.stripNumbers(txt);
		txt = HTMLCleaner.cleanLines(txt);
		txt = txt.toLowerCase();
		txt = txt.trim();
		return txt;
	}
	
	public List<String> copyQueries(){
		
		List <String> list = new ArrayList<>();
		
		list.addAll(queriesResults.keySet());
		
		Collections.sort(list);
		
		return list;
	}
	
	public List<Word> copyResults(String query){
		
		if(queriesResults.get(query).equals(null)) {
			return null;
		}
		
		List <Word> list = new ArrayList<>();
	
		list.addAll(queriesResults.get(query));
		
		return list;
	}
	
}
