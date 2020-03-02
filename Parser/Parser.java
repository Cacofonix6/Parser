/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

import scanner.*;
import java.util.Queue;

/**
 *  Parser class - Contains the implementation of the grammar
 *  			 - Provides some semantic analysis mainly due to the symbol table 
 *  			   being able to handle scoping. (more semantic analysis to come in part 4)
 */
public class Parser {

	////////////////////// Variables //////////////////////
	private Queue<Token> tokens;		// Tokens to parse
	private Token currentToken = null;	// The current token being evaluated
	private int expectedTokenVal = -1;	// Used for error reporting
	private String errorReport = "";	// Report to sent to Output
	private SymTab symTab;				// Symbol Table
	private Output output;				// Output
	private boolean parseFail = false;	// set to true if an error occurs. 
										// allows parser to continue and find more errors

	// Primitive type names
	private final String VOID 	 = "VOID";
	private final String INTEGER = "INTEGER";
	private final String REAL 	 = "REAL";
	private final String BOOLEAN = "BOOLEAN";

	////////////////////// Helper Functions //////////////////////
	
	/**
	 * Starts the parser
	 * @param  _tokens - Tokens to parse
	 * @param  _output - Output object to send errors to
	 * @return         - Root node of the resulting syntax tree
	 */
	public TreeNode parse(final Queue<Token> _tokens, final Output _output){
		tokens = _tokens;
		output = _output;
		currentToken = tokens.poll();

		TreeNode root = program();

		return root;
	}

	/**
	 * Gets the next token and sets the current token to it. 
	 * Flags a parse failure if an undefined token is found (the scanner reports the actual error).
	 */
	private void consumeToken(){
		currentToken = tokens.poll();
		if(currentToken.value() == ReferenceTable.TUNDF){
			parseFail = true;
			consumeToken();
		}
	}

	/**
	 * Checks whether the current token is of a given given value
	 * @param  _tokenVal - expected value
	 * @return           - true if the values match, else false
	 */
	private boolean checkKeyword(int _tokenVal){
		if(currentToken.value() == _tokenVal){
			consumeToken();
			return true;
		}		
		expectedTokenVal = _tokenVal; // set expected value for error reporting
		return false;
	}
	
	/**
	 * Creates an error report for the output and flags the parse as a fail
	 * @param type - type of error, (Syntactic or Semantic)
	 */
	private void createErrorReport(String type){
		ErrorReport error = new ErrorReport(type, currentToken.getLn(), currentToken.getPos(), errorReport);
		output.addError(error);
		parseFail = true;
	}

	/**
	 * sets the error report and returns null so parsing halts
	 * @return - always null
	 */
	private TreeNode error(){		
		errorReport = "'" + ReferenceTable.TPRINT_WORD[expectedTokenVal] + "' Expected";	
		return null;
	}

	/**
	 * overload to specifically set the expected value (mainly used for lists that dont have commas)
	 * @param  expectedVal - expected token value
	 * @return             - always null
	 */
	private TreeNode error(int expectedVal){
		expectedTokenVal = expectedVal;
		return error();
	}

	/**
	 * overload to allow a custom error description
	 * @param  error - error description
	 * @return       - always null
	 */
	private TreeNode error(String error){	
		errorReport = error;
		return null;
	}

	/**
	 * Attempt to enter a record into the symbol table. Failure means the symbol already
	 * exists in the current scope (see SymTab) in which case it flags the parse as a fail but
	 * doesn't cause the parser to halt as it won't affect the syntax and more errors can be found.
	 * @param  name        - Name of the record
	 * @param  attribute   - Atribute of the record
	 * @param  isParameter - whether its a parameter record (for register info to be set)
	 * @return             - The record if found or null if not
	 */
	private StRec enterRecord(String name, String attribute, Boolean isParameter){	
		StRec record = symTab.enterRecord(name, attribute, isParameter);
		if(record == null) {			
			error(name + " has already been declared in this scope");
			createErrorReport("Semantic");
		}	

		consumeToken();
		return record;
	}

	/**
	 * Overload that allows exclusion of attributes
	 * @param  name        - Name of the record
	 * @param  isParameter - whether its a parameter record (for register info to be set)
	 * @return             - The record if found or null if not
	 */
	private StRec enterRecord(String name, Boolean isParameter){
		return enterRecord(name, "", isParameter);
	}

	/**
	 * Attempts to find a record in the symbol table. Failure means the symbol can't be found 
	 * in the current scope or the global scope and flags the parse as a fail. Like record creation,
	 * parsing will continue.
	 * @param  name   - Name of the record
	 * @param  isType - whether its a type record as they have their own scope
	 * @return        - The record if found or null if not
	 */
	private StRec findRecord(String name, Boolean isType){
		StRec record;

		if(isType) 
			record = symTab.findType(name);
		else 
			record = symTab.findRecord(name);		

		if(record == null) {	
			if(isType) 
				error("'" + name + "' is not a type");
			else 
				error("Cannot find symbol '" + name + "'");
			
			createErrorReport("Semantic");
		}	
		consumeToken();
		return record;
	}

	////////////////////// Grammar Functions //////////////////////
	/// There is a function for almost every non-terminal in the grammar document provided, 
	/// and they are defined in the same order as they appear in that document.

	////////////////////// <program> //////////////////////
	private TreeNode program(){
		if(!checkKeyword(ReferenceTable.TCD19)) return error(); 

		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		symTab = new SymTab(currentToken.getStr());
		consumeToken();

		TreeNode program = new TreeNode(ReferenceTable.NPROG, symTab.getProgramRecord());

		symTab.setScope(SymTab.GLOBAL);

		program = globals(program); 
		if(program == null) {
			createErrorReport("Syntactic");
			return null;
		}

		if(currentToken.value() == ReferenceTable.TFUNC){
			TreeNode funcs = funcs();
			if(funcs == null) {
				createErrorReport("Syntactic");
				return null;
			}
			program.setMiddle(funcs);
		}

		symTab.setScope(SymTab.MAIN);
		TreeNode mainbody = mainbody(); 
		if(mainbody == null) {
			createErrorReport("Syntactic");
			return null;
		}
		program.setRight(mainbody);

		if(currentToken.value() != ReferenceTable.TEOF){
			error("Illegal input after program end");
			createErrorReport("Semantic");
			return null;
		}

		if(parseFail || currentToken == null)
			return null;
		else
			return program;
	}
	////////////////////// <globals> //////////////////////
	private TreeNode globals(TreeNode program){	
		TreeNode globals = new TreeNode(ReferenceTable.NGLOB);

		if(checkKeyword(ReferenceTable.TCONS)){
			TreeNode consts = initlist();
			if(consts == null) return null;
			globals.setLeft(consts);
		}

		if(checkKeyword(ReferenceTable.TTYPS)){
			symTab.setScope(SymTab.TYPES);
			TreeNode types = typelist();
			if(types == null) return null;
			globals.setMiddle(types);
			symTab.setScope();
		}

		if(checkKeyword(ReferenceTable.TARRS)){
			TreeNode arrays = arrdecls();
			if(arrays == null) return null;
			globals.setRight(arrays);
		}

		if(!globals.hasChildren()) return program;

		program.setLeft(globals);

		return program;		
	}
	////////////////////// <initlist> //////////////////////
	private TreeNode initlist(){
		TreeNode init = init();
		if(init == null) return null;

		TreeNode initlist = initlisttail(init);
		return initlist;
	}
	////////////////////// <initlisttail> //////////////////////
	private TreeNode initlisttail(TreeNode init){
		int val = currentToken.value();
		if(val == ReferenceTable.TTYPS || val == ReferenceTable.TARRS || val == ReferenceTable.TFUNC || val == ReferenceTable.TMAIN){
			return init;
		}
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode initlist = initlist();
			if(initlist == null) return null;
			return new TreeNode(ReferenceTable.NINIT, init, initlist);
		}
		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <init> //////////////////////
	private TreeNode init(){	
		if(currentToken.value() != ReferenceTable.TIDEN) return error();		
		StRec record = enterRecord(currentToken.getStr(), "undef", false);

		if(!checkKeyword(ReferenceTable.TEQUL)) return error();

		TreeNode exp = expr();
		if(exp == null) return error("Illegal expression");
		
		TreeNode init = new TreeNode(ReferenceTable.NINIT, record);
		init.setLeft(exp); 
		return init;
	}	
	////////////////////// <funcs> //////////////////////
	private TreeNode funcs(){		
		TreeNode func = func();
		if(func == null) return null;

		if(currentToken.value() == ReferenceTable.TFUNC){
			TreeNode funcs = funcs();
			if(funcs == null) return null;
			TreeNode funclist = new TreeNode(ReferenceTable.NFUNCS, func, funcs);
			return funclist;
		}
		
		return func;			
	}	
	////////////////////// <mainbody> //////////////////////
	private TreeNode mainbody(){		
		if(!checkKeyword(ReferenceTable.TMAIN)) return error();

		TreeNode slist = slist();
		if(slist == null) return null;

		if(!checkKeyword(ReferenceTable.TBEGN)) return error();

		TreeNode stats = stats();
		if(stats == null) return null;


		if(!checkKeyword(ReferenceTable.TEND)) return error();
		if(!checkKeyword(ReferenceTable.TCD19)) return error();

		if(currentToken.value() != ReferenceTable.TIDEN) return error();

		if(!currentToken.getStr().equals(symTab.getProgramRecord().getName())) 
			return error("Program name not matched"); 
		consumeToken();
		TreeNode mainbody = new TreeNode(ReferenceTable.NMAIN, slist, stats);

		return mainbody;
	}
	////////////////////// <slist> //////////////////////
	private TreeNode slist(){		
		TreeNode sdecl = sdecl();
		if(sdecl == null) return null;	

		TreeNode slist = slisttail(sdecl);
		return slist;
	}
	////////////////////// <slisttail> //////////////////////
	private TreeNode slisttail(TreeNode sdecl){		
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode slist =  slist();
			if(slist == null) return null;
			return new TreeNode(ReferenceTable.NSDLST, sdecl, slist);
		}
		return sdecl;
	}
	////////////////////// <typelist> //////////////////////
	private TreeNode typelist(){		
		TreeNode type = type();
		if(type == null) return null;	

		TreeNode typelist = typelisttail(type);
		return typelist;
	}
	////////////////////// <typelisttail> //////////////////////
	private TreeNode typelisttail(TreeNode type){	
		if(currentToken.value() != ReferenceTable.TIDEN) return type;
		TreeNode typelist = typelist();
		if(typelist == null) return null;	
		return new TreeNode(ReferenceTable.NTYPEL, type, typelist);
	}	
	////////////////////// <type> //////////////////////
	private TreeNode type(){		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = enterRecord(currentToken.getStr(), "type", false);

		if(!checkKeyword(ReferenceTable.TIS)) return error();

		return typedecide(symbol);
	}
	////////////////////// <typedecide> //////////////////////
	private TreeNode typedecide(StRec symbol){	

		if(checkKeyword(ReferenceTable.TARAY)){
			if(!checkKeyword(ReferenceTable.TLBRK)) return error();

			TreeNode expr = expr();
			if(expr == null) return null;		

			if(!checkKeyword(ReferenceTable.TRBRK)) return error();		
			if(!checkKeyword(ReferenceTable.TOF)) return error();

			if(currentToken.value() != ReferenceTable.TIDEN) return error();
			StRec type = findRecord(currentToken.getStr(), true);
			
			if(type != null) symbol.setAttribute(type.getCode());	

			TreeNode typedecide = new TreeNode(ReferenceTable.NATYPE, symbol);
			typedecide.setLeft(expr);
			typedecide.setType(type);
			return typedecide;
		}

		symTab.setScope(symbol.getName());
		TreeNode fields = fields();
		if(fields == null) return null;
		symTab.setScope(SymTab.TYPES);

		if(!checkKeyword(ReferenceTable.TEND)) return error();	

		TreeNode typedecide = new TreeNode(ReferenceTable.NRTYPE, symbol);
		typedecide.setLeft(fields);		

		return typedecide;
	}
	////////////////////// <fields> //////////////////////
	private TreeNode fields(){		
		TreeNode sdecl = sdecl();
		if(sdecl == null) return null;	

		TreeNode fields = fieldstail(sdecl);

		return fields;
	}
	////////////////////// <fieldstail> //////////////////////
	private TreeNode fieldstail(TreeNode sdecl){		
		if(checkKeyword(ReferenceTable.TCOMA)) {
			return new TreeNode(ReferenceTable.NFLIST, sdecl, fields());
		}
		return sdecl;
	}
	////////////////////// <sdecl> //////////////////////
	private TreeNode sdecl(){		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = enterRecord(currentToken.getStr(), false);

		if(!checkKeyword(ReferenceTable.TCOLN)) return error();

		StRec type = stype();
		if(type == null) return null;

		symbol.setAttribute(type.getName());

		
		TreeNode sdecl = new TreeNode(ReferenceTable.NSDECL, symbol);
		sdecl.setType(type);

		return sdecl;
	}
	////////////////////// <arrdecls> //////////////////////
	private TreeNode arrdecls(){		
		TreeNode arrdecl = arrdecl();
		if(arrdecl == null) return null;	

		TreeNode arrdecls = arrdeclstail(arrdecl);

		return arrdecls;
	}
	////////////////////// <arrdeclstail> //////////////////////
	private TreeNode arrdeclstail(TreeNode arrdecl){	
		int val = currentToken.value();
		if(val == ReferenceTable.TTYPS || val == ReferenceTable.TARRS || val == ReferenceTable.TFUNC || val == ReferenceTable.TMAIN){
			return arrdecl;
		}	
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode arrdecls = arrdecls();
			if(arrdecls == null) return null;
			return new TreeNode(ReferenceTable.NALIST, arrdecl, arrdecls);
		}
		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <arrdecl> //////////////////////
	private TreeNode arrdecl(){		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = enterRecord(currentToken.getStr(), false);

		if(!checkKeyword(ReferenceTable.TCOLN)) return error();

		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec type = findRecord(currentToken.getStr(), true);

		if(type != null) symbol.setAttribute(type.getCode());
		
		TreeNode arrdecl = new TreeNode(ReferenceTable.NARRD, symbol);
		arrdecl.setType(type);

		return arrdecl;
	}
	////////////////////// <func> //////////////////////
	private TreeNode func(){		
		if(!checkKeyword(ReferenceTable.TFUNC)) return error();
		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = enterRecord(currentToken.getStr(), "func", false);
		
		TreeNode func = new TreeNode(ReferenceTable.NFUND, symbol);	

		symTab.setScope(symbol.getName());
		if(!checkKeyword(ReferenceTable.TLPAR)) return error();

		func = plist(func);
		if(func == null) return null;

		if(!checkKeyword(ReferenceTable.TRPAR)) return error();
		if(!checkKeyword(ReferenceTable.TCOLN)) return error();
		
		StRec type = rtype();
		if(type == null) return null;
		type.setAttribute("return");
		
		func = funcbody(func);
		if(func == null) return null;

		symTab.setScope();

		func.setType(type);

		return func;
	}
	////////////////////// <rtype> //////////////////////
	private StRec rtype(){	
		if(checkKeyword(ReferenceTable.TVOID)) return symTab.enterType(VOID, "");	
		return stype();
	}
	////////////////////// <plist> //////////////////////
	private TreeNode plist(TreeNode func){		
		int val = currentToken.value();
		
		if(val == ReferenceTable.TIDEN || val == ReferenceTable.TCNST)	{			
			TreeNode params = params();		
			if(params == null) return null;	
			func.setLeft(params);
		}
			
		return func;
	}
	////////////////////// <params> //////////////////////
	private TreeNode params(){		
		TreeNode param = param();
		if(param == null) return null;

		TreeNode params = paramstail(param);

		return params;
	}
	////////////////////// <paramstail> //////////////////////
	private TreeNode paramstail(TreeNode param){		
		if(currentToken.value() == ReferenceTable.TRPAR )	{	
			return param;
		}
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode params = params();
			if(params == null) return null;
			return new TreeNode(ReferenceTable.NPLIST, param, params);
		}

		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <param> //////////////////////
	private TreeNode param(){		
		TreeNode param = null;

		if(checkKeyword(ReferenceTable.TCNST)) {
			param = new TreeNode(ReferenceTable.NARRC);			
		}

		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = enterRecord(currentToken.getStr(), true);

		if(!checkKeyword(ReferenceTable.TCOLN)) return error();

		if(currentToken.value() == ReferenceTable.TIDEN){
			if(param == null)
				param = new TreeNode(ReferenceTable.NARRP);

			StRec type = findRecord(currentToken.getStr(), true);
			if(type != null) symbol.setAttribute(type.getCode());
			
			param.setSymbol(symbol);
			param.setType(type);
			return param;
		}

		StRec type = stype();
		if(type != null){
			if(param == null)
				 param = new TreeNode(ReferenceTable.NSIMP);

			symbol.setAttribute(type.getName());
			
			param.setSymbol(symbol);
			param.setType(type);
			return param;
		}
		return error("Incomplete parameter.");
	}	
	////////////////////// <funcbody> //////////////////////
	private TreeNode funcbody(TreeNode func){		
		
		func = locals(func);
		if(func == null) return null;

		if(!checkKeyword(ReferenceTable.TBEGN)) return error();

		TreeNode stats = stats();
		if(stats == null) return null;
		func.setRight(stats);

		if(!checkKeyword(ReferenceTable.TEND)) return error();		

		return func;
	}
	////////////////////// <locals> //////////////////////
	private TreeNode locals(TreeNode func){		
		if(currentToken.value() == ReferenceTable.TBEGN){
			return func;
		}
		TreeNode dlist = dlist();		
		if(dlist == null) return null;

		func.setMiddle(dlist);	

		return func;
	}
	////////////////////// <dlist> //////////////////////
	private TreeNode dlist(){		
		TreeNode decl = decl();
		if(decl == null) return null;

		TreeNode dlist = dlisttail(decl);

		return dlist;
	}
	////////////////////// <dlisttail> //////////////////////
	private TreeNode dlisttail(TreeNode decl){		
		if(currentToken.value() == ReferenceTable.TBEGN )	{	
			return decl;
		}
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode dlist = dlist();
			if(dlist == null) return null;
			return new TreeNode(ReferenceTable.NDLIST, decl, dlist);
		}
		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <decl> //////////////////////
	private TreeNode decl(){		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = enterRecord(currentToken.getStr(), false);
		if(!checkKeyword(ReferenceTable.TCOLN)) return error();

		return decldecide(symbol);
	}
	////////////////////// <decldecide> //////////////////////
	private TreeNode decldecide(StRec symbol){	
		if(currentToken.value() == ReferenceTable.TIDEN){

			StRec type = findRecord(currentToken.getStr(), true);

			if(type != null) symbol.setAttribute(type.getCode());

			TreeNode decl = new TreeNode(ReferenceTable.NARRD, symbol);
			decl.setType(type);
			return decl;
		}

		StRec type = stype();
		if(type != null){
			symbol.setAttribute(type.getName());
			
			TreeNode decl = new TreeNode(ReferenceTable.NSDECL, symbol);
			decl.setType(type);
			return decl;
		}
		return error("Illegal Declaration");
	}
	////////////////////// <stype> //////////////////////
	private StRec stype(){		
		if(checkKeyword(ReferenceTable.TINTG)) return symTab.enterType(INTEGER, "");
		if(checkKeyword(ReferenceTable.TREAL)) return symTab.enterType(REAL, "");
		if(checkKeyword(ReferenceTable.TBOOL)) return symTab.enterType(BOOLEAN, "");
		error();
		return null;
	}
	////////////////////// <stats> //////////////////////
	private TreeNode stats(){	
		if(currentToken.value() == ReferenceTable.TFOR || currentToken.value() == ReferenceTable.TIFTH){	
			TreeNode stat = strstat();
			if(stat == null) return null;
			return statstail(stat);
		}

		TreeNode stat = stat();
		if(stat == null) return null;

		if(!checkKeyword(ReferenceTable.TSEMI)) return error();

		return statstail(stat);
	}
	////////////////////// <statstail> //////////////////////
	private TreeNode statstail(TreeNode stat){		
		if( currentToken.value() == ReferenceTable.TEND || 
			currentToken.value() == ReferenceTable.TUNTL || 
			currentToken.value() == ReferenceTable.TELSE)
			return stat;

		if( currentToken.value() == ReferenceTable.TFUNC || 
			currentToken.value() == ReferenceTable.TMAIN)
			return error(ReferenceTable.TEND);

		TreeNode stats = stats();
		if(stats == null) return null;

		return new TreeNode(ReferenceTable.NSTATS, stat, stats);
	}
	////////////////////// <strstat> //////////////////////
	private TreeNode strstat(){	
		if(checkKeyword(ReferenceTable.TFOR)) {
			TreeNode forstat = forstat();
			if(forstat == null) return null;
			return forstat;
		}

		if(checkKeyword(ReferenceTable.TIFTH)){
			TreeNode ifstat = ifstat();
			if(ifstat == null) return null;
			return ifstat;
		}

		return error();
	}
	////////////////////// <stat> //////////////////////
	private TreeNode stat(){			

		if(checkKeyword(ReferenceTable.TREPT)){			
			TreeNode repstat = repstat();
			if(repstat == null) return null;
			return repstat;
		}

		if(checkKeyword(ReferenceTable.TRETN)){	
			TreeNode returnstat = returnstat();
			if(returnstat == null) return null;
			return returnstat;
		}

		if( currentToken.value() == ReferenceTable.TINPT ||
			currentToken.value() == ReferenceTable.TPRIN ||
			currentToken.value() == ReferenceTable.TPRLN){
			TreeNode iostat = iostat();
			if(iostat == null) return null;
			return iostat;
		}
		
		if(currentToken.value() == ReferenceTable.TIDEN){
			TreeNode idstat = idstat();
			if(idstat == null) return null;
			return idstat;
		}
		

		return error("Not a Statement");
	}
	////////////////////// <idstat> //////////////////////
	private TreeNode idstat(){

		if(tokens.peek().value() == ReferenceTable.TLPAR) return callstat();
	
		return asgnstat();
	}
	////////////////////// <forstat> //////////////////////
	private TreeNode forstat(){		
		TreeNode forstat = new TreeNode(ReferenceTable.NFOR);
		if(!checkKeyword(ReferenceTable.TLPAR)) return error();

		forstat = asgnlist(forstat);
		if(forstat == null) return null;

		if(!checkKeyword(ReferenceTable.TSEMI)) return error();

		TreeNode bool = bool();
		if(bool == null) return null;
		forstat.setMiddle(bool);

		if(!checkKeyword(ReferenceTable.TRPAR)) return error();

		TreeNode stats = stats();
		if(stats == null) return null;		
		forstat.setRight(stats);

		if(!checkKeyword(ReferenceTable.TEND)) return error();

		return forstat;
	}
	////////////////////// <repstat> //////////////////////
	private TreeNode repstat(){		
		TreeNode repstat = new TreeNode(ReferenceTable.NREPT);	
		if(!checkKeyword(ReferenceTable.TLPAR)) return error();
		
		repstat = asgnlist(repstat);
		if(repstat == null) return null;

		if(!checkKeyword(ReferenceTable.TRPAR)) return error();

		TreeNode stats = stats();
		if(stats == null) return null;
		repstat.setMiddle(stats);

		if(!checkKeyword(ReferenceTable.TUNTL)) return error();

		TreeNode bool = bool();
		if(bool == null) return null;		
		repstat.setRight(bool);

		return repstat;
	}
	////////////////////// <asgnlist> //////////////////////
	private TreeNode asgnlist(TreeNode stat){	
		if(currentToken.value() == ReferenceTable.TSEMI || currentToken.value() == ReferenceTable.TRPAR)
			return stat;
		TreeNode alist = alist();
		if(alist == null) return null;
		
		return alist;
	}
	////////////////////// <alist> //////////////////////
	private TreeNode alist(){		
		TreeNode asgnstat = asgnstat();
		if(asgnstat == null) return null;

		TreeNode alist = alisttail(asgnstat);
		return alist;
	}	
	////////////////////// <alisttail> //////////////////////
	private TreeNode alisttail(TreeNode asgnstat){	
		if(currentToken.value() == ReferenceTable.TSEMI || currentToken.value() == ReferenceTable.TRPAR)
			return asgnstat;	
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode alist = alist();
			if(alist == null) return null;
			return new TreeNode(ReferenceTable.NASGNS, asgnstat, alist);
		}
		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <ifstat> //////////////////////
	private TreeNode ifstat(){		
		if(!checkKeyword(ReferenceTable.TLPAR)) return error();

		TreeNode bool = bool();
		if(bool == null) return null;

		if(!checkKeyword(ReferenceTable.TRPAR)) return error();

		TreeNode stats = stats();
		if(stats == null) return null;

		TreeNode ifstat = ifstattail(bool, stats);
		return ifstat;
	}
	////////////////////// <ifstattail> //////////////////////
	private TreeNode ifstattail(TreeNode bool, TreeNode stats){		
		if(checkKeyword(ReferenceTable.TELSE)) {
			TreeNode elseStats = stats();
			if(elseStats == null) return null;
			if(!checkKeyword(ReferenceTable.TEND)) return error();
			return new TreeNode(ReferenceTable.NIFTE, bool, stats, elseStats);
		}
		if(checkKeyword(ReferenceTable.TEND)) {
			return new TreeNode(ReferenceTable.NIFTH, bool, stats);
		}
		return error();
	}
	////////////////////// <asgnstat> //////////////////////
	private TreeNode asgnstat(){	
		TreeNode variable = var();
		if(variable == null) return null;

		int nodeID = asgnop();
		if(nodeID == -1) return error("Not a Statement");

		TreeNode bool = bool();
		if(bool == null) return error("Illegal assignment");

		return new TreeNode(nodeID, variable, bool);
	}
	////////////////////// <asgnop> //////////////////////
	private int asgnop(){
		if(checkKeyword(ReferenceTable.TEQUL)) return ReferenceTable.NASGN;
		if(checkKeyword(ReferenceTable.TPLEQ)) return ReferenceTable.NPLEQ;
		if(checkKeyword(ReferenceTable.TMNEQ)) return ReferenceTable.NMNEQ;
		if(checkKeyword(ReferenceTable.TSTEQ)) return ReferenceTable.NSTEQ;
		if(checkKeyword(ReferenceTable.TDVEQ)) return ReferenceTable.NDVEQ;
		return -1;
	}
	////////////////////// <iostat> //////////////////////
	private TreeNode iostat(){
		if(checkKeyword(ReferenceTable.TINPT)){
			TreeNode vlist = vlist();
			if(vlist == null) return null;
			TreeNode input = new TreeNode(ReferenceTable.NINPUT);			
			input.setLeft(vlist);
			return input;
		}

		if(checkKeyword(ReferenceTable.TPRIN)){
			TreeNode prlist = prlist();
			if(prlist == null) return null;
			TreeNode print = new TreeNode(ReferenceTable.NPRINT);
			print.setLeft(prlist);
			return print;
		}

		if(checkKeyword(ReferenceTable.TPRLN)){
			TreeNode prlist = prlist();
			if(prlist == null) return null;
			TreeNode printline = new TreeNode(ReferenceTable.NPRLN);
			printline.setLeft(prlist);
			return printline;
		}
		return error();
	}	
	////////////////////// <callstat> //////////////////////
	private TreeNode callstat(){		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = findRecord(currentToken.getStr(), false);


		if(!checkKeyword(ReferenceTable.TLPAR)) return error();

		TreeNode callstat = new TreeNode(ReferenceTable.NCALL, symbol);
		callstat = callstattail(callstat);
		if(callstat == null) return null;
		if(!checkKeyword(ReferenceTable.TRPAR)) return error();
		return callstat;
	}	
	////////////////////// <callstattail> //////////////////////
	private TreeNode callstattail(TreeNode callstat){		
		if(currentToken.value() == ReferenceTable.TRPAR)
			return callstat;

		TreeNode elist = elist();
		if(elist == null) return null;
		callstat.setLeft(elist);
		return callstat;
	}	
	////////////////////// <returnstat> //////////////////////
	private TreeNode returnstat(){		

		TreeNode returnstat = new TreeNode(ReferenceTable.NRETN);
		
		return returnstattail(returnstat);
	}
	////////////////////// <returnstattail> //////////////////////
	private TreeNode returnstattail(TreeNode returnstat){		
		if(currentToken.value() == ReferenceTable.TSEMI)
			return returnstat;
		TreeNode expr = expr();
		if(expr == null) return null;
		returnstat.setLeft(expr);
		return returnstat;
	}	
	////////////////////// <vlist> //////////////////////
	private TreeNode vlist(){		
		TreeNode variable = var();
		if(variable == null) return null;

		TreeNode v = vlisttail(variable);
		return v;
	}	
	////////////////////// <vlisttail> //////////////////////
	private TreeNode vlisttail(TreeNode variable){	

		if(currentToken.value() == ReferenceTable.TSEMI){
			return variable;	
		}

		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode vlist = vlist();
			if(vlist == null) return null;
			return new TreeNode(ReferenceTable.NVLIST, variable, vlist);	
		}

		return error(ReferenceTable.TCOMA);
	}	
	////////////////////// <var> //////////////////////
	private TreeNode var(){	
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = findRecord(currentToken.getStr(), false);

		return vartail(symbol);
	}	
	////////////////////// <vartail> //////////////////////
	private TreeNode vartail(StRec symbol){	
		if(checkKeyword(ReferenceTable.TLBRK)) {
			TreeNode expr = expr();
			if(expr == null) return null;	

			if(!checkKeyword(ReferenceTable.TRBRK)) return error();
			if(!checkKeyword(ReferenceTable.TDOT)) return error();

			if(currentToken.value() != ReferenceTable.TIDEN) return error();			

			StRec type = null;
			if(symbol != null){
				type = symTab.findArrayVarType(symbol, currentToken.getStr());
				if(type == null) {
					error("Cannot find array variable '" + currentToken.getStr() + "'");
					createErrorReport("Semantic");
				}
			}			
			consumeToken();
			
			TreeNode variable = new TreeNode(ReferenceTable.NARRV, symbol);

			variable.setLeft(expr);
			variable.setType(type);
			return variable;
		}		

		TreeNode variable = new TreeNode(ReferenceTable.NSIMV, symbol);
		
		return variable;
	}	
	////////////////////// <elist> //////////////////////
	private TreeNode elist(){		
		TreeNode bool = bool();
		if(bool == null) return null;

		return elisttail(bool);
	}
	////////////////////// <elisttail> //////////////////////
	private TreeNode elisttail(TreeNode bool){	
		if(currentToken.value() == ReferenceTable.TRPAR)
			return bool;	
		if(checkKeyword(ReferenceTable.TCOMA)){
			TreeNode elist = elist();
			if(elist == null) return null;
			return new TreeNode(ReferenceTable.NEXPL, bool, elist);
		}
		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <bool> //////////////////////
	private TreeNode bool(){		
		TreeNode rel = rel();
		if(rel == null) return null;

		return booltail(rel);
	}
	////////////////////// <booltail> //////////////////////
	private TreeNode booltail(TreeNode rel){
		int nodeID = logop();
		if(nodeID == -1) return rel;

		TreeNode secondRel = rel();
		if(secondRel == null) return null;

		return new TreeNode(nodeID, rel, booltail(secondRel));
	}
	////////////////////// <rel> //////////////////////
	private TreeNode rel(){
		if(checkKeyword(ReferenceTable.TNOT)) {
			TreeNode lexpr = expr();
			if(lexpr == null) return null;

			int nodeID = relop();
			if(nodeID == -1) return error();

			TreeNode rexpr = expr();
			if(rexpr == null) return null;

			TreeNode relop = new TreeNode(nodeID, lexpr, rexpr);

			TreeNode not = new TreeNode(ReferenceTable.NNOT);
			not.setLeft(relop);
			return not;
		}
		TreeNode expr = expr();
		if(expr == null) return null;

		return reltail(expr);
	}
	////////////////////// <reltail> //////////////////////
	private TreeNode reltail(TreeNode lexpr){		
		int nodeID = relop();
		if(nodeID == -1) return lexpr;
		TreeNode rexpr = expr();
		if(rexpr == null) return null;

		return new TreeNode(nodeID, lexpr, rexpr);
	}
	////////////////////// <logop> //////////////////////
	private int logop(){
		if(checkKeyword(ReferenceTable.TAND)) return ReferenceTable.NAND;
		if(checkKeyword(ReferenceTable.TOR )) return ReferenceTable.NOR;
		if(checkKeyword(ReferenceTable.TXOR)) return ReferenceTable.NXOR;
		return -1;
	}
	////////////////////// <relop> //////////////////////
	private int relop(){		
		if(checkKeyword(ReferenceTable.TEQEQ)) return ReferenceTable.NEQL;
		if(checkKeyword(ReferenceTable.TNEQL)) return ReferenceTable.NNEQ;
		if(checkKeyword(ReferenceTable.TGRTR)) return ReferenceTable.NGRT;
		if(checkKeyword(ReferenceTable.TLEQL)) return ReferenceTable.NLEQ;
		if(checkKeyword(ReferenceTable.TLESS)) return ReferenceTable.NLSS;
		if(checkKeyword(ReferenceTable.TGEQL)) return ReferenceTable.NGEQ;
		return -1;
	}	
	////////////////////// <expr> //////////////////////
	private TreeNode expr(){		
		TreeNode fact = fact();
		if(fact == null) return null;
		return exprtail(fact);
	}	
	////////////////////// <exprtail> //////////////////////
	private TreeNode exprtail(TreeNode fact){		
		if(checkKeyword(ReferenceTable.TPLUS)) {
			TreeNode rfact = fact();
			if(rfact == null) return null;

			return new TreeNode(ReferenceTable.NADD, fact, exprtail(rfact));
		}

		if(checkKeyword(ReferenceTable.TMINS)) {
			TreeNode rfact = fact();
			if(rfact == null) return null;

			return new TreeNode(ReferenceTable.NSUB, fact, exprtail(rfact));
		}
		
		return fact;
	}	
	////////////////////// <fact> //////////////////////
	private TreeNode fact(){		
		TreeNode term = term();
		if(term == null) return null;
		return facttail(term);
	}	
	////////////////////// <facttail> //////////////////////
	private TreeNode facttail(TreeNode term){		
		if(checkKeyword(ReferenceTable.TSTAR)) {
			TreeNode rterm = term();
			if(rterm == null) return null;

			return new TreeNode(ReferenceTable.NMUL, term, facttail(rterm));
		}

		if(checkKeyword(ReferenceTable.TDIVD)) {
			TreeNode rterm = term();
			if(rterm == null) return null;

			return new TreeNode(ReferenceTable.NDIV, term, facttail(rterm));
		}

		if(checkKeyword(ReferenceTable.TPERC)) {
			TreeNode rterm = term();
			if(rterm == null) return null;

			return new TreeNode(ReferenceTable.NMOD, term, facttail(rterm));
		}
		
		return term;
	}	
	////////////////////// <term> //////////////////////
	private TreeNode term(){		
		TreeNode exponent = exponent();
		if(exponent == null) return null;
		return termtail(exponent);
	}	
	////////////////////// <termtail> //////////////////////
	private TreeNode termtail(TreeNode exponent){		
		if(checkKeyword(ReferenceTable.TCART)) {
			TreeNode rexponent = term();
			if(rexponent == null) return null;

			return new TreeNode(ReferenceTable.NPOW, exponent, termtail(rexponent));
		}
		return exponent;
	}	
	////////////////////// <exponent> //////////////////////
	private TreeNode exponent(){	
		if(checkKeyword(ReferenceTable.TTRUE)) return new TreeNode(ReferenceTable.NTRUE);
		if(checkKeyword(ReferenceTable.TFALS)) return new TreeNode(ReferenceTable.NFALS);
		
		if(checkKeyword(ReferenceTable.TLPAR)) {
			TreeNode bool = bool();
			if(bool == null) return null;	
			if(!checkKeyword(ReferenceTable.TRPAR)) return error();
			return bool;
		}

		if(currentToken.value() == ReferenceTable.TIDEN){			
			if(tokens.peek().value() == ReferenceTable.TLPAR) return fncall();
			
			return var();
		}

		String sign = "";
		if(checkKeyword(ReferenceTable.TMINS)){
			sign = "-";
		}

		if(currentToken.value() == ReferenceTable.TILIT){
			StRec value = symTab.enterConstant(sign + currentToken.getStr(), INTEGER);
			consumeToken();
			return new TreeNode(ReferenceTable.NILIT, value);
		}

		if(currentToken.value() == ReferenceTable.TFLIT){
			StRec value = symTab.enterConstant(sign + currentToken.getStr(), REAL);
			consumeToken();
			return new TreeNode(ReferenceTable.NFLIT, value);
		}

		return error("Illegal expression");
	}
	////////////////////// <fncall> //////////////////////
	private TreeNode fncall(){		
		if(currentToken.value() != ReferenceTable.TIDEN) return error();
		StRec symbol = findRecord(currentToken.getStr(), false);
		if(!checkKeyword(ReferenceTable.TLPAR)) return error();

		TreeNode fncall = new TreeNode(ReferenceTable.NFCALL, symbol);
		fncall = fncalltail(fncall);

		if(!checkKeyword(ReferenceTable.TRPAR)) return error();

		return fncall;
	}
	////////////////////// <fncalltail> //////////////////////
	private TreeNode fncalltail(TreeNode fncall){	
		if(currentToken.value() == ReferenceTable.TRPAR)			
			return fncall;

		TreeNode elist = elist();
		if(elist == null) return null; 
		fncall.setLeft(elist);
		return fncall;
	}
	////////////////////// <prlist> //////////////////////
	private TreeNode prlist(){		
		TreeNode printitem = printitem();
		if(printitem == null) return null;

		return prlisttail(printitem);
	}
	////////////////////// <prlisttail> //////////////////////
	private TreeNode prlisttail(TreeNode printitem){	
		if(currentToken.value() == ReferenceTable.TSEMI)
			return printitem;	
		if(checkKeyword(ReferenceTable.TCOMA)) {
			TreeNode prlist = prlist();
			if(prlist == null) return null;
			return new TreeNode(ReferenceTable.NPRLST, printitem, prlist);
		}

		return error(ReferenceTable.TCOMA);
	}
	////////////////////// <printitem> //////////////////////
	private TreeNode printitem(){			
		if(currentToken.value() == ReferenceTable.TSTRG){
			StRec value = symTab.enterConstant(currentToken.getStr(), "string");
			consumeToken();
			return new TreeNode(ReferenceTable.NSTRG, value);
		}
		return expr();
	}
}