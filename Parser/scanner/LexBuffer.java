/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

import java.util.List;
import java.util.ArrayList;

/**
 *	LexBuffer class - Holds information about the current lexeme for the tokenizer. 
 */
public class LexBuffer {

	//////////////////////  Globals  //////////////////////
	// Used as temporary tokenID's to make certain types of decisions simpler
	public static final int EMPTY = -1;
	public static final int SYMBL = -2;		
	public static final int COMNT = -3;	
	public static final int PCMNT = -4;	
	public static final int PFLIT = -5;		

	////////////////////// Variables //////////////////////
	private String lex = "";		// Lexeme currently in the buffer
	private int pos = -1;			// Position in the line
	private int tokenID = EMPTY;	// Token ID that the lexeme currently represents
	private int currentLine;		// The line the current character being processed is on
	private int currentPos;			// Position the current character being processed is at

	////////////////////// Accessors //////////////////////
	public String 	getLexeme() 	{ return lex; }
	public int 		getPos() 		{ return pos; }
	public int 		getLine() 		{ return currentLine; }
	public int 		getTokenID() 	{ return tokenID; }

	////////////////////// Mutators  //////////////////////
	public void setTokenID(final int _value) { tokenID = _value; }

	////////////////////// Functions //////////////////////
	
	/**
	 * Updates the line number and position of the current character being looked at.
	 * @param _line - current line
	 * @param _pos  - current position
	 */
	public void updateBuffer(final int _line, final int _pos){
		currentLine = _line;
		currentPos  = _pos;
	}

	/**
	 * Adds a character to the current lexeme. If the lexeme is empty then update the 
	 * position of the lexeme to be the position of the character currently being looked at.
	 * @param _character - Character to be added to the lexeme
	 */
	public void addChar(final char _character) {
		lex += _character;
		if(pos == -1) { pos = currentPos; }
	}

	/**
	 * Overload that allows for the ID of the lexeme to be updated
	 * @param _character - Character to be added to the lexeme
	 * @param _tokenID   - The new token ID
	 */
	public void addChar(final char _character, final int _tokenID) {
		lex += _character;
		if(pos == -1) { pos = currentPos; }
		tokenID = _tokenID;
	}

	public char checkLast(){
		if(lex.length()>0) {
			return lex.charAt(lex.length()-1);
		} else {
			return Character.MIN_VALUE;
		}
	}

	public char popLast(){
		char last = checkLast();
		if(last == Character.MIN_VALUE){
			return Character.MIN_VALUE;
		}
		StringBuilder sb = new StringBuilder(lex);
		sb.deleteCharAt(lex.length()-1);
		lex = sb.toString();
		if(lex.length() == 0){
			reset();
		}
		return last;
	}

	/**
	 * Clears the buffer to allow for a new lexeme. (generally after tokeen creation)
	 */
	public void reset() {
		lex = "";
		pos = -1;
		tokenID = EMPTY;
	}

	/**
	 * Checks if the buffer currently contains a lexeme.
	 * @return - returns true if the token ID is empty, other wise returns false.
	 */
	public boolean isEmpty() {
		if(tokenID == EMPTY){ return true; } 
		else 				{ return false;}
	}

	/**
	 * Creates a token based on the current lexeme and ID then resets the buffer.
	 * @return - returns the generated token, or null if the buffer was empty.
	 */
	public Token createToken(){

		// If the token is a String then remove the quotations
		if(tokenID == ReferenceTable.TSTRG){
			StringBuilder sb = new StringBuilder(lex);	
			sb.deleteCharAt(lex.length()-1);
			sb.deleteCharAt(0);		
			lex = sb.toString();
		}

		if(!this.isEmpty() && tokenID != COMNT){
			Token token = new Token(this.getTokenID(), this.getLine(), this.getPos(), this.getLexeme());	
			this.reset();
			return token;
		} else {
			this.reset();
			return null;
		}
	}	
}