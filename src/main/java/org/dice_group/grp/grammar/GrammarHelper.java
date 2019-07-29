package org.dice_group.grp.grammar;

public class GrammarHelper {

	private String prefix = "u:";
	
	private Integer nonT = 0;
	
	/**
	 * This is our Non Terminal 
	 * @return
	 */
	public String getNextNonTerminal() {

		return prefix+nonT++;
	}
	
}
