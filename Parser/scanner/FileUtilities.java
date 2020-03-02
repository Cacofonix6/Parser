/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * FileUtilities interface - contains global functions to handle files
 */
public interface FileUtilities {
	
	
	/**
	 * Converts a text file to a string
	 * @param  _filePath - path of the file
	 * @return           - text file as a string (null if an error occurs)
	 */
	public static String textFileReader(final String _filePath) {

		String out = "";
		String line;

		File file = new File(_filePath);
		
		try {
			
			// Create the file reading utilities
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);            

            // Read each line of the file and add it too the output
            while((line = bufferedReader.readLine()) != null) {	        
            	out += line + '\n';   		            	
            }
            
            bufferedReader.close();

		} catch(FileNotFoundException ex) {
            System.out.println("\nUnable to open file at '" + _filePath + "'");
			return null;            
        } catch(IOException ex) {
            System.out.println("\nError reading file at '" + _filePath + "'");
			return null;             
        }

        return out;
	}

	/**
	 * Converts a text file to a string
	 * @param  _filePath - path of the file
	 * @return           - text file as a string (null if an error occurs)
	 */
	public static List<String> textFileToStringList(final String _filePath) {

		List<String> out = new ArrayList<>();
		String line;

		File file = new File(_filePath);
		
		try {
			
			// Create the file reading utilities
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);            

            // Read each line of the file and add it too the output
            while((line = bufferedReader.readLine()) != null) {	        
            	out.add(line);   		            	
            }
            
            bufferedReader.close();

		} catch(FileNotFoundException ex) {
            System.out.println("\nUnable to open file at '" + _filePath + "'");
			return null;            
        } catch(IOException ex) {
            System.out.println("\nError reading file at '" + _filePath + "'");
			return null;             
        }

        return out;
	}

	/**
	 * overload that checks for a given file at the path given as well as in a default folder to allow for shorter 
	 * command line calls
	 * @param  _filePath    - path or name of the file
	 * @param  _defaultPath - secondary path to check
	 * @return              - text file as a string (null if an error occurs)
	 */
	public static String textFileReader(final String _filePath, final String _defaultPath) {

		String out = "";

		File file = new File(_filePath);
		
		// try the file path
		out = FileUtilities.textFileReader(_filePath);

		// if the file path failed then try and find it in the default path
		if(out == null){
			System.out.println("\nChecking the " + _defaultPath + " folder...");
			out = FileUtilities.textFileReader(_defaultPath + _filePath);
		}

        return out;
	}
	
	/**
	 * Returns the extention of a file as a string 
	 * @param  _file - the file to be checked
	 * @return       - the string after the last '.' in the file name
	 */
	public static String getFileExtention(File _file) {
		
		if(_file.isDirectory()) {
			return "";
		}
		
		// Split the file name by '.'
		String[] fileNameParts = _file.getName().split("\\.");
			
		// return the last element of fileNameParts
		if(fileNameParts.length > 0) {
			return fileNameParts[fileNameParts.length - 1];
		} else {
			return "";	
		}		
	}

	public static void writeToFile(String _out, String _path){
		PrintWriter printWriter = null;
		try
        {
			File file = new File (_path);
            printWriter = new PrintWriter(file);
			printWriter.println(_out);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if ( printWriter != null ) 
            {
                printWriter.close();
            }
        }
	}
}