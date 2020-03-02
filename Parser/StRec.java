/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

/**
 * StRec class - Container class for symbol table records
 */
public class StRec {

	////////////////////// Variables //////////////////////
	private String name, attribute, tableCode;
	private int br = -1;
	private int offset = -1;

	////////////////////// Constructor //////////////////////
	public StRec(String _name, String _attribute, String _code){
		name = _name;
		attribute = _attribute;
		tableCode = _code;
	}

	////////////////////// Accessors //////////////////////
	public String getName() { return name; }
	public String getAttribute() { return attribute; }
	public String getCode() { return tableCode; }
	public int getBR() { return br; }
	public int getOffset() { return offset; }

	////////////////////// Mutators //////////////////////
	public void setAttribute(String _attribute) { attribute = _attribute; }
	public void setRegInfo(int _baseRegister, int _offset) {
		br = _baseRegister;
		offset = _offset;
	}
}