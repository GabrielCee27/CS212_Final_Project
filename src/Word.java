import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Uses frequency, position, and path to compare words.
 * 
 * @see Comparable
 */
public class Word implements Comparable<Word> {
	
	/** Frequency of word */
	private int frequency;
	
	/** Initial position */
	private int position;
	
	/** Path of file where the word was found */
	private final String path;
	
	
	/**
	 * Initializes a word from the provided parameters.
	 * 
	 * @param path
	 *            Location
	 * @param frequency
	 *            Frequency of word
	 * @param position
	 *            Initial position
	 */
	public Word(String path, int frequency, int position) {
		this.frequency = frequency;
		this.position = position;
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public int getPosition() {
		return position;
	}
	
	@Override
	public String toString() {
		return String.format("%d times, %d position, at path: %s.", this.frequency, this.position, this.path);
	}
	
	/**
	 * Updates position and frequency appropriately.
	 * 
	 * @param frequency
	 * @param position
	 */
	public void update(int frequency, int position) {
		
		this.frequency += frequency;
		
		if(position < this.position) {
			this.position = position;
		}
	}
	
	/**
	 * Compares Word by frequency in descending order. If frequency is the same,
	 * compares Word by position in ascending order. If position is the same,
	 * compares Word by path in ascending order.
	 * 
	 * @param other
	 * 			Word to compare to
	 */
	public int compareTo(Word other) { //Natural order
		
		int frequencyCompare = Integer.compare(other.frequency, this.frequency);
		
		if(frequencyCompare == 0) {
			
			int positionCompare = Integer.compare(this.position, other.position);
			
			if(positionCompare == 0) {
				
				return this.path.compareToIgnoreCase(other.path);
				
			} else {
				return positionCompare;
			}
			
		} else {
			return frequencyCompare;
		}
		
	}
	
	/**
	 * Returns an ArrayList of Words by natural order
	 * 
	 * @param words
	 * 			collection of words to sort
	 * @return sorted ArrayList of Words
	 */
	public static final ArrayList<Word> listByNaturalOrder(Collection<Word> words) {
		
		ArrayList<Word> list = new ArrayList<Word>(words);
		
		Collections.sort(list, new Comparator<Word>() {

			@Override
			public int compare(Word o1, Word o2) {
				return o1.compareTo(o2);
			}
			
		});
		
		return list;
		
	}
	
	
}
