/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * SymTab class - Stores records in a single hash map using codes that are
 * 				  a combination of the scope and the string number as keys.
 * 				- Retrieves records from the hashmap.
 * 				- Sets the register information of function parameters and locals.
 */
public class SymTab {

	////////////////////// Globals //////////////////////
	/// Names of some constant scopes that will be needed. All these names
	/// are also keywords in the grammar, this is because when new scopes are
	/// created they are given the name of the function that created them. Naming
	/// them after keywords prevents a new scope clashing with them.
	public static final String  CONSTANTS 	= "constants", 
								GLOBAL 		= "cd19", 
								MAIN 		= "main", 
								TYPES 		= "types"; 

	////////////////////// Variables //////////////////////		

	// All the keys are stored in a list so the records can be accessed in the order they were added.
	private List<String> keys = new ArrayList<>();
	
	// Maps unique identifier strings to a number to save space. Used to generate record keys.
	private Map<String, Integer> strings = new HashMap<>();
	
	// Maps unique scope strings to a number to save space. Used to generate record keys.
	private Map<String, Integer> scopes = new HashMap<>();

	// Main map that stores records. keys are a string of the combined scope number and string number.
	// eg. if the variable name "iter" is mapped to 3 and it's in the "main" scope (mapped to 2) then
	// the key would be "32".
	private Map<String, StRec> records = new HashMap<>();
		
	private int scopeNumber = 0;		// Incremented number to assign to scopes in the scopes map.
	private int stringNumber = 0;		// Incremented number to assign to strings in the strings map.
	private String currentScope = "";	// The current scope the parser is in		
	private StRec program;				// Separate record for the program 
	
	// Variables to set register info of functions (see setFuncDetails()).
	private int funcOffset = 16;		
	private int paramOffset = -8;
	private int funcStart = 0;


	////////////////////// Accessors //////////////////////
	public StRec getProgramRecord() { return program; }

	////////////////////// Constructor //////////////////////
	public SymTab(String programName){
		program = new StRec(programName, "program", "PROG");

		// create the constant scopes
		createScope(CONSTANTS);	// scope 0
		createScope(GLOBAL);	// scope 1
		createScope(MAIN);		// scope 2
		createScope(TYPES);		// scope 3
	}

	////////////////////// Functions //////////////////////
	
	/**
	 * Creates a new scope using the provided name
	 * @param scope - scope name
	 */
	private void createScope(String scope) {
		scopes.put(scope, scopeNumber);
		scopeNumber++;
	}

	/**
	 * Enters name into the strings map if its not already there
	 * @param name [description]
	 */
	private void enterName(String name){
		if(!strings.containsKey(name)){
			strings.put(name, stringNumber);
			stringNumber++;
		}
	}

	/**
	 * Sets the current scope
	 * @param scope - name of the scope
	 */
	public void setScope(String scope){
		if(scopes.containsKey(scope)){
			currentScope = scope;
		} else {
			createScope(scope);
			currentScope = scope;
		}
	}

	/**
	 * overload to set it back to global easily.
	 */
	public void setScope(){
		currentScope = GLOBAL;		
	}

	/**
	 * Generates a key from the scope index and name index. This ensures that unique indentifiers can be
	 * declared in different scopes but not the same one.
	 * @param  scope - Scope name
	 * @param  name  - Identifier name
	 * @return       - Resulting key
	 */
	public String generateKey(String scope, String name) {
		enterName(name);
		String key = "" + scopes.get(scope) + strings.get(name);
		return key;
	}

	/**
	 * Generic record entering function used by the following functions
	 * @param  name      - Record name
	 * @param  attribute - Record attribute
	 * @param  key       - Record key
	 * @return           - Entered record
	 */
	public StRec enter(String name, String attribute, String key){
		if(records.containsKey(key)) {
			return records.get(key);
		}
		keys.add(key);
		StRec record = new StRec(name, attribute, key);
		records.put(key, record);
		return record;
	}

	/**
	 * Attempts to add a record into the table. returns null if the record exists in the current 
	 * scope already.
	 * @param  name        - Name of the identifier
	 * @param  attribute   - Attribute of the identifier
	 * @param  isParameter - If its a parameter (used for setting register info for function variables)
	 * @return             - The record entered, or null if it already exists.
	 */
	public StRec enterRecord(String name, String attribute, boolean isParameter) {			
		String key = generateKey(currentScope, name);
		if(records.containsKey(key)) {
			return null;
		}
		StRec record = enter(name, attribute, key);
		setFuncDetails(record, isParameter);
		return record;
	}

	/**
	 * Enters a constant into the CONSTANTS scope and returns the record. if the record exists,
	 * return the one thats there
	 * @param  name      - Name of the constant
	 * @param  attribute - Attribute of the constant
	 * @return           - The record entered or found
	 */
	public StRec enterConstant(String name, String attribute) {		
		String key = generateKey(CONSTANTS, name);		
		return enter(name, attribute, key);
	}

	/**
	 * Enters a type into the TYPES scope and returns the record. if the record exists,
	 * return the one thats there
	 * @param  name      - Name of the type
	 * @param  attribute - Attribute of the type
	 * @return           - The record entered or found
	 */
	public StRec enterType(String name, String attribute){
		String key = generateKey(TYPES, name);		
		return enter(name, attribute, key);
	}

	/**
	 * Tries to find the record in the current scope, if its not the then check the global scope,
	 * other wise return null.
	 * @param  name - Name of record
	 * @return      - Record if found otherwise null
	 */
	public StRec findRecord(String name) {
		String key = generateKey(currentScope, name);

		if(records.containsKey(key)) return records.get(key);

		key = generateKey(GLOBAL, name);
		if(records.containsKey(key)) return records.get(key);

		return null;
	}

	/**
	 * Tries to find a defined type record, if it can't, return null
	 * Note: This is only used to find structs and arrays, primitive types always use
	 * 		 the enterType() function so their type record is always found or created.
	 * @param  name - Name of type
	 * @return      - Record if found, otherwise null
	 */
	public StRec findType(String name) {
		String key = generateKey(TYPES, name);

		if(records.containsKey(key)){
			return records.get(key);
		}

		return null;
	}

	/**
	 * Sets the register information for function parameters and locals.
	 * @param  record      - Record to be checked
	 * @param  isParameter - Is it a parameter
	 * @return             - The record parameter, altered or not
	 */
	public StRec setFuncDetails(StRec record, boolean isParameter){
		// if the record is has the "func" attribute then reset the register info as the
		// coming records will be it's parameters and locals.
		if(record.getAttribute().equals("func")){
			funcOffset = 16;
			paramOffset = -8;
			if(funcStart == 0) funcStart = scopeNumber;

		} 
		// else if functions are starting to be declared and the current scope isn't one of the 
		// constant scopes then we know its a function parameter or local, so set it accordingly.
		else if(funcStart != 0 && scopes.get(currentScope) >= 4){
			if(isParameter){
				record.setRegInfo(2, paramOffset);
				paramOffset -= 8;
			} else {
				record.setRegInfo(2, funcOffset);
				funcOffset += 8;
			}
		}
		return record;
	}

	/**
	 * Finds the record of array variables. 
	 * @param  array    - Array record
	 * @param  typeName - Variable name
	 * @return          - Record of the variable declaration else null
	 */
	public StRec findArrayVarType(StRec array, String typeName) {

		// An array variable attribute is set to the key of the array definition
		String arrayTypeKey = array.getAttribute();
		if(!records.containsKey(arrayTypeKey)) return null;
		StRec arrayType = records.get(arrayTypeKey);

		// An array definition attribute is set to the key of the struct definition
		String strucTypeKey = arrayType.getAttribute();		
		if(!records.containsKey(strucTypeKey)) return null;		
		StRec strucType = records.get(strucTypeKey);

		// now we have the struct the array contains, check the struct contains the type definition
		String typeKey = generateKey(strucType.getName(), typeName);
		if(records.containsKey(typeKey)) {
			StRec type = new StRec(typeKey, records.get(typeKey).getAttribute(), typeKey);
			return type;
		} else {
			return null;		
		}
	}

	/**
	 * Returns a formatted string of all the contents of the records in the order they were added.
	 * If the keys are sorted then it will report the records in code order, ie. scope 0, 1, 2, 3... etc.
	 * @return - Report in string form
	 */
	public String report() {
		//Collections.sort(keys);
		String format = "| %-5s| %-11s| %-10s| ";
		String out = "\n" + String.format(format, "CODE", "NAME", "ATTRIBUTE") + "REGISTER INFO (Base Register, Offset)\n";		
		for(String entry : keys){
			StRec record = records.get(entry);
			out += String.format(format, entry, record.getName(),record.getAttribute());
			if(record.getBR() > -1) {
				out += record.getBR() + ", " + record.getOffset();
			}

			out += "\n";
		}
		return out;
	}
}