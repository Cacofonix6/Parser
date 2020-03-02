
// COMP3290 CD19 Compiler
//
//	Token class	- constructs a token on behalf of the scanner for it to be sent to the parser.
//				- IDs/FLITs/Strings do not have their symbol table reference set in this class,
//			      this is best done within the scanner as it makes things easier in later phases,
//			      when we are dealing with things like variable scoping.
//
//    Rules of Use: The text for this class has been extracted from a working CD13 scanner.
//			  Code released via Blackboard may not be passed on to anyone outside this
//			  semester's COMP3290 class.
//			  You may not complain or expect any consideration if the code does not work
//			  the way you expect it to.
//			  It is supplied as an assistance and may be used in your project if you wish.
//
//	8
//
//

package scanner;

public class Token {
		
	private int tid;		// token number - for token classification
	private int line;		// line number on listing
	private int pos;		// character position within line
	private String str;		// lexeme - actual character string from scanner for TIDEN/TILIT/TFLIT/TSTRG
	
	public Token(int t, int ln, int p, String s) {  //Constructor takes in token number, line, column & lexeme
		tid = t;
		line = ln;
		pos = p;
		if(t >= 58 && t <= 62){ // The lexeme will only be kept if the token is a TIDEN, TILIT, TFLIT, TSTRG or TUNDF
			str = s;	
		} else {
			str = null;
		}				


		if (tid == ReferenceTable.TIDEN) {				// Identifier lexeme could be a reserved keyword
			int v = ReferenceTable.checkKeywords(s);	// (match is case-insensitive)
			if (v > 0) { tid = v; str = null; }	// if keyword, alter token type and set lexeme to null
		}
	}

	public int value() { return tid; }
	public int getLn() { return line; }
	public int getPos() { return pos; }
	public String getStr() { return str; }

	public String toString() {				// This does NOT produce output for the Scanner Phase	   *****
		String s = ReferenceTable.TPRINT[tid]+" " + line + " " + pos;	// It is meant to be used for diagnostic printing only	   *****
		if (str == null) return s;			// It may give you some ideas wrt reporting lexical errors *****
		if (tid != ReferenceTable.TUNDF)
			s += " " + str;
		else {
			s += " ";
			for (int i=0; i<str.length(); i++) { // output non-printables as ascii codes
				char ch = str.charAt(i);
				int j = (int)ch;
				if (j <= 31 || j >= 127) s += "\\" +j; else s += ch;
			}
		}
		return s;
	}

	public String shortString() {		// This produces a string which may be useful for output in the Scanner Phase	*****
		String s = ReferenceTable.TPRINT[tid];		// Token as a string
		if (str == null) return s;	// If that is all - return
		if (tid == ReferenceTable.TSTRG) {		// For Strings - add the lexeme with ""
			s += "\""+str + "\" ";
			int j = (6 - s.length()%6) % 6;
			for (int i=0; i<j; i++)
				s += " ";	// right-fill with spaces
			return s;		// return ID/ILIT/FLIT
		}
		if (tid != ReferenceTable.TUNDF) {		// For IDs, ILITS and FLITs - add the lexeme
			s += str + " ";
			int j = (6 - s.length()%6) % 6;
			for (int i=0; i<j; i++)
				s += " ";	// right-fill with spaces
			return s;		// return ID/ILIT/FLIT
		}
		s += "\n" + "lexical error: ";
		for (int i=0; i<str.length(); i++) { // output non-printables as ascii codes
			char ch = str.charAt(i);
			int j = (int)ch;
			if (j <= 31 || j >= 127) s += "\\" +j; else s += ch;
		}
		return s;
	}
}