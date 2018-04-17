import java.util.Collection;
import java.util.List;

/**
 * A thread-safe version of WordIndex using a read/write lock.
 *
 * @see WordIndex
 * @see ReadWriteLock
 */
public class ThreadSafeWordIndex extends WordIndex {

	private ReadWriteLock lock;
	
	public ThreadSafeWordIndex() {
		super();
		this.lock = new ReadWriteLock();
	}
	
	@Override
	public void add(String word, String path, int position) {
		lock.lockReadWrite();
		
		try {
			super.add(word, path, position);
		}
		finally {
			lock.unlockReadWrite();
		}
	}
	
	@Override
	public void addAll(String[] words, String path) {
		lock.lockReadWrite();
		
		try {
			//ERROR: Would call this add instead of super.add and lead to deadlock
			//super.addAll(words, path);
			
			//FIX: Copied over super.addAll functionality
			for(int i = 0; i < words.length; i++) {	
				super.add(words[i], path, i+1);
			}
		}
		finally {
			lock.unlockReadWrite();
		}
		
	}
	
	@Override
	public int count(String word, String path) {
		lock.lockReadOnly();
		
		try {
			return super.count(word, path);
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public int words() {
		lock.lockReadOnly();
		
		try {
			return super.words();
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public boolean contains(String word) {
		lock.lockReadOnly();
		
		try {
			return super.contains(word);
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public List<String> copyWords() {
		lock.lockReadOnly();
		
		try {
			return super.copyWords();
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public List<Integer> copyPositions(String word, String path) {
		lock.lockReadOnly();
		
		try {
			return super.copyPositions(word, path);
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public List<String> copyPaths(String word){
		lock.lockReadOnly();
		
		try {
			return super.copyPaths(word);
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public String toString() {
		lock.lockReadOnly();
		
		try {
			return super.toString();
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public Collection<Word> exactSearch(List<String> queries) {
		
		lock.lockReadOnly();
		
		try {
			return super.exactSearch(queries);
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	@Override
	public Collection<Word> partialSearch(List<String> queries){
		lock.lockReadOnly();
		
		try {
			return super.partialSearch(queries);
		}
		finally {
			lock.unlockReadOnly();
		}
		
	}
	
	
}
