package org.dice_group.grp.grammar;

public class GrammarHelper {

	public static final String NON_TERMINAL_PREFIX = "http://n.";
	
	private static int nonT = 0;
	private static int nonTi = 0;


	/**
	 * This is our Non Terminal 
	 * @return
	 */
	public static String getNextNonTerminal() {

		return NON_TERMINAL_PREFIX+nonT++;
	}

	public static Long getIDOfNT(String uriNT) {
		//nonT should be in HDT Dict
		return Long.valueOf(uriNT.replace(NON_TERMINAL_PREFIX, ""));
	}

	public static int getNextNonTerminalInt() {
		return nonTi++;
	}

	public static void setStartIndexForNT(int ntStart){
		nonTi=ntStart;
	}
}
