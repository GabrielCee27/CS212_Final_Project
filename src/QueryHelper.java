import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.util.ArrayList;


public class QueryHelper {
	
	/** Can change inner HashSet to List if able to remove duplicates from queries */
	private Map<String, HashSet<Word>> queriesResults;
	
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
			
			//System.out.println("QueryHelper.parseFile:");
			
			while((str = reader.readLine()) != null) {
				
				System.out.println("Original query: " + str);
				
				//Remove special characters
				String cleanedTxt = cleanTxt(str);
				
				//Sort words in query
				List <String> queriesList = Arrays.asList(cleanedTxt.split(" "));
				Collections.sort(queriesList);
				
				//System.out.println(queriesList.toString());
				
				String queriesStr = String.join(" ", queriesList);
			
				if(!queriesStr.isEmpty())
					search(String.join(" ", queriesList), wordIndex);
				
			}
			
		}
		
	}
	
	/** Use appropriate search from WordIndex and save results */
	/** FIX: Don't search for empty strings */
	/** Why is an empy string being found in word index? */
	private void search(String queriesStr, WordIndex wordIndex){
	
		//String queriesStr = String.join(" ", queriesList);
		
		System.out.println("query: " + queriesStr);
		
		if(!queriesResults.containsKey(queriesStr)) {
			
			List<String> queriesList = Arrays.asList(queriesStr.split(" "));
			
			//populate queriesResults
			if(exactSearch) {
				
				//HashSet<Word> resultsHashSet = (HashSet<Word>) wordIndex.exactSearch(queriesList);
				
				//TODO:
				
				HashSet<Word> resultsHashSet= new HashSet<>(wordIndex.exactSearch(queriesList));
				
				queriesResults.put(queriesStr, resultsHashSet);
				
				//System.out.println("queriesResults: " + queriesResults.toString());
				
			} else {
				//TODO: perform partial search
				
				HashSet<Word> resultsHashSet= new HashSet<>(wordIndex.partialSearch(queriesList));
				queriesResults.put(queriesStr, resultsHashSet);
				
			}
			
			//TODO: add to quereiesResults?
			
		} else {
			System.out.println("Already searched this query.");
		}
		
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
		
		//TODO: Send to Word to be sorted in natural order
		
		return list;
	}
	
	public void writeToFile(Path path) throws IOException {
		
		JSONWriter.asQueriesResults(this.queriesResults, path);
		
	}
	
	
	
}
