import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves html files using sockets and parses them using regex.
 */
public class LinkParser {
	
	/** Port used by socket. For web servers, should be port 80. */
	public static final int DEFAULT_PORT = 80;

	/** Version of HTTP used and supported. */
	public static final String version = "HTTP/1.1";
	
	/** Valid HTTP method types. */
	public static enum HTTP {
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	};
	
	/**
	 * Removes the fragment component of a URL (if present), and properly
	 * encodes the query string (if necessary).
	 *
	 * @param url
	 *            url to clean
	 * @return cleaned url (or original url if any issues occurred)
	 */
	public static URL clean(URL url) {
		try {
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), null).toURL();
		}
		catch (MalformedURLException | URISyntaxException e) {
			return url;
		}
	}

	/**
	 * Crafts a minimal HTTP/1.1 request for the provided method.
	 *
	 * @param url
	 *            - url to fetch
	 * @param type
	 *            - HTTP method to use
	 *
	 * @return HTTP/1.1 request
	 *
	 * @see {@link HTTP}
	 */
	public static String craftHTTPRequest(URL url, HTTP type) {
		String host = url.getHost();
		String resource = url.getFile().isEmpty() ? "/" : url.getFile();

		// The specification is specific about where to use a new line
		return String.format("%s %s %s\r\n" + "Host: %s\r\n" + "Connection: close\r\n" + "\r\n", type.name(), resource,
				version, host);
	}
	
	/**
	 * Will connect to the web server and fetch the URL using the HTTP request
	 * provided. It would be more efficient to operate on each line as returned
	 * instead of storing the entire result as a list.
	 *
	 * @param url
	 *            - url to fetch
	 * @param request
	 *            - full HTTP request
	 *
	 * @return the lines read from the web server
	 *
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static List<String> fetchLines(URL url, String request) throws UnknownHostException, IOException {
		ArrayList<String> lines = new ArrayList<>();
		int port = url.getPort() < 0 ? DEFAULT_PORT : url.getPort();

		try (
				Socket socket = new Socket(url.getHost(), port);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
		) {
			writer.println(request);
			writer.flush();

			String line = null;

			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		return lines;
	}
	
	/**
	 * Helper method that parses HTTP headers into a map where the key is the
	 * field name and the value is the field value. The status code will be
	 * stored under the key "Status".
	 *
	 * @param headers
	 *            - HTTP/1.1 header lines
	 * @return field names mapped to values if the headers are properly
	 *         formatted
	 */
	public static Map<String, String> parseHeaders(List<String> headers) {
		Map<String, String> fields = new HashMap<>();

		if (headers.size() > 0 && headers.get(0).startsWith(version)) {
			fields.put("Status", headers.get(0).substring(version.length()).trim());

			for (String line : headers.subList(1, headers.size())) {
				String[] pair = line.split(":", 2);

				if (pair.length == 2) {
					fields.put(pair[0].trim(), pair[1].trim());
				}
			}
		}

		return fields;
	}
	
	/**
	 * Fetches the HTML for the specified URL (without headers).
	 *
	 * @param url
	 *            - url to fetch
	 * @return HTML as a single {@link String}, or null if not HTML
	 *
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String fetchHTML(URL target) {
	
		String request = craftHTTPRequest(target, HTTP.GET);
		List<String> lines;
		try {
			lines = fetchLines(target, request);
			
			int start = 0;
			int end = lines.size();

			// Determines start of HTML versus headers.
			while (!lines.get(start).trim().isEmpty() && start < end) {
				start++;
			}

			// Double-check this is an HTML file.
			Map<String, String> fields = parseHeaders(lines.subList(0, start + 1));
			String type = fields.get("Content-Type");

			if (type != null && type.toLowerCase().contains("html")) {
				return String.join(System.lineSeparator(), lines.subList(start + 1, end));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns a list of all the HTTP(S) links found in the href attribute of the
	 * anchor tags in the provided HTML. The links will be converted to absolute
	 * using the base URL and cleaned (removing fragments and encoding special
	 * characters as necessary). Should not include links in the href attribute of the link tag
	 *
	 * @param base
	 *            base url used to convert relative links to absolute3
	 * @param html
	 *            raw html associated with the base url
	 * @return cleaned list of all http(s) links in the order they were found
	 */
	public static ArrayList<URL> listLinks(URL base, String html) {
	
		ArrayList<URL> links = new ArrayList<URL>();
		
		//Matches with opening anchor tag
		String regex = "<(?!\\/)(?i)a[\\s\\S]*?>";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(html);
		
		while(m.find()) {
			
			String link = getHrefLink(m.group());	
			if(link != "") {
				
				try {
					URL absolute = new URL(base, link);
					
					//If no authority, don't add
					if(absolute.getAuthority() != null) {
						links.add(clean(absolute));
					}
						
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		} //while

		return links;
	}
	
	/**
	 * Uses regex to search for href attribute, if found returns it's value. If not found or if
	 * the value is empty, returns "".
	 * 
	 * @param str
	 * 			String to look for href in.
	 * @return
	 * 			returns a String representation of href value (without quotations) or "".
	 */
	private static String getHrefLink(String str) {
		
		String regex = "(?i)href[\\s\\S]*?=[\\s\\S]*?\"[\\s\\S]*?\"";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		
		if(m.find()) {	
			//Remove quotations
			String[] urlSplit = m.group().split("\"");
			return urlSplit[urlSplit.length - 1];
		}
		
		return "";
	}
	
}
