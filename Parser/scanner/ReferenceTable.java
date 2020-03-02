/////////////////
// Angus Walsh //
// 3268157     //
// COMP3290    //
/////////////////

package scanner;

public interface ReferenceTable {

	// TOKEN VALUES
	// ***********************
	public static final int
		
		TEOF  =  0,	  // Token value for end of file

		// The 30 keywords

		TCD19 =  1,	TCONS = 2,	TTYPS = 3,	TIS   = 4,	TARRS = 5,	TMAIN = 6,
		TBEGN =  7,	TEND  = 8,	TARAY = 9,	TOF   = 10,	TFUNC = 11,	TVOID = 12,
		TCNST = 13,	TINTG = 14,	TREAL = 15,	TBOOL = 16,	TFOR  = 17,	TREPT = 18,
		TUNTL = 19,	TIFTH = 20,	TELSE = 21,	TINPT = 22,	TPRIN = 23,	TPRLN = 24,
		TRETN = 25,	TNOT  = 26,	TAND  = 27,	TOR   = 28,	TXOR  = 29,	TTRUE = 30,
		TFALS = 31,

		// the operators and delimiters
		TCOMA = 32,	TLBRK = 33,	TRBRK = 34,	TLPAR = 35,	TRPAR = 36, TEQUL = 37,	
		TPLUS = 38,	TMINS = 39,	TSTAR = 40,	TDIVD = 41,	TPERC = 42,	TCART = 43,	
		TLESS = 44,	TGRTR = 45,	TCOLN = 46,	TLEQL = 47,	TGEQL = 48, TNEQL = 49,	
		TEQEQ = 50,	TPLEQ = 51,	TMNEQ = 52,	TSTEQ = 53,	TDVEQ = 54,	TPCEQ = 55,	
		TSEMI = 56,	TDOT  = 57,

		// the tokens which need tuple values

		TIDEN = 58,	TILIT = 59,	TFLIT = 60,	TSTRG = 61,	TUNDF = 62;

	public static final String TPRINT[] = {  	//  TPRINT[tokenValue] will produce the associated String
												//  e.g. TPRINT[TMAIN] will be the String "TMAIN ".
		"TEOF  ",
		"TCD19 ",	"TCONS ",	"TTYPS ",	"TIS   ",	"TARRS ",	"TMAIN ",
		"TBEGN ",	"TEND  ",	"TARAY ",	"TOF   ",	"TFUNC ",	"TVOID ",
		"TCNST ",	"TINTG ",	"TREAL ",	"TBOOL ",	"TFOR  ",	"TREPT ",
		"TUNTL ",	"TIFTH ",	"TELSE ",	"TINPT ",	"TPRIN ",	"TPRLN ",
		"TRETN ",	"TNOT  ",	"TAND  ",	"TOR   ",	"TXOR  ",	"TTRUE ",
		"TFALS ",	"TCOMA ",	"TLBRK ",	"TRBRK ",	"TLPAR ",	"TRPAR ",
		"TEQUL ",	"TPLUS ",	"TMINS ",	"TSTAR ",	"TDIVD ",	"TPERC ",
		"TCART ",	"TLESS ",	"TGRTR ",	"TCOLN ",	"TLEQL ",	"TGEQL ",
		"TNEQL ",	"TEQEQ ",	"TPLEQ ",	"TMNEQ ",	"TSTEQ ",	"TDVEQ ",
		"TPCEQ ",	"TSEMI ",	"TDOT  ",

		"TIDEN ",	"TILIT ",	"TFLIT ",	"TSTRG ",	"TUNDF "
	};

	public static final String TPRINT_WORD[] = {  	
		"TEOF  ",
		"CD19"  ,	"constants",	"types",	"is",		"arrays",	"main",
		"begin",	"end",			"array",	"of",		"function",	"void",
		"const",	"integer",		"real",		"boolean",	"for",		"repeat",
		"until",	"if",			"else",		"input ",	"print",	"printline",
		"return",	"not",			"and",		"or",		"xor",		"true",
		"false",	",",			"[",		"]",		"(",		")",
		"=",		"+",			"-",		"*",		"/",		"%",
		"^",		"<",			">",		":",		"<=",		">=",
		"!=",		"==",			"+=",		"-=",		"*=",		"/=",
		"%=",		";",			".",

		"identifier",	"integer literal",	"real literal",	"string",	"undefined"
	};

	public static int checkString(String _s) {
		int out = checkKeywords(_s);
		if(out >= 0) return out; 
		out = checkSymbol(_s);
		if(out != TUNDF) return out;

		if(_s.equals("<id>") || _s.equals("<structid>") || _s.equals("<typeid>")) return TIDEN;
		if(_s.equals("<intlit>")) return TILIT;
		if(_s.equals("<reallit>")) return TFLIT;
		if(_s.equals("<string>")) return TSTRG;
		return -1;
	}

	public static int checkKeywords(String s) {	// Takes a lexeme recognised as an ID
											// Returns the correct keyword Token number
		s = s.toLowerCase(); // change to lower case before checking
		if ( s.equals("cd19")      )	return TCD19;
		if ( s.equals("constants") )	return TCONS;
		if ( s.equals("types")     )	return TTYPS;
		if ( s.equals("is")        )	return TIS;
		if ( s.equals("arrays")    )	return TARRS;

		if ( s.equals("main")      )	return TMAIN;
		if ( s.equals("begin")     )	return TBEGN;
		if ( s.equals("end")       )	return TEND;
		if ( s.equals("array")     )	return TARAY;
		if ( s.equals("of")        )	return TOF;
		if ( s.equals("function")  )	return TFUNC;
		if ( s.equals("void")      )	return TVOID;
		if ( s.equals("const")     )	return TCNST;

		if ( s.equals("integer")   )	return TINTG;
		if ( s.equals("real")      )	return TREAL;
		if ( s.equals("boolean")   )	return TBOOL;

		if ( s.equals("for")       )	return TFOR;
		if ( s.equals("repeat")    )	return TREPT;
		if ( s.equals("until")     )	return TUNTL;
		if ( s.equals("if")        )	return TIFTH;
		if ( s.equals("else")      )	return TELSE;

		if ( s.equals("input")     )	return TINPT;
		if ( s.equals("print")     )	return TPRIN;
		if ( s.equals("printline") )	return TPRLN;
		if ( s.equals("return")    )	return TRETN;

		if ( s.equals("and")       )	return TAND;
		if ( s.equals("or")        )	return TOR;
		if ( s.equals("xor")       )	return TXOR;
		if ( s.equals("not")       )	return TNOT;
		if ( s.equals("true")      )	return TTRUE;
		if ( s.equals("false")     )	return TFALS;

		return -1;		// not a Keyword
	}

	public static int checkSymbol(final String _symbol){

		// single character SYMBLs
		if(_symbol.equals(",")) { return TCOMA; }
		if(_symbol.equals("[")) { return TLBRK; }
		if(_symbol.equals("]")) { return TRBRK; }
		if(_symbol.equals("(")) { return TLPAR; }
		if(_symbol.equals(")")) { return TRPAR; }
		if(_symbol.equals("=")) { return TEQUL; }
		if(_symbol.equals("+")) { return TPLUS; }
		if(_symbol.equals("-")) { return TMINS; }
		if(_symbol.equals("*")) { return TSTAR; }
		if(_symbol.equals("/")) { return TDIVD; }
		if(_symbol.equals("%")) { return TPERC; }
		if(_symbol.equals("^")) { return TCART; }
		if(_symbol.equals("<")) { return TLESS; }
		if(_symbol.equals(">")) { return TGRTR; }
		if(_symbol.equals(":")) { return TCOLN; }
		if(_symbol.equals(";")) { return TSEMI; }
		if(_symbol.equals(".")) { return TDOT ; }
		if(_symbol.equals("\"")){ return TSTRG; }

		// double character SYMBLs
		if(_symbol.equals("<=")) { return TLEQL; }
		if(_symbol.equals(">=")) { return TGEQL; }
		if(_symbol.equals("!=")) { return TNEQL; }
		if(_symbol.equals("==")) { return TEQEQ; }
		if(_symbol.equals("+=")) { return TPLEQ; }
		if(_symbol.equals("-=")) { return TMNEQ; }
		if(_symbol.equals("*=")) { return TSTEQ; }
		if(_symbol.equals("/=")) { return TDVEQ; }
		if(_symbol.equals("%=")) { return TPCEQ; }
		
		return TUNDF;
	}
	

	// SYNTAX TREE NODE VALUES
	// ***********************
	public static final int NUNDEF = 0,
				NPROG = 1,		NGLOB = 2,		NILIST = 3,		NINIT = 4,		NFUNCS = 5,
				NMAIN = 6,		NSDLST = 7,		NTYPEL = 8,		NRTYPE = 9,		NATYPE = 10,
				NFLIST = 11,	NSDECL = 12,	NALIST = 13,	NARRD = 14,		NFUND = 15,
				NPLIST = 16,	NSIMP = 17,		NARRP = 18,		NARRC = 19,		NDLIST = 20,
				NSTATS = 21,	NFOR = 22,		NREPT = 23,		NASGNS = 24,	NIFTH = 25,
				NIFTE = 26,		NASGN = 27,		NPLEQ = 28,		NMNEQ = 29,		NSTEQ = 30,
				NDVEQ = 31,		NINPUT = 32,	NPRINT = 33,	NPRLN = 34,		NCALL = 35,
				NRETN = 36,		NVLIST = 37,	NSIMV = 38,		NARRV = 39,		NEXPL = 40,
				NBOOL = 41,		NNOT = 42,		NAND = 43,		NOR = 44,		NXOR = 45,
				NEQL = 46,		NNEQ = 47,		NGRT = 48,		NLSS = 49,		NLEQ = 50,
				NADD = 51,		NSUB = 52,		NMUL = 53,		NDIV = 54,		NMOD = 55,
				NPOW = 56,		NILIT = 57,		NFLIT = 58,		NTRUE = 59,		NFALS = 60,
				NFCALL = 61,	NPRLST = 62,	NSTRG = 63,		NGEQ = 64, 		EPSLON = 65,
				SPEC = 66;

	public static final String PRINTNODE[] = {  	//  PRINTNODE[TreeNode Value] will produce the associated String
						  							//  e.g. PRINTNODE[NPROG] will be the String "NPROG".
				"NUNDEF",
				"NPROG ",	"NGLOB ",	"NILIST",	"NINIT ",	"NFUNCS",
				"NMAIN ",	"NSDLST",	"NTYPEL",	"NRTYPE",	"NATYPE",
				"NFLIST",	"NSDECL",	"NALIST",	"NARRD ",	"NFUND ",
				"NPLIST",	"NSIMP ",	"NARRP ",	"NARRC ",	"NDLIST",
				"NSTATS",	"NFOR  ",	"NREPT ",	"NASGNS",	"NIFTH ",
				"NIFTE ",	"NASGN ",	"NPLEQ ",	"NMNEQ ",	"NSTEQ ",
				"NDVEQ ",	"NINPUT",	"NPRINT",	"NPRLN ",	"NCALL ",
				"NRETN ",	"NVLIST",	"NSIMV ",	"NARRV ",	"NEXPL ",
				"NBOOL ",	"NNOT  ",	"NAND  ",	"NOR   ",	"NXOR  ",
				"NEQL  ",	"NNEQ  ",	"NGRT  ",	"NLSS  ",	"NLEQ  ",
				"NADD  ",	"NSUB  ",	"NMUL  ",	"NDIV  ",	"NMOD  ",
				"NPOW  ",	"NILIT ",	"NFLIT ",	"NTRUE ",	"NFALS ",
				"NFCALL",	"NPRLST",	"NSTRG ",	"NGEQ  ", 	"EPSLON", "SPEC  "};

	public static int getNodeValue(String s) {	// Takes a lexeme recognised as an ID
											// Returns the correct keyword Token number
		if ( s.equals("NUNDEF")	) { return NUNDEF; }
		if ( s.equals("NPROG")	) { return NPROG; }
		if ( s.equals("NGLOB")	) { return NGLOB; }
		if ( s.equals("NILIST")	) { return NILIST; }
		if ( s.equals("NINIT")	) { return NINIT; }	
		if ( s.equals("NFUNCS")	) { return NFUNCS; }
		if ( s.equals("NMAIN")	) { return NMAIN; }	
		if ( s.equals("NSDLST")	) { return NSDLST; }	
		if ( s.equals("NTYPEL")	) { return NTYPEL; }
		if ( s.equals("NRTYPE")	) { return NRTYPE; }	
		if ( s.equals("NATYPE")	) { return NATYPE; }
		if ( s.equals("NFLIST")	) { return NFLIST; }	
		if ( s.equals("NSDECL")	) { return NSDECL; }	
		if ( s.equals("NALIST")	) { return NALIST; }	
		if ( s.equals("NARRD")	) { return NARRD; }	
		if ( s.equals("NFUND")	) { return NFUND; }
		if ( s.equals("NPLIST")	) { return NPLIST; }	
		if ( s.equals("NSIMP")	) { return NSIMP; }	
		if ( s.equals("NARRP")	) { return NARRP; }	
		if ( s.equals("NARRC")	) { return NARRC; }	
		if ( s.equals("NDLIST")	) { return NDLIST; }
		if ( s.equals("NSTATS")	) { return NSTATS; }	
		if ( s.equals("NFOR")	) { return NFOR; }	
		if ( s.equals("NREPT")	) { return NREPT; }	
		if ( s.equals("NASGNS")	) { return NASGNS; }	
		if ( s.equals("NIFTH")	) { return NIFTH; }
		if ( s.equals("NIFTE")	) { return NIFTE; }
		if ( s.equals("NASGN")	) { return NASGN; }
		if ( s.equals("NPLEQ")	) { return NPLEQ; }	
		if ( s.equals("NMNEQ")	) { return NMNEQ; }	
		if ( s.equals("NSTEQ")	) { return NSTEQ; }
		if ( s.equals("NDVEQ")	) { return NDVEQ; }	
		if ( s.equals("NINPUT")	) { return NINPUT; }	
		if ( s.equals("NPRINT")	) { return NPRINT; }	
		if ( s.equals("NPRLN")	) { return NPRLN; }	
		if ( s.equals("NCALL")	) { return NCALL; }
		if ( s.equals("NRETN")	) { return NRETN; }	
		if ( s.equals("NVLIST")	) { return NVLIST; }	
		if ( s.equals("NSIMV")	) { return NSIMV; }	
		if ( s.equals("NARRV")	) { return NARRV; }	
		if ( s.equals("NEXPL")	) { return NEXPL; }
		if ( s.equals("NBOOL")	) { return NBOOL; }	
		if ( s.equals("NNOT")	) { return NNOT; }
		if ( s.equals("NAND")	) { return NAND; }	
		if ( s.equals("NOR")	) { return NOR; }
		if ( s.equals("NXOR")	) { return NXOR; }
		if ( s.equals("NEQL")	) { return NEQL; }		
		if ( s.equals("NNEQ")	) { return NNEQ; }		
		if ( s.equals("NGRT")	) { return NGRT; }		
		if ( s.equals("NLSS")	) { return NLSS; }		
		if ( s.equals("NLEQ")	) { return NLEQ; }
		if ( s.equals("NADD")	) { return NADD; }		
		if ( s.equals("NSUB")	) { return NSUB; }		
		if ( s.equals("NMUL")	) { return NMUL; }		
		if ( s.equals("NDIV")	) { return NDIV; }		
		if ( s.equals("NMOD")	) { return NMOD; }
		if ( s.equals("NPOW")	) { return NPOW; }		
		if ( s.equals("NILIT")	) { return NILIT; }	
		if ( s.equals("NFLIT")	) { return NFLIT; }
		if ( s.equals("NTRUE")	) { return NTRUE; }
		if ( s.equals("NFALS")	) { return NFALS; }
		if ( s.equals("NFCALL")	) { return NFCALL; }	
		if ( s.equals("NPRLST")	) { return NPRLST; }
		if ( s.equals("NSTRG")	) { return NSTRG; }
		if ( s.equals("NGEQ")	) { return NGEQ; }

		return SPEC;		// not a Keyword
	}
}