Angus Walsh 
3268157

Java version 1.8.0

To compile:

	javac A3.java

To execute with absolute path:

	java A3 "<filePath>"

	notes:	- Quotes aren't neccesary unless the path contains a space character
			- The program will look in the "TestFiles" folder if <filePath> fails.
			  This mean that the program will run using just a file name, provided it's in the "TestFiles" folder

default execution:

	java A3

	note:	- This will run the program using the default test file "PolyTest.txt" in the "TestFiles" folder 



******* FOLDERS *******

Grammar folder:
	
	Contains the grammar used to write the parser.
	Has had left recursion removed and mostly left factored 
		- The left factoring left to do is <callstat> and <asgnstat> both, either directly or indirectly, 
		  starting with <id>. This wasn't altered in the grammar as it would change alot of things so it is handled
		  in the parser by looking ahead in the tokens for a left parenthesis symbol.

Results folder:
	
	Destination for the preorder traversal, syntax tree drawing and program listing documents.

Scanner folder:
	
	Contains the scanner implementation and some of the utility classes.

TetstFiles folder:
	
	As mentioned above, files can be added to this folder to remove the need for absolute paths.
	It also contains the "PolyTest.txt" file which is one I used for testing alot as it has a bit of everything.