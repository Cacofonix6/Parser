/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Output class - Handles error reporting and program listing
 */
public class Output {
	////////////////////// Variables //////////////////////
	private List<ErrorReport> errors = new ArrayList<>();
	private List<Integer> lines = new ArrayList<>();
	private Map<Integer, String> programListing = new HashMap<>();
	private Map<Integer, ErrorReport> errorListing = new HashMap<>();

	////////////////////// Mutators //////////////////////	
	public void addError(ErrorReport error) { 
		errors.add(error); 
		errorListing.put(error.getLine(), error);
	}

	public void addLineToListing(int lineNo, String line) {
		programListing.put(lineNo, line);
		lines.add(lineNo);
	}

	////////////////////// Functions //////////////////////
	
	/**
	 * Formats all the errors into a string
	 * @return - Report in string form
	 */
	public String reportErrors() {
		String out = "";
		
		Collections.sort(errors);
		for(ErrorReport e : errors){
			out += e.report()+ "\n";
		}

		return out;
	}

	/**
	 * Formats the program listing and erros into a string and prints it to the 
	 * given filepath.
	 * @param filePath - File path of output 
	 */
	public void printListing(String filePath){
		String out = "";
		int index = 0;
		Collections.sort(errors);

		for(Integer i : lines){
			out += String.format("%4s", i);			
			out += ": " + programListing.get(i);			
		}

		out += "\n"+ reportErrors();
		FileUtilities.writeToFile(out, filePath);
	}
}