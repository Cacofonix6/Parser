/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

import java.util.List;
import java.util.ArrayList;

public class ErrorReport implements Comparable<ErrorReport>{

	private int line;
	private int pos;
	private String message;
	private String type;

	public int getLine() { return line; }
	public int getPos() { return pos; }

	public ErrorReport(String _type, int _line, int _pos, String _message) {
		line = _line;
		pos = _pos;
		message = _message;
		type = _type;
	}
	
	public String report() {
		String out = type + " Error at Line: " + line + ", Position: " + pos;
		out += "\n    " + message + "\n";
		return out;
	}

	public int compareTo(ErrorReport e){
		if(line < e.line)
			return -1;
		if(line > e.line)
			return 1;
		if(pos < e.pos)
			return -1;
		if(pos > e.pos)
			return 1;
		return 0;
	}
}