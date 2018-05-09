import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Builds index from the web using a breadth-first manner.
 */
public class WebCrawler {

	/** Shared index */
	private ThreadSafeWordIndex idx;
	
	private final WorkQueue queue;
	
	private final URL base;
	
	/** total number of URLs to crawl */
	private final int limit;
	
	/** To avoid repeating urls and to keep count */
	private HashSet<URL> urlSet; 
	
	/**
	 * Initialized the class with the index, work queue, url base, and limit of crawls.
	 * 
	 * @param idx
	 * 			index to build
	 * @param queue
	 * 			WorkQueue to use
	 * @param base
	 * 			URL to start from
	 * @param limit
	 * 			the number of urls to parse
	 */
	public WebCrawler(ThreadSafeWordIndex idx, WorkQueue queue, URL base, int limit) {
		this.idx = idx;
		this.queue = queue;
		this.base = base;
		this.limit = limit;
		this.urlSet = new HashSet<URL>();
	}
	
	/**
	 * Populates wordIndex by parsing the file given.
	 * 
	 * @param wordIndex
	 * 			index to populate
	 * @param file
	 * 			String to parse
	 * @param url
	 * 			path to use
	 */
	public void buildIndex(WordIndex wordIndex, String file, URL url) {
		
		String cleanedTxt = HTMLCleaner.stripHTML(file);
		
		if(!cleanedTxt.equals("")) 
			wordIndex.addAll(cleanedTxt.split(" "), url.toString());
	}
	
	/**
	 * Web crawler using recursion.
	 * 
	 * @param target
	 * 			URL to crawl
	 */
	public void crawlRec(URL target) {		
		
		System.out.println("target: " + target.toString());
		
		if(urlSet.size() < limit && IndexHelper.isHTMLorHTM(target.toString())) {
			
			urlSet.add(target);
			
			String html = LinkParser.fetchHTML(target);
			
			WordIndex tempIdx = new WordIndex();
			buildIndex(tempIdx, html, target);
			this.idx.mergeWith(tempIdx);
			
			ArrayList<URL> urls = LinkParser.listLinks(base, html);
			System.out.println("Found " + urls.size() + " urls");
			
			for(URL url : urls) {	
				if(!urlSet.contains(url)) {
					crawlRec(url);
				}
				else {
					System.out.println("Url has already been processed");
				}
			}//for
			
		}
		else if(urlSet.size() == limit) {
			System.out.println("Counter reached limit");
		}
	
	}

	/**
	 * Starts the web crawling and places any found links into the queue.
	 */
	public void crawl() {
		String html = LinkParser.fetchHTML(base);
		ArrayList<URL> urls = LinkParser.listLinks(base, html);
		
		urlSet.add(base);
		
		if(urls.size() > 0)
			updateUrlSet(urls);
			
		WordIndex tempIdx = new WordIndex();
		buildIndex(tempIdx, html, base);
		this.idx.mergeWith(tempIdx);
	}

	/**
	 * Safely updates the url set with urls and adds them to the work queue.
	 * 
	 * @param urls
	 * 			ArrayList to update the url set with
	 */
	private void updateUrlSet(ArrayList<URL> urls) {
		synchronized(urlSet) {
			for(URL url : urls)	
				if(urlSet.size() < limit && !urlSet.contains(url) && IndexHelper.isHTMLorHTM(url.toString())) {
					queue.execute(new WebCrawlTask(url));
					urlSet.add(url);
				}
		}	
	}
	
	/**
	 * Creates a Runnable task that parses html files given a url.
	 * Populates a temporary index and merges with shared index when done.
	 */
	private class WebCrawlTask implements Runnable{

		private final URL target;
		
		/** Temporary index that will merge with shared index when done being populated */
		private WordIndex tempIdx;
		
		public WebCrawlTask(URL target) {
			this.target = target;
			this.tempIdx = new WordIndex();
		}
		
		@Override
		public void run() {
				
			String html = LinkParser.fetchHTML(target);
			ArrayList<URL> urls = LinkParser.listLinks(base, html);
			
			if(urls.size() > 0)
				updateUrlSet(urls);
				
			buildIndex(tempIdx, html, target);
			idx.mergeWith(tempIdx);
		}

	}
	
}
