package com.robestone.species;

import java.util.Collection;

public interface EntrySearcher {

	int search(String queryString, Collection<Integer> existingIds);
}
