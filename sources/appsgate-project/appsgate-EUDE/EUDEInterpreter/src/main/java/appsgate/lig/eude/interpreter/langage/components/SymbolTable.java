package appsgate.lig.eude.interpreter.langage.components;

import java.util.Vector;

public class SymbolTable {
	
	private Vector<String> symbols;
	
	public SymbolTable() {
		symbols = new Vector<String>();
	}
	
	public void add(String symbol) {
		if (!symbols.contains(symbol)) {
			symbols.add(symbol);
		}
	}
	
}
