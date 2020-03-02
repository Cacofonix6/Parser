/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

import scanner.*;
import java.io.*;

import java.util.Queue;
import java.util.LinkedList;

/**
 * A3 class - loads an input file from console command and runs it through the pipeline
 * 			- Contains the main funcion
 */
public class A3 {

	//////////////////////  Globals  //////////////////////
	private static final String DEFAULT_TESTFILE = "PolyTest.txt";
	private static final String DEFAULT_FILEPATH = "TestFiles\\";
	private static final String RESULT_FILEPATH  = "Results\\Results.txt";
	private static final String TREE_FILEPATH    = "Results\\SyntaxTree.txt";	
	private static final String LISTING_FILEPATH = "Results\\ProgramListing.lst";

	/**
	 * main function that sets up the scanner
	 * @param args - console input
	 */
	public static void main(final String[] args) {

		String input = null;
		Scanner scanner = new Scanner();
		Parser parser = new Parser();
		Queue<Token> tokens = new LinkedList<>();
		Output output = new Output();

		// If the a filepath was entered in the console then check that path, otherwise use the default file
		if(args.length > 0)	{
			input = FileUtilities.textFileReader(args[0], DEFAULT_FILEPATH);				
		} else {			
			input = FileUtilities.textFileReader(DEFAULT_FILEPATH + DEFAULT_TESTFILE);			
		}

		// If an input was found then tokenize it
		if(input != null){
			tokens = scanner.tokenizeInput(input, output);
		} else {
			System.out.println("Invalid input");
			return;
		}

		// Parse the tokens
		TreeNode tree = parser.parse(tokens, output);

		// Print a program listing
		output.printListing(LISTING_FILEPATH);	

		// If the tree is not null then the parse was successful
		if(tree != null ){
			System.out.println("Parse Successfull!!");		
		} else {	
			System.out.println(output.reportErrors());
			return;
		}

		// Print the results
		FileUtilities.writeToFile(TreeNode.drawTree(tree), TREE_FILEPATH);
		FileUtilities.writeToFile(TreeNode.printTree(tree), RESULT_FILEPATH);
	}
}