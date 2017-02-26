package com.robestone.species.parse;

import java.util.Collection;
import java.util.List;

public class NumberListCompoundCruncher {

	private int minNumberLength = 3;
	private int maxNumberLength = 10;
	private NumberListCruncher[] crunchers = new NumberListCruncher[maxNumberLength + 1];
	
	public NumberListCompoundCruncher() {
		for (int i = minNumberLength; i <= maxNumberLength; i++) {
			crunchers[i] = new NumberListSubtractionCruncher(i);
		}
	}
	
	// TODO now we concatenate the entire string based on each possible zero-pad, and then
	//			divide them up by other possible padding
	
	
	public List<Integer> toList(String crunchedNumbers) {
		// TODO optimize
		String indexString = crunchedNumbers.substring(0, 1);
		int index = Integer.parseInt(indexString) + minNumberLength;
		String actualNumbers = crunchedNumbers.substring(1);
		return crunchers[index].toList(actualNumbers);
	}

	public String toString(Collection<Integer> numbers) {
		String best = null;
		int bestIndex = 0;
		for (int i = minNumberLength; i <= maxNumberLength; i++) {
			String next = crunchers[i].toString(numbers);
			if (best == null || next.length() <= best.length()) {
				best = next;
				bestIndex = i;
			}
		}
		return (bestIndex - minNumberLength) + best;
	}
	
	
}
