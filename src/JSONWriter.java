import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Converts data structures into "pretty" JSON format.
 */
public class JSONWriter {

	/**
	 * Returns a String with the specified number of tab characters.
	 *
	 * @param times
	 *            number of tab characters to include
	 * @return tab characters repeated the specified number of times
	 */
	public static String indent(int times) {
		char[] tabs = new char[times];
		Arrays.fill(tabs, '\t');
		return String.valueOf(tabs);
	}

	/**
	 * Returns a quoted version of the provided text.
	 *
	 * @param text
	 *            text to surround in quotes
	 * @return text surrounded by quotes
	 */
	public static String quote(String text) {
		return String.format("\"%s\"", text);
	}

	/**
	 * Writes the set of elements as a JSON array at the specified indent level.
	 *
	 * @param writer
	 *            writer to use for output
	 * @param elements
	 *            elements to write as JSON array
	 * @param level
	 *            number of times to indent the array itself
	 * @throws IOException
	 */
	private static void asArray(Writer writer, TreeSet<Integer> elements, int level) throws IOException {
			
		writer.write("[\n");
		
		for(Integer i : elements) {
			
			writer.write(indent(level) + i.toString());
			
			if(i != elements.last()) {
				writer.write(",");
			}
			
			writer.write("\n");
		}
		
		writer.write(indent(level-1) + "]");
	}
	
	/**
	 * Writes the set of elements as a JSON array at the specified indent level.
	 *
	 * @param writer
	 *            writer to use for output
	 * @param elements
	 *            elements to write as JSON array
	 * @param level
	 *            number of times to indent the array itself
	 * @throws IOException
	 */
	private static void asArray(Writer writer, List <Integer> elements, int level) throws IOException {
		
		writer.write("[\n");
			
		for(int i=0; i< elements.size(); i++) {
			
			writer.write(indent(level) + elements.get(i).toString());
			
			if(i != elements.size()-1) {
				writer.write(",");
			}
			
			writer.write("\n");
		}
		
		writer.write(indent(level-1) + "]");
	}

	/**
	 * Writes the set of elements as a JSON array to the path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asArray(TreeSet<Integer> elements, Path path) throws IOException {
 
		try (
				BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
			){
			
				bw.write("[");
				bw.newLine();
				
				for(Integer i : elements) {
					
					bw.write("\t");
					
					if(i != elements.last()) {
						bw.write(i.toString() + ",");
					} else {
						bw.write(i.toString());
					}
					
					bw.newLine();
				}
				
				bw.write("]");
				bw.close();
		}
	}
	
	
	/**
	 * Writes the map of elements as a JSON object to the path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON object
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asObject(TreeMap<String, Integer> elements, Path path) throws IOException {
		
		try(
				BufferedWriter writer =  Files.newBufferedWriter(path, StandardCharsets.UTF_8);
			){
			
			writer.write("{");
			writer.newLine();
			
			for(String key : elements.keySet()) {
				
				writer.write("\t");
				
				Integer e = elements.get(key);
				
				 if(key != elements.lastKey()) {
					 writer.write(quote(key) + ": " + e.toString() + ",");
				 } else { 
					 writer.write(quote(key) + ": " + e.toString());
				 }
				
				writer.newLine();
			}
			
			writer.write("}");
			writer.close();
			
		}
		
	}
	
	/**
	 * Writes the set of elements as a JSON object with a nested array to the
	 * path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON object with a nested array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asNestedObject(TreeMap<String, TreeSet<Integer>> elements, Path path) throws IOException {
	
		try(
				BufferedWriter writer = Files.newBufferedWriter(path,  StandardCharsets.UTF_8);
				
				){
			
			writer.write("{");
			writer.newLine();
			
			for(String key : elements.keySet()) {
				
				writer.write("\t");
				
				TreeSet <Integer> set = elements.get(key);
				
				writer.write(quote(key) + ": ");
				
				 
				 asArray(writer, set, 2);
				 

				 if(key != elements.lastKey()) { 
					 writer.append(',');
				 }
				
				writer.newLine();
			}
			
			writer.write("}");
			writer.close();
		}
	
	}
	
	
	/**
	 * Writes the set of elements as a JSON object with a nested object to the
	 * path using UTF8.
	 *
	 * @param wordIndex
	 *            WordIndex to write as a JSON object with a nested array
	 * @param path
	 *            path to write file
	 */
	public static void asWordIndex(WordIndex wordIndex, Path p) {
		
		try {
			
			BufferedWriter writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8);
			
			writer.write("{\n");
			
			List <String> words = wordIndex.copyWords();
			
			for(int i=0; i < words.size(); i++) {
				
				writer.write(indent(1));
				
				writer.write(quote(words.toArray()[i].toString()));
				
				writer.write(": {\n");
				
				List <String> paths = wordIndex.copyPaths(words.toArray()[i].toString());
				
				for(int j=0; j < paths.size(); j++) {
					
					writer.write(indent(2));
					
					writer.write(quote(paths.toArray()[j].toString()));
					
					List <Integer> positions = wordIndex.copyPositions(words.toArray()[i].toString(), paths.toArray()[j].toString());
					
					//array
//					writer.write(":");
//					asArray(writer, positions, 3);
					
					writer.write(": [\n");
					
					for(int k = 0; k < positions.size(); k++) {
						
						writer.write(indent(3));
						
						writer.write(positions.toArray()[k].toString());
							
						if(k != positions.size()-1) {
							writer.write(",");
						}
							
						writer.write("\n");
					}
					
					writer.write(indent(2) + "]");
					//end of array
					
					if(j != paths.size()-1) {
						writer.write(",");
					}
					
					writer.write("\n");
					
				}
				
				writer.write(indent(1) + "}");
				
				if(i != words.size()-1) {
					writer.write(",");
				}
				
				writer.write("\n");
			}
			
			
			writer.write("}");
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
