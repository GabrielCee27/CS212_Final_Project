import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Word implements Comparable<Word> {
	
	/** Frequency of word */
	public int frequency;
	
	/** Initial position */
	public int position;
	
	/** Path of file where the word was found */
	public final String path;
	
	
	/**
	 * Initializes a word from the provided parameters.
	 *
	 * @param word
	 *           Word as a string
	 * @param frequency
	 *            Frequency of word
	 * @param position
	 *            Initial position
	 * @param path
	 *            Location
	 */
	public Word(String path, int frequency, int position) {
		this.frequency = frequency;
		this.position = position;
		this.path = path;
	}
	
	@Override
	public String toString() {
		return String.format("%d times, %d position, at path: %s.", this.frequency, this.position, this.path);
	}
	
	public void updatePosition(int position) {
		
		if(position < this.position) {
			this.position = position;
		}
		
	}
	
	public void addToFrequency(int frequency) {
		this.frequency += frequency;
	}
	
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
	
	public static final Comparator<Word> POSITION_COMPARATOR = new Comparator<Word>() {
		
		@Override
		public int compare(Word o1, Word o2) {
			
			int positionCompare = Integer.compare(o1.position, o2.position);
			
			if( positionCompare == 0)
				return o1.compareTo(o2);
			
			return positionCompare;
		}
		
	};
	
	public static final Comparator<Word> PATH_COMPARATOR	= new Comparator<Word>() {
		
		@Override
		public int compare(Word o1, Word o2) {
			
			int pathCompare = o1.path.compareToIgnoreCase(o2.path);
			
			if(pathCompare == 0)
				return o1.compareTo(o2);
			
			return pathCompare;
		}
		
	};
	
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
