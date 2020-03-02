/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Scanner class - Processes each character of an input string and sends it to the tokenizer
 *				  - Handles the debug report of the tokens
 */
public class Scanner {

	//////////////////////  Globals  //////////////////////
	public static final int TAB_SIZE = 4;
	public static final int REPORT_WIDTH = 60;

	////////////////////// Functions //////////////////////
	
	/**
	 * Sends each character in the given string to the tokenizer along with position and line info
	 * @param  _input  - String to be tokenized
	 * @return         - Resulting list of tokens
	 */
	public Queue<Token> tokenizeInput(final String _input, final Output _output){
		Tokenizer tokenizer = new Tokenizer(_output);		
		int lineNo = 1;
		int pos = 1;
		String line = "";

		for (char c : _input.toCharArray()){

			// send the char to the tokenizer
			tokenizer.processChar(c, lineNo, pos);
			line += c;
			// Keep track of the characters position in the line
			if(c == '\t'){
				pos += TAB_SIZE;
			} else {
				pos++;
			}
			
			// keep track of the line number
			if(c == '\n'){
				_output.addLineToListing(lineNo, line);
				line = "";
				lineNo++;
				pos = 1;
			}
		}

		// Tell the tokenizer the file is finished
		tokenizer.endOfFile();

		return tokenizer.getTokens();
	}

	/**
	 * Prints out a list of tokens according to the assignment formatting outlines
	 * @param _tokens - list of tokens to print
	 */
	public void printTokens(final Queue<Token> _tokens) {

		String line = "\n";
		
		for (Token t : _tokens) {

			// If the token is undefined then print the current line and start a new one
			if(t.value() == ReferenceTable.TUNDF){
				if(!line.equals("")){
					System.out.println(line);
				}
				line = "";
				System.out.println(t.shortString());

			// otherwise add the tokens string to the line and check if the line length exceeds 60
			} else {
				line += t.shortString();

				if(line.length()>REPORT_WIDTH){
				System.out.println(line);
					line = "";
				}
			}
		}
		System.out.println(line);
		

	}
	
}