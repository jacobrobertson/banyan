package com.robestone.species.parse;

import java.util.Collection;
import java.util.List;

public interface NumberListCruncher {

	String toString(Collection<Integer> numbers);
	List<Integer> toList(String crunchedNumbers);
}
