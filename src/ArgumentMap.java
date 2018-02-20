import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ArgumentMap {

	private final Map<String, String> map;
	
	/**
	 * Initializes the argument map.
	 */
	public ArgumentMap() {
		map = new HashMap<>();
	}

	/**
	 * Initializes the argument map and parses the specified arguments into
	 * key/value pairs.
	 *
	 * @param args
	 *            command line arguments
	 *
	 * @see #parse(String[])
	 */
	public ArgumentMap(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Parses the specified arguments into key/value pairs and adds them to the
	 * argument map.
	 *
	 * @param args
	 *            command line arguments
	 */
	public void parse(String[] args) {
		
		for(int i=0; i < args.length; i++) {
			
			//System.out.println("args[" + i + "]: " + args[i]);
			
			if(isFlag(args[i])) {
				//System.out.println("flag");
				
				if( (i+1 < args.length) && isValue(args[i+1]) )  {
					
					System.out.println("put key: " + args[i] + " val: " + args[i+1]);
					map.put(args[i], args[i+1]);
					i = i+1;
					
				} else { //if not a value or no value found
					System.out.println("No value");
					
					if(hasFlag(args[i])) {
						//replace?
						map.replace(args[i], null);
					} else {
						map.put(args[i], null);
					}
				
				}
					
			} 
			//else {
//				System.out.println("Not a flag");
//			}
		
		}; //loop
		
	}

	/**
	 *
	 * @param arg
	 * @return
	 */
	public static boolean isFlag(String arg) {
		//valid flags: "-path" or "-index"
		//"-url"
		
		arg.trim();
		
		if(arg.equalsIgnoreCase("-index") || arg.equalsIgnoreCase("-path") || arg.equalsIgnoreCase("-url")) {
			return true;
		}
		
		return false;
	}

	/**
	 *
	 * @param arg
	 * @return
	 */
	public static boolean isValue(String arg) {
		
		arg.trim();
		
		if(arg == null || arg == "" || arg.charAt(0) == '-') {
			return false;
		}	
		
		return true; // TODO
	}

	/**
	 * Returns the number of unique flags stored in the argument map.
	 *
	 * @return number of flags
	 */
	public int numFlags() {
		return map.size();
	}

	/**
	 * Determines whether the specified flag is stored in the argument map.
	 *
	 * @param flag
	 *            flag to test
	 *
	 * @return true if the flag is in the argument map
	 */
	public boolean hasFlag(String flag) {
		return map.containsKey(flag); // TODO (1 LOC)
	}

	/**
	 * Determines whether the specified flag is stored in the argument map and
	 * has a non-null value stored with it.
	 *
	 * @param flag
	 *            flag to test
	 *
	 * @return true if the flag is in the argument map and has a non-null value
	 */
	public boolean hasValue(String flag) {
		
		if(map.get(flag) != null) {
			return true;
		}
		
		return false; // TODO (1 LOC)
	}

	/**
	 * Returns the value for the specified flag as a String object.
	 *
	 * @param flag
	 *            flag to get value for
	 *
	 * @return value as a String or null if flag or value was not found
	 */
	public String getString(String flag) {
		return map.get(flag); // TODO (1 LOC)
	}

	/**
	 * Returns the value for the specified flag as a String object. If the flag
	 * is missing or the flag does not have a value, returns the specified
	 * default value instead.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if flag or value is missing
	 * @return value of flag as a String, or the default value if the flag or
	 *         value is missing
	 */
	public String getString(String flag, String defaultValue) {
		
		if(hasFlag(flag) && hasValue(flag)) {
			return getString(flag);
		}
		
		return defaultValue; // TODO
	}

	/**
	 * Returns the value for the specified flag as an int value. If the flag is
	 * missing or the flag does not have a value, returns the specified default
	 * value instead.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if the flag or value is missing
	 * @return value of flag as an int, or the default value if the flag or
	 *         value is missing
	 */
	public int getInteger(String flag, int defaultValue) {
		
		int value = 0;
		
		try {
			value = Integer.parseInt(map.get(flag));
		} catch (Exception e) {
			System.out.println(e);
			return defaultValue;
		}
		
		return value; // TODO
	}

	@Override
	public String toString() {
		return map.toString();
	}
	
	
}
