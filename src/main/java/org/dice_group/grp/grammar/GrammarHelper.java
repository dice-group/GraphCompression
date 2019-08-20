package org.dice_group.grp.grammar;

public class GrammarHelper {

	public static final String NON_TERMINAL_PREFIX = ":n";
	
	private static Long nonT = 0l;
	
	/**
	 * This is our Non Terminal 
	 * @return
	 */
	public static String getNextNonTerminal() {

		return NON_TERMINAL_PREFIX+nonT++;
	}

	public static Long getIDOfNT(String uriNT) {
		return Long.valueOf(uriNT.replace(NON_TERMINAL_PREFIX, ""));
	}
	
}
