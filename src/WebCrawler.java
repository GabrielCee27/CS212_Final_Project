import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.nio.file.Path;
import java.nio.file.Paths;

//TODO: Documentation
public class WebCrawler {

	/** Shared index */
	private ThreadSafeWordIndex idx;
	
	private final WorkQueue queue;
	
	private final URL base;
	
	/** total number of URLs to crawl */
	private final int limit;
	
	/** Used to avoid repeating urls and to keep count */
	private HashSet<URL> urlSet; 
	
	
	public WebCrawler(ThreadSafeWordIndex idx, WorkQueue queue, URL base, int limit) {
		this.idx = idx;
		this.queue = queue;
		this.base = base;
		this.limit = limit;
		this.urlSet = new HashSet<URL>();
		
	}
	
	public void buildIndex(WordIndex wordIndex, String file, URL url) {
		
		String cleanedTxt = HTMLCleaner.stripHTML(file);
		
		// Avoid empty files
		if(!cleanedTxt.equals("")) { 
			wordIndex.addAll(cleanedTxt.split(" "), url.toString());
		}
		
	}
	
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

	public void crawl() {
		String html = LinkParser.fetchHTML(base);
		ArrayList<URL> urls = LinkParser.listLinks(base, html);
		
		urlSet.add(base);
		
		if(urls.size() > 0) {
			updateUrlSet(urls);
		}
			
		WordIndex tempIdx = new WordIndex();
		buildIndex(tempIdx, html, base);
		this.idx.mergeWith(tempIdx);
	}

	private void updateUrlSet(ArrayList<URL> urls) {
		
		synchronized(urlSet) {
			for(URL url : urls)	
				if(urlSet.size() < limit && !urlSet.contains(url) && IndexHelper.isHTMLorHTM(url.toString())) {
					queue.execute(new WebCrawlTask(url));
					urlSet.add(url);
				}
		}
		
	}
	
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
			
			if(urls.size() > 0) {
				updateUrlSet(urls);
			}
				
			buildIndex(tempIdx, html, target);
			idx.mergeWith(tempIdx);

		}

	}
	
}
