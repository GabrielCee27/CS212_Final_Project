import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Data structure to store strings and their positions.
 */
public class WordIndex {

	/**
	 * Stores a mapping of words to the positions the words were found according to path.
	 */
	
	private Map<String, Map<String, Set<Integer>>> idx;
	

	/**
	 * Initializes the index.
	 */
	public WordIndex() {
		idx = new TreeMap<>();
	}

	/**
	 * Adds the word and the position it was found to the index.
	 *
	 * @param word
	 *            word to clean and add to index
	 * @param path
	 * 			  String of where in the directory a word was found
	 * @param position
	 *            position word was found
	 */
	public void add(String word, String path, int position) {

		
		if(idx.containsKey(word)) {
			//already has key
			
			//check if path is set
			if(idx.get(word).containsKey(path)) {
				//contains word and path
				
				if(idx.get(word).get(path) != null) {
					//insert position into existing set
					Set <Integer> set = idx.get(word).get(path);
					set.add(position);
					
				} else {
					//create a new set and initialize with position
					Set <Integer> set = new HashSet<>();
					set.add(position);
					idx.get(word).put(path, set);
				}
				
			} else if(!idx.get(word).isEmpty()) {
				
				//should update current pathMap
				
				Set <Integer> set = new HashSet<>();
				
				set.add(position);
				
				idx.get(word).putIfAbsent(path, set);
				
				
			} else { //does not have path
				
				//create a new pathMap
				
				Map<String, Set<Integer>> pathMap = new TreeMap<>();
				
				Set <Integer> set = new HashSet<>();
				
				set.add(position);
				
				pathMap.put(path, set);
				
				idx.put(word, pathMap);
			}
			
			
		} else {
			//does not contain key
			//insert key
			//create a new set and initialize with position
			
			Set <Integer> set = new HashSet<>();
			set.add(position); //adds to set if not already present
			
			Map<String, Set<Integer>> pathMap = new TreeMap<>();
			pathMap.put(path, set);
			
			idx.put(word, pathMap);
			
		}
		
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at position 0.
	 *
	 * @param words
	 *            array of words to add
	 *            
	 * @see WordIndex#add(String, String, int)
	 */
	public void addAll(String[] words, String path) {
		for(int i = 0; i < words.length; i++) {	
			add(words[i], path, i+1);
		}
	}


	/**
	 * Returns the number of times a word was found (i.e. the number of
	 * positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @param path
	 * 			  String of path to look for
	 * 
	 * @return number of times the word was found
	 */
	public int count(String word, String path) {
		
		//check if the word is even in the index
		if(idx.get(word) == null) {
			return 0;
		} 
		
		Set <Integer> set = idx.get(word).get(path);
		return set.size();
	}

	/**
	 * Returns the number of words stored in the index.
	 *
	 * @return number of words
	 */
	public int words() {
		return idx.size();
	}

	/**
	 * Tests whether the index contains the specified word.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word) {
		
		if(idx.containsKey(word)) {
			return true;
		}
		
		return false;
	}

	/**
	 * Returns a copy of the words in this index as a sorted list.
	 *
	 * @return sorted list of words
	 *
	 * @see ArrayList#ArrayList(java.util.Collection)
	 * @see Collections#sort(List)
	 */
	public List<String> copyWords() {
		
		List <String> list = new ArrayList<>();
		
		list.addAll(idx.keySet());
		
		//Collections.sort(list);
		
		return list;
	}

	/**
	 * Returns a copy of the positions for a specific word and path.
	 *
	 * @param word
	 *            to find in index
	 * @param path
	 * 			  to find in index
	 * 
	 * @return sorted list of positions for that word
	 *
	 * @see ArrayList#ArrayList(java.util.Collection)
	 * @see Collections#sort(List)
	 */
	public List<Integer> copyPositions(String word, String path) {
		
		if(idx.get(word).equals(null)) {
			return null;
		}
		
		List <Integer> list = new ArrayList<>();
	
		list.addAll(idx.get(word).get(path));
		
		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * Returns a copy of the paths for a specific word.
	 *
	 * @param word
	 *            to find in index
	 * @return sorted list of paths for that word
	 *
	 * @see ArrayList#ArrayList(java.util.Collection)
	 * @see Collections#sort(List)
	 */
	public List<String> copyPaths(String word){
		
		List <String> list = new ArrayList<>();
		
		list.addAll(idx.get(word).keySet());
		
		//Collections.sort(list);
		
		return list;
	}
	

	/**
	 * Returns a string representation of this index.
	 */
	@Override
	public String toString() {
		return idx.toString();
	}
	
}