import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class Driver {
	
	
	public static void pathDetails(Path path) {
		String format = "%22s: %s%n";
		
		System.out.printf(format, "toString()", path.toString());
		System.out.printf(format, "isAbsolute()", path.isAbsolute());
		System.out.printf(format, "getParent()", path.getParent());
		System.out.printf(format, "getRoot()", path.getRoot());
		System.out.printf(format, "getFileName()", path.getFileName());
		System.out.printf(format, "normalize()", path.normalize());
		System.out.printf(format, "toAbsolutePath()", path.toAbsolutePath());
		System.out.println();
	}
	
	
	public static void readFileAt(Path p) throws IOException {
		try(
				BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8);
				){
				
					System.out.println("Reading File");
					String str = null;
					while((str = reader.readLine()) != null) { //while the new line is not null
				
						System.out.println(str);
						
			}//while
		}
	}
	
	public static Path retrieveIndexPath(ArgumentMap argMap) {
		
		Path defaultPath = Paths.get(".", "index.json");
		
		Path p = Paths.get(argMap.getString("-index", defaultPath.toString()));
		//p = p.toAbsolutePath().normalize();
		
		return p.toAbsolutePath().normalize();
	}

	public static File createIndexFile(Path indexPath) {
		File indexFile = new File(indexPath.toString());
		
		if(indexFile.exists() && indexFile.delete()) {
			System.out.println("Deleted exsistng file.");
		} else {
			try {
				indexFile.createNewFile();
				System.out.println("Created a new index file.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return indexFile;
	}
	

	public static void main(String[] args) {

		// TODO
		System.out.println("args: " + Arrays.toString(args));
		
		ArgumentMap argMap = new ArgumentMap(args);
		System.out.println("numFlags(): " + argMap.numFlags());
		
		Path p = null;
		
		if(argMap.hasFlag("-path") && argMap.hasValue("-path")) {
			p = Paths.get(argMap.getString("-path"));
		} else { //Should throw an exception? IndexTest.ExceptionTest.test07
			System.out.println("No path given.");
			
			//return;
		}
		
		
		if(argMap.hasFlag("-index")) {
		
			Path indexPath = retrieveIndexPath(argMap); //FIX: Make argMap global?
			File indexFile = createIndexFile(indexPath);
			
		} else {
			System.out.println("No index file created.");
		}
		
		
		
	}
	

}
