import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Cleans simple, validating HTML 4/5 into plain-text words using regular
 * expressions.
 *
 * @see <a href="https://validator.w3.org/">validator.w3.org</a>
 * @see <a href="https://www.w3.org/TR/html51/">HTML 5.1 Specification</a>
 * @see <a href="https://www.w3.org/TR/html401/">HTML 4.01 Specification</a>
 *
 * @see java.util.regex.Pattern
 * @see java.util.regex.Matcher
 * @see java.lang.String#replaceAll(String, String)
 */
public class HTMLCleaner {

	/**
	 * Replaces all HTML comments with a single space. For example, "A<!-- B
	 * -->C" will become "A C".
	 *
	 * @param html
	 *            text including HTML comments to remove
	 * @return text without any HTML comments
	 */
	public static String stripComments(String html) {
		
		String regex = "<!--[\\S\\s]*?-->"; 
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(html);
		
		return m.replaceAll(" ");
	}

	/**
	 * Replaces everything between the element tags and the element tags
	 * themselves with a single space. For example, consider the html code: *
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed,
	 * and replaced with a single space.
	 *
	 * @param html
	 *            text including HTML elements to remove
	 * @param name
	 *            name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 */
	public static String stripElement(String html, String name) {
		
		String regex = String.format("<(?i)(%s)[\\s\\S]*?(<\\/(?i)(%s)[\\s\\S]*?>)", name, name);
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(html);
		
		return m.replaceAll(" ");
	}

	/**
	 * Replaces all HTML tags with a single space. For example, "A<b>B</b>C"
	 * will become "A B C".
	 *
	 * @param html
	 *            text including HTML tags to remove
	 * @return text without any HTML tags
	 */
	public static String stripTags(String html) {
		
		String regex = "<\\/*[\\s\\S]*?>";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(html);
		
		return m.replaceAll(" ");
	}

	/**
	 * Replaces all HTML entities with a single space. For example,
	 * "2010&ndash;2012" will become "2010 2012".
	 *
	 * @param html
	 *            text including HTML entities to remove
	 * @return text without any HTML entities
	 */
	public static String stripEntities(String html) {
		
		String regex = "&(?!\\s)\\S*?;";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(html);
	
		return m.replaceAll(" ");
		
	}

	/**
	 * Replaces all numbers with a single space.
	 *
	 * @param html
	 *            text including numbers to remove
	 * @return text without any numbers
	 */
	public static String stripNumbers(String txt) {
		
		String regex = String.format("\\d");
		
		Pattern p = Pattern.compile(regex);
		
		Matcher m = p.matcher(txt);
		
		return m.replaceAll(" ");

	}
	
	// Proffessor Recomended
	public static String stripNonWords(String txt) {
		
		String regex = String.format("(?U)[^\\p{Alpha}\\p{Space}]+");
		
		Pattern p = Pattern.compile(regex);
		
		Matcher m = p.matcher(txt);
		
		return m.replaceAll(" ");
	}

	/**
	 * Replaces all punctuations with a single space.
	 *
	 * @param html
	 *            text including punctuations to remove
	 * @return text without any punctuations
	 */
	public static String stripPunctuations(String txt) {
		
		String regex = String.format("(?![a-zA-ZÀ-ÿ])[\\W\\_]");
	
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(txt);
		
		return m.replaceAll(" ");
	}

	/**
	 * Replaces all spaces of two or more with one single space.
	 *
	 * @param html
	 *            text including spaces to remove
	 * @return text separated by a single space
	 */
	public static String cleanLines(String txt) {
		
		String regex = String.format("\\s{2,}");
		
		Pattern p = Pattern.compile(regex);
		
		Matcher m = p.matcher(txt);
		
		return m.replaceAll(" ");

	}

/**
	 * Removes all HTML (including any CSS and JavaScript).
	 *
	 * @param html
	 *            text including HTML to remove
	 * @return text without any HTML, CSS, or JavaScript
	 */
	public static String stripHTML(String html) {
		
		html = stripComments(html);
		
		html = stripElement(html, "head");	
		html = stripElement(html, "style");
		html = stripElement(html, "script");

		html = stripTags(html);
		
		html = stripEntities(html);
		
		html = stripNumbers(html);
		
		//html = stripNonWords(html);
		
		html = stripPunctuations(html);
		
		html = cleanLines(html);
		
		html = html.toLowerCase();
		html = html.trim();
		
		return html; 
	}

	
}