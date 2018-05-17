import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class SearchEngineServer {

	public static final int PORT = 8080;
	
	private static WorkQueue queue = new WorkQueue(5);
	
	private static final int limit = 50;
	
	public static void main(String[] args) throws Exception {
		
		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContext.setContextPath("/");
		servletContext.addServlet(SearchServlet.class, "/");
		servletContext.addServlet(NewCrawlServlet.class, "/newcrawl");
		servletContext.addServlet(SearchHistoryServlet.class, "/searched");
		servletContext.addServlet(SavedLinksServlet.class, "/saved");
		servletContext.addServlet(VisitedHistoryServlet.class, "/visited");
		
		// default handler for favicon.ico requests
		DefaultHandler defaultHandler = new DefaultHandler();
		defaultHandler.setServeIcon(true);
		
		ContextHandler defaultContext = new ContextHandler("/favicon.ico");
		defaultContext.setHandler(defaultHandler);

		// setup handler order
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { defaultContext, servletContext });		
		
		// setup jetty server
		Server server = new Server(PORT);
		server.setHandler(handlers);
		server.start();
		server.join();
	}
	
	private static void buildIndexWith(ThreadSafeWordIndex idx, URL url) {
		WebCrawler webCrawler = new WebCrawler(idx, queue, url, limit);
		webCrawler.crawl();
		queue.finish();
	}

	@SuppressWarnings("serial")
	public static class NewCrawlServlet extends HttpServlet{
		
		private static final String TITLE = "New URL Seed";
		
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			System.out.println(Thread.currentThread().getName() + ": " + request.getRequestURI());
			
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			
			out.printf("<html>%n");
			out.printf("<head><title>%s</title></head>%n", TITLE);
			out.printf("<body>%n");

			out.printf("<form>%n");
			out.printf("<h3>URL: </h3>%n");
			out.printf("<input type=\"text\" name=\"url\" size=\"50\"/>");
			out.printf("<input type=\"submit\" value=\"Submit\">%n");
			out.printf("</form>%n");
			
			String urlStr = request.getParameter("url");
			if(urlStr != null) {
				URL newUrl = new URL(urlStr);
				
				buildIndexWith(Driver.wordIndex, newUrl);
		
				out.printf("<script>");
				out.printf("window.alert(\"New web crawl was successful!\");");
				out.printf("</script>");
			}
				
			out.printf("</body>%n");
			out.printf("</html>%n");

			response.setStatus(HttpServletResponse.SC_OK);
		}
		
	}
	
	@SuppressWarnings({"serial", "unchecked"})
	public static class SearchServlet extends HttpServlet {
		
		private static final String TITLE = "Search Engine";
		
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			System.out.println(Thread.currentThread().getName() + ": " + request.getRequestURI());
			
			HttpSession session = request.getSession(true);
			
			LinkedList <String> history = null;
			String lastVisit = null;
			
			String currentVisit = getDate();
			
			//try to get the current state from session
			try {
				history = (LinkedList<String>) session.getAttribute("history");
				lastVisit = (String) session.getAttribute("lastVisit");
			} catch(Exception ignored) {	
			}
			
			session.setAttribute("lastVisit", currentVisit);
			
			if(history == null)
				history = new LinkedList<String>();
			
			//creating the html page
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			
			out.printf("<html>%n");
			out.printf("<head><title>%s</title></head>%n", TITLE);
			out.printf("<body>%n");
		
			out.printf("<form>%n");
			out.printf("<h3>Query: </h3>%n");
			out.printf("<input type=\"text\" name=\"query\" size=\"30\"/>");
			out.printf("<input type=\"submit\" value=\"Search\">%n");
			out.printf("</form>%n");
			
			if(history != null && history.size() > 0)
				printRecommendedSearches(history, out);
			
			String query = request.getParameter("query");
			if(query != null) {
				history.add(query);
				session.setAttribute("history", history);
				printResults(query, request, response);
			}
			
			if(lastVisit != null)
				out.printf("<p>Last visited: %s.</p>%n", lastVisit);
			
			out.printf("<p>session id: %s</p>", session.getId());
			out.printf("</body>%n");
			out.printf("</html>%n");
		
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
		
		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			String query = request.getParameter("query");
			System.out.println("query in doPost: " + query);
			
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			
			HttpSession session = request.getSession(true);
			LinkedList <String> savedLinks = null;
			LinkedList <String> history = null;
			
			try {
				savedLinks = (LinkedList<String>) session.getAttribute("savedLinks");
				history = (LinkedList<String>) session.getAttribute("history");
			} catch(Exception ignored) {	
			}
			
			if(savedLinks == null)
				savedLinks = new LinkedList<String>();
			
			String link = request.getParameter("link");
			
			if(link != null) {
				savedLinks.add(link);
				session.setAttribute("savedLinks", savedLinks);
				System.out.println("successfully saved " + link);
			}
				
			String redirectPath = request.getServletPath();
			if(history != null)
				redirectPath += "?query=" + history.getLast();
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(redirectPath);
		}

		/**
		 * Returns the date and time in a long format. For example: "12:00 am on
		 * Saturday, January 01 2000".
		 *
		 * @return current date and time
		 */
		public static String getDate() {
			String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
			DateFormat formatter = new SimpleDateFormat(format);
			return formatter.format(new Date());
		}
		
		private static void printResults(String query, HttpServletRequest request, HttpServletResponse response) throws IOException{
			//System.out.println("query: " + query);
			
			PrintWriter out = response.getWriter();
			
			QueryHelper queryHelper = new QueryHelper(queue, false);
			
			long start = System.currentTimeMillis();
			queryHelper.parseAndSearchString(query, Driver.wordIndex);
			queue.finish();
			long totalTime = System.currentTimeMillis() - start;
			
			TreeSet<String> queriesTreeSet = new TreeSet<>(queryHelper.copyQueries());
			
			
			//Note: TreeSet should only be one in size
			for(String q : queriesTreeSet) {
				
				List<Word> resultsList = Word.listByNaturalOrder(queryHelper.copyResults(q));
				out.printf("<h3>%d results for \"%s\" in %d milli seconds</h3>%n", resultsList.size(), query, totalTime);

				for(int i= 0; i < resultsList.size(); i++) {
					out.printf("<form method=\"post\" action=\"%s\">%n", request.getServletPath());
					//System.out.println(resultsList.get(i).getPath());
					String path = resultsList.get(i).getPath();
					//out.printf("<input type=\"hidden\" name=\"visitedLink\" value=\"%s\">", path);
					out.printf("<input type=\"hidden\" name=\"link\" value=\"%s\">", path);
					//out.printf("<a href=\"%s\">%s</a> <input type=\"submit\" value=\"Save\"> <br>", path, getFileName(path));
					out.printf("<a href=\"http://localhost:8080/visited?add=%s\">%s</a> <input type=\"submit\" value=\"Save\"> <br>", path, getFileName(path));
					out.printf("</form>\n%n");
				}
				
			}
		
		}

		public static String getFileName(String path) {
			File file = new File(path);
			return file.getName();
		}
		
		/**
		 * Prints the last 5 searches.
		 * 
		 * @param history
		 * @param out
		 */
		private static void printRecommendedSearches(LinkedList<String> history, PrintWriter out) {
			
			HashSet <String> recommended = new HashSet<String>();
			
			out.printf("<p>Recommended Searches: </p>");
			for(int i = 0; i < history.size() && i < 5; i++) {
				String q = history.get(i);
				if(!recommended.contains(q)) {
					out.printf("<form>%n");
					out.printf("<input type=\"hidden\" value=\"%s\" name=\"query\"/>", history.get(i));
					out.printf("<input type=\"submit\" value=\"%s\">%n", history.get(i));
					out.printf("</form>%n");
					
					recommended.add(history.get(i));
				}
			}
		}
		
	}
	
	@SuppressWarnings({"serial", "unchecked"})
	public static class SearchHistoryServlet extends HttpServlet {
		
		private static final String TITLE = "Search History";
		
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			System.out.println(Thread.currentThread().getName() + ": " + request.getRequestURI());
			
			HttpSession session = request.getSession(true);
			LinkedList <String> history = null;
			
			//try to get the current state
			
			try {
				history = (LinkedList<String>) session.getAttribute("history");	
			} catch(Exception ignored) {
				
			}
			
			//creating the html page
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			
			out.printf("<html>%n");
			out.printf("<head><title>%s</title></head>%n", TITLE);
			out.printf("<body>%n");
			
			out.printf("<h3>Search History:</h3>");
			if(history != null)
				printHistory(history, request, response);
			
			out.printf("<p>session id: %s</p>", session.getId());
			out.printf("</body>%n");
			out.printf("</html>%n");
		
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
		
		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			HttpSession session = request.getSession(true);
			
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			
			session.setAttribute("history", null);
			System.out.println("Cleared search history.");
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(request.getServletPath());
		}
		
		//TODO: Have a button allow the user to clear their history
		private static void printHistory(LinkedList<String> history, HttpServletRequest request, HttpServletResponse response) throws IOException {
			
			PrintWriter out = response.getWriter();
		
			out.printf("<form method=\"post\" action=\"%s\">%n", request.getServletPath());
			out.printf("<input type=\"submit\" name=\"clear\" value=\"Clear\" method=\"post\"> <br>");
			out.printf("</form>");
			for(int i = 0; i < history.size(); i++)
				out.printf("%s<br>", history.get(i));
		}
		
	}
	
	@SuppressWarnings({"serial", "unchecked"})
	public static class VisitedHistoryServlet extends HttpServlet {
		
		private static final String TITLE = "Visited History";
		
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			System.out.println(Thread.currentThread().getName() + ": " + request.getRequestURI());
			
			HttpSession session = request.getSession(true);
			LinkedList <String> visitedLinks = null;
			
			try {
				visitedLinks = (LinkedList<String>) session.getAttribute("visitedLinks");
			} catch(Exception ignored) {	
			}
			
			String add = request.getParameter("add");
			
			session.setAttribute("visitedLinks", visitedLinks);
			
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			
			out.printf("<html>%n");
			out.printf("<head><title>%s</title></head>%n", TITLE);
			out.printf("<body>%n");
			out.printf("<h3> Visited Links: </h3>");
			
			if(add != null) {
				
				if(visitedLinks == null)
					visitedLinks = new LinkedList<String>();
				
				visitedLinks.add(add);
				session.setAttribute("visitedLinks", visitedLinks);
				System.out.println("successfully added " + add + " to visited history.");
				
				response.setStatus(HttpServletResponse.SC_OK);
				response.sendRedirect(add);
			} else {
				System.out.println("add is null");
		
				out.printf("<form method=\"post\" action=\"%s\">%n", request.getServletPath());
				out.printf("<input type=\"submit\" name=\"clear\" value=\"Clear\" method=\"post\"> <br>");
				out.printf("</form>");
				
				if(visitedLinks != null)
					for(int i = 0; i < visitedLinks.size(); i++)
						out.printf("<a href=\"%s\"> %s </a> <br>", visitedLinks.get(i), SearchServlet.getFileName(visitedLinks.get(i)));
			}
			
			out.printf("<p>session id: %s</p>", session.getId());
			out.printf("</body>%n");
			out.printf("</html>%n");
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
		
		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			HttpSession session = request.getSession(true);
			
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			
			session.setAttribute("visitedLinks", null);
			System.out.println("Cleared visited history.");
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(request.getServletPath());
		}
		
	}
	
	@SuppressWarnings({"serial", "unchecked"})
	public static class SavedLinksServlet extends HttpServlet {
		
		private static final String TITLE = "Saved Links";
		
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			System.out.println(Thread.currentThread().getName() + ": " + request.getRequestURI());
			
			HttpSession session = request.getSession(true);
			LinkedList <String> savedLinks = null;
			
			try {
				savedLinks = (LinkedList<String>) session.getAttribute("savedLinks");
			} catch(Exception ignored) {	
			}
			
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			
			out.printf("<html>%n");
			out.printf("<head><title>%s</title></head>%n", TITLE);
			out.printf("<body>%n");
			
			out.printf("<h3> Saved Results: </h3> <br>");
			
			if(savedLinks != null)
				printSavedResults(savedLinks, request, response);
			
			out.printf("<p>session id: %s</p>", session.getId());
			out.printf("</body>%n");
			out.printf("</html>%n");
		
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
			
		}
		
		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			HttpSession session = request.getSession(true);
			
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			
			session.setAttribute("savedLinks", null);
			System.out.println("set savedLinks to null");
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(request.getServletPath());
		}
		
		private static void printSavedResults(LinkedList<String> savedLinks, HttpServletRequest request, HttpServletResponse response) throws IOException {
			PrintWriter out = response.getWriter();
			out.printf("<form method=\"post\" action=\"%s\">%n", request.getServletPath());
			out.printf("<input type=\"submit\" name=\"clear\" value=\"Clear\" method=\"post\"> <br>");
			out.printf("</form>");
			for(int i = 0; i < savedLinks.size(); i++)
				out.printf("<a href=\"%s\"> %s </a> <br>", savedLinks.get(i), SearchServlet.getFileName(savedLinks.get(i)));
		}
		
	}
	
	
}
