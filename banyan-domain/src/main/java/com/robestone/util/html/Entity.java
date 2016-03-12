/*
 * Created on Dec 17, 2003
 */
package com.robestone.util.html;

/**
 * @author cjsr
 */
public class Entity {

	private int number;
	private String name;
	private String symbol;
	private String searchText;
	private boolean isNumberUsed;
	private String description;

	public Entity(int number, String name, String symbol, String searchText, String description) {
		this.number = number;
		this.name = name;
		this.symbol = symbol;
		this.searchText = searchText;
		isNumberUsed = number >= 0;
//		// for fun, validate the symbol
//		int symInt = (int) symbol.charAt(0);
//		if (symInt != number) {
//			throw new IllegalArgumentException("NO GOOD SUMBVOL! symbol=" + symbol + ",symInt=" + symInt + ",number=" + number);
//		}
	}
	public String getDescription() {
		return description;
	}
	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}
	public boolean isNumberUsed() {
		return isNumberUsed;
	}
	public String getSearchText() {
		return searchText;
	}
	/**
	 * Just a convenience to calling String.valueOf((char) getNumber())
	 */
	public String getSymbol() {
		return symbol;
	}
	public String toString() {
		return "[" + number + "|" + symbol + "|" + name + "|" + searchText + "]";
	}

}
