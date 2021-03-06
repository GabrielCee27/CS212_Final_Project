
/**
 * A simple custom lock that allows simultaneously read operations, but
 * disallows simultaneously write and read/write operations.
 *
 * Does not implement any form or priority to read or write operations. The
 * first thread that acquires the appropriate lock should be allowed to
 * continue.
 */
public class ReadWriteLock {
	private int readers;
	private int writers;

	/**
	 * Initializes a multi-reader single-writer lock.
	 */
	public ReadWriteLock() {
		readers = 0;
		writers = 0;
	}

	/**
	 * Will wait until there are no active writers in the system, and then will
	 * increase the number of active readers.
	 */
	public synchronized void lockReadOnly() {
		//System.out.println("lockReadOnly");
		while(writers > 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		readers++;
	}

	/**
	 * Will decrease the number of active readers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockReadOnly() {
		//System.out.println("UnlockReadOnly");
		readers--;
		
		//to notify a thread trying to write
		this.notify();
	}

	/**
	 * Will wait until there are no active readers or writers in the system, and
	 * then will increase the number of active writers.
	 */
	public synchronized void lockReadWrite() {
		//System.out.println("lockReadWrite");
		while(readers > 0 || writers > 0) {
			try {
				this.wait();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		writers++;
	}

	/**
	 * Will decrease the number of active writers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockReadWrite() {
		//System.out.println("unlockReadWrite");
		writers--;
		this.notifyAll();
	}
}
