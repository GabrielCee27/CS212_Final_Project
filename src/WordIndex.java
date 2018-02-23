import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	 * Stores a mapping of words to the positions the words were found.
	 */
	//private Map<String, Set<Integer>> index;
	
	private Map<String, Map<String, Set<Integer>>> idx;
	

	/**
	 * Initializes the index.
	 */
	public WordIndex() {
		/*
		 * TODO: Choose the best data structures. Keep in mind you do not want
		 * duplicates, and do not need to store anything in sorted order.
		 */
		//index = new HashMap<>();
		//index = new TreeMap<>();
		
		idx = new TreeMap<>();
	}

	/**
	 * Adds the word and the position it was found to the index.
	 *
	 * @param word
	 *            word to clean and add to index
	 * @param position
	 *            position word was found
	 */
	public void add(String word, String path, int position) {
		/*
		 * TODO: Make sure you initialize any inner data structures.
		 */
		
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
	 * at position 1.
	 *
	 * @param words
	 *            array of words to add
	 *
	 * @see #addAll(String[], int)
	 */
	public void addAll(String[] words, String path) {
		
		addAll(words, path, 1);
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at the provided starting position
	 *
	 * @param words
	 *            array of words to add
	 * @param start
	 *            starting position
	 */
	public void addAll(String[] words, String path, int start) {
		/*
		 * TODO: Add each word using the start position. (You can call your
		 * other methods here.)
		 */
		
		for(int i = start; i < words.length; i++) {
//			System.out.println(words[i] + " at " + (i+1));
			
//			if(!words[i].equals("")) {
//				add(words[i], path, i+1);
//			}
			
			add(words[i], path, i+1);

		}
	}

	/**
	 * Returns the number of times a word was found (i.e. the number of
	 * positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	public int count(String word, String path) {
		/*
		 * TODO: Return the count.
		 */
		
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
		/*
		 * TODO: Return number of words. No counting is necessary!
		 */
		
		//return the number of keys
		//return index.size();
		
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
		/*
		 * TODO: Return whether the word is in the index.
		 */
		
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
		/*
		 * TODO: Create a copy of the words in the index as a list, and sort
		 * before returning.
		 */
		
		List <String> list = new ArrayList<>();
		
		list.addAll(idx.keySet());
		
		Collections.sort(list);
		
		//Collections.reverse(list);
		
		//System.out.println("list: " + list.toString());
		
		return list;
	}

	/**
	 * Returns a copy of the positions for a specific word.
	 *
	 * @param word
	 *            to find in index
	 * @return sorted list of positions for that word
	 *
	 * @see ArrayList#ArrayList(java.util.Collection)
	 * @see Collections#sort(List)
	 */
	public List<Integer> copyPositions(String word, String path) {
		/*
		 * TODO: Create a copy of the positions for the word, and sort before
		 * returning.
		 */
		
		if(idx.get(word).equals(null)) {
			return null;
		}
		
		List <Integer> list = new ArrayList<>();
	
		list.addAll(idx.get(word).get(path));
		
		Collections.sort(list);
		
		return list;
	}
	
	public List<String> copyPaths(String word){
		
		List <String> list = new ArrayList<>();
		
		list.addAll(idx.get(word).keySet());
		
		Collections.sort(list);
		
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