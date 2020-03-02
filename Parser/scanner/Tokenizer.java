/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

import java.util.Queue;
import java.util.LinkedList;

/**
 *	Tokenizer class - Takes each character given to it by the scanner and uses them to compile a 
 *					  list of tokens.
 *					- Utilises the LexBuffer class to store the current lexeme.
 */
public class Tokenizer {
	
	////////////////////// Variables //////////////////////
	private char currentChar;	// current character to be processed
	private Output output;

	private Queue<Token> tokens = new LinkedList<>();		// container for the tokens
	private LexBuffer currentLexeme = new LexBuffer();	// buffer to help decide the ID of the token
	
	////////////////////// Constructor //////////////////////
	public Tokenizer(final Output _output) { output = _output; }

	////////////////////// Accessors //////////////////////
	public Queue<Token> getTokens() { return tokens; }

	////////////////////// Functions //////////////////////

	/**
	 * Decides whether a character is a new line, letter, number or delimiter.
	 * It also skips processing the character if the buffer currently contains a comment.
	 * @param _character 	- the current character the scanner is looking at
	 * @param _lineNo		- the line the character is in
	 * @param _pos			- the position in the string the character is in
	 */
	public void processChar(final char _character, final int _lineNo, final int _pos){
		int id = currentLexeme.getTokenID();		
		currentChar = _character;

		// update the current line and position in the lex buffer
		currentLexeme.updateBuffer(_lineNo, _pos);

		// check some special cases otherwise process the character
		
		// a new line will always just clear the buffer, if the current lexeme is an unfinished string token 
		// then set the token to undefined
		if (currentChar == '\n' ) { 
			if(id == ReferenceTable.TSTRG) {
				currentLexeme.setTokenID(ReferenceTable.TUNDF);
			}
			this.clearBuffer();	
			return;

		// otherwise if the current lexeme in the buffer is a token then we can 
		// ignore all characters until a new line clears the buffer
		} else if(id == LexBuffer.COMNT) { 
			return;

		// otherwise process the current character 
		} else { 
			if(Character.isLetter(_character)) 		{ this.processLetter(); } 
			else if(Character.isDigit(_character))	{ this.processNumber(); } 
			else 									{ this.processDelim();  }
		}		
	}

	/**
	 * Decides how to process a letter. 
	 */
	private void processLetter(){
		int id = currentLexeme.getTokenID(); 

		// If the buffer is currently an identifier or string then just add the character to it		
		if(id == ReferenceTable.TIDEN || id == ReferenceTable.TSTRG){ 									
			currentLexeme.addChar(currentChar); 

		// otherwise clear the buffer and start a new identifier lexeme
		} else { 
			this.clearBuffer();	
			currentLexeme.addChar(currentChar, ReferenceTable.TIDEN);
		}		
	}

	public void endOfFile(){
		Token eofToken = new Token(ReferenceTable.TEOF , currentLexeme.getLine(), 1, null);
		tokens.add(eofToken);
	}

	/**
	 * Decides how to process a number
	 */
	private void processNumber(){
		int id = currentLexeme.getTokenID(); 

		// If the buffer is currently an int, a float or a string then add the character to it
		if(id == ReferenceTable.TILIT || id == ReferenceTable.TFLIT || id == ReferenceTable.TSTRG || id == ReferenceTable.TIDEN){ 
			currentLexeme.addChar(currentChar); 

		// otherwise if the buffer is a potential float (ie <int><dot>) then add the character 
		// and promote the lexeme in the buffer to a float literal
		} else if(id == LexBuffer.PFLIT) { 								   
			currentLexeme.addChar(currentChar, ReferenceTable.TFLIT); 

		// otherwise clear the buffer and start a new lexeme
		} else { 
			this.clearBuffer();	
			currentLexeme.addChar(currentChar, ReferenceTable.TILIT);
		}
	}

	/**
	 * Decides how to process a delimiter
	 */
	private void processDelim(){
		int id = currentLexeme.getTokenID();
		
		// if the delimiter is a space or tab
		if(Character.isSpaceChar(currentChar) || currentChar == '\t' ){

			// if the current lexeme in the buffer is a string then just add the space or tab
			if(id == ReferenceTable.TSTRG){ 
				currentLexeme.addChar(currentChar);	

			// otherwise clear the buffer
			} else { 
				this.clearBuffer();	
			}
		// otherwise check for the special case that the delimiter is an underscore
		} else if (currentChar == '_') { 

			// if the current lexeme is an indentifier or a string then just add the underscore
			if(id == ReferenceTable.TIDEN || id == ReferenceTable.TSTRG){ 
				currentLexeme.addChar(currentChar);

			// otherwise clear the buffer and start a new undefined lexeme
			} else { 				
				this.clearBuffer();
				currentLexeme.addChar(currentChar, ReferenceTable.TUNDF);
			}

		// otherwise check if the delimiter can be determined by the checkSymbol method (see below)
		} else if (this.checkSymbol(currentChar) == ReferenceTable.TUNDF) {

			// if the current lexeme is already undefined or a string then just add the character
			if(id == ReferenceTable.TUNDF || id == ReferenceTable.TSTRG){
				currentLexeme.addChar(currentChar);

			// otherwise clear the buffer and start a new undefined lexeme
			} else {
				this.clearBuffer();
				currentLexeme.addChar(currentChar, checkSymbol(currentChar));
			}
		// if the character makes it this far then it has to be a symbol that is accepted by the language
		} else {
			this.processSymbol();
		}
		
	}

	/**
	 * Decides how to process a symbol
	 */
	private void processSymbol(){
		int id = currentLexeme.getTokenID();

		// Check if the symbol is a string symbol because it is handled differently to all other symbols.
		if(this.checkSymbol(currentChar) == ReferenceTable.TSTRG){

			// if the lexeme in the buffer is already a string then the string clear the buffer.
			if(id == ReferenceTable.TSTRG){	
				currentLexeme.addChar(currentChar);			
				this.clearBuffer();
				return;

			// otherwise clear the buffer and start a new string lexeme
			} else {
				this.clearBuffer();
				currentLexeme.addChar(currentChar, ReferenceTable.TSTRG);
				return;
			}
		} 

		// check the state of the lexeme currently in the buffer 
		switch(id){
			
			// If there is an integer in the buffer then check if the current character is a dot symbol.
			// If it is then add the dot to the lexeme, promoting it to a protential float literal (PFLIT).
			// otherwise clear the buffer and start a new symbol lexeme
			case ReferenceTable.TILIT: 
				if(this.checkSymbol(currentChar) == ReferenceTable.TDOT){
					currentLexeme.addChar(currentChar, LexBuffer.PFLIT);
				} else {
					this.clearBuffer();					
					currentLexeme.addChar(currentChar, LexBuffer.SYMBL);					
				}
				break;

			// If there is a symbol in the buffer then check if the symbols combine to create a composite symbol.
			// otherwise clear the buffer and start a new symboll lexeme
			case LexBuffer.SYMBL: 
				String symbol = currentLexeme.getLexeme()+currentChar;
				if(this.checkSymbol(symbol) != ReferenceTable.TUNDF) {
					currentLexeme.addChar(currentChar, checkSymbol(symbol));

					// if the composite symbol is not a potential comment symbol then clear the buffer.
					if(checkSymbol() != LexBuffer.PCMNT) { 
						this.clearBuffer();
					}
				} else {
					this.clearBuffer();
					currentLexeme.addChar(currentChar, LexBuffer.SYMBL);
				}
				break;

			// If there is a potential comment in the buffer (currently "/-") then check if the current character 
			// is a minus symbol. If it is then add the character and promote the lexeme to a comment
			// otherwise clear the buffer and start a new symbol lexeme
			case LexBuffer.PCMNT: 
				if(this.checkSymbol(currentChar) == ReferenceTable.TMINS) {
					currentLexeme.addChar(currentChar, LexBuffer.COMNT);	
				} else { 
					this.clearBuffer(); 
					currentLexeme.addChar(currentChar, LexBuffer.SYMBL);
				}
				break;

			// If there is a string in the buffer then just add the character to it.
			case ReferenceTable.TUNDF: 				
				if(this.checkSymbol(currentChar) != ReferenceTable.TEQUL || currentLexeme.checkLast() != '!'){			
					this.clearBuffer();				
					currentLexeme.addChar(currentChar, LexBuffer.SYMBL);
					break;
				}		

				char excl = currentLexeme.popLast();
				this.clearBuffer();	
				currentLexeme.addChar(excl);			
				currentLexeme.addChar(currentChar, ReferenceTable.TNEQL);	

				break;

			// If there is a string in the buffer then just add the character to it.
			case ReferenceTable.TSTRG: 				
				currentLexeme.addChar(currentChar);				
				break;

			// Other than the special cases above, a symbol will clear the buffer then start a new symbol lexeme.
			default:				
				this.clearBuffer();				
				currentLexeme.addChar(currentChar, LexBuffer.SYMBL);				
		}		
	}

	/**
	 * Creates a token based on the lexeme currently in the buffer and then clears
	 * the buffer for the next lexeme. 
	 */
	private void clearBuffer(){
		int id = currentLexeme.getTokenID();

		// Special case for token creation if the current lexeme is a potential comment that wasn't completed
		// (ie "/-")
		if(id == LexBuffer.PCMNT){
			this.createPCMNTtokens();
			return;
		}

		// Special case for token creation if the current lexeme is a potential float literal that 
		// wasn't completed (ie didn't have integers after the dot)
		if(id == LexBuffer.PFLIT){
			this.createPFLITtokens();
			return;
		}

		// For comparison purposes, symbols in the buffer have a general ID called SYMBL. 
		// So before the token is created, the specific symbol ID must be determined.
		if(id == LexBuffer.SYMBL) {
			currentLexeme.setTokenID(this.checkSymbol());
		}

		if(id == ReferenceTable.TUNDF) {
			String errorDetails = "'" + currentLexeme.getLexeme() + "' is undefined";
			ErrorReport error = new ErrorReport("Lexical", currentLexeme.getLine(), currentLexeme.getPos(), errorDetails);
			output.addError(error);
		}

		// Have the buffer create a token and if it's not null then add it too the token list.
		Token token = currentLexeme.createToken();
		if(token != null) {tokens.add(token);}
	}

	/**
	 * handles the token creation for the special case that a potential comment symbol (currently "/-")
	 * is found in the buffer and hasn't been completed.
	 */
	private void createPCMNTtokens(){

		// Split the string 
		String div = ""+currentLexeme.getLexeme().charAt(0);
		String min = ""+currentLexeme.getLexeme().charAt(1);

		// Create the separate tokens then add them to the token list.
		Token divToken = new Token(ReferenceTable.TDIVD, currentLexeme.getLine(), currentLexeme.getPos(), div);
		Token minToken = new Token(ReferenceTable.TMINS, currentLexeme.getLine(), currentLexeme.getPos()+div.length(), min);
		tokens.add(divToken);
		tokens.add(minToken);

		// manually reset the buffer.
		currentLexeme.reset();
	}

	/**
	 * handles the token creation for the special case that a potential float literal has been found
	 * in the buffer but hasn't been completed with an integer literal after the dot.
	 */
	private void createPFLITtokens(){
		String intLit = "";
		String dot = "";

		// Split the string into integer and dot
		for(char c : currentLexeme.getLexeme().toCharArray()){
			if(c == '.') { dot += c; } 
			else 		 {intLit += c;}
		}
		
		// Create the separate tokens then add them to the token list.
		Token intToken = new Token(ReferenceTable.TILIT, currentLexeme.getLine(), currentLexeme.getPos(), intLit);
		Token dotToken = new Token(ReferenceTable.TDOT , currentLexeme.getLine(), currentLexeme.getPos()+intLit.length(), dot);
		tokens.add(intToken);
		tokens.add(dotToken);
		
		currentLexeme.reset();
	}
	
	/**
	 * Overload for the checkSymbol method that requires no parameters and uses what ever 
	 * is currently in the buffer
	 * @return - see last check symbol
	 */
	private int checkSymbol(){ return this.checkSymbol(currentLexeme.getLexeme()); }

	/**
	 * Overload for the checkSymbol method that converts a char into a string
	 * @return - see last check symbol
	 */
	private int checkSymbol(final char _char){ return this.checkSymbol(""+_char); }

	/**
	 * Contains a list of all the symbols and composite symbols accepted by the language.
	 * Compares a given string to this list and returns a token ID, a special case ID, 
	 * or defaults to the "undefined" ID
	 * @param  _symbol  - [description]
	 * @return          - [description]
	 */
	private int checkSymbol(final String _symbol){

		int ref = ReferenceTable.checkSymbol(_symbol);

		if(ref != ReferenceTable.TUNDF) { return ref; }

		// Special case symbols
		if(_symbol.equals("/-"))  { return LexBuffer.PCMNT; }
		if(_symbol.equals("/--")) { return LexBuffer.COMNT; }

		return ReferenceTable.TUNDF;
	}
}