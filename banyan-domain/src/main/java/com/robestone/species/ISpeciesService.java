package com.robestone.species;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

/**
 * Spring is okay with not having an interface, but Tapestry is insisting on it.
 * Keep these down to just what tapestry needs.
 * 
 * @author Jacob Robertson
 */
public interface ISpeciesService {

	List<CompleteEntry> findChildren(Integer id);

	CompleteEntry findRandomTree(int treeSize);
	
	/**
	 * Filters out boring things.
	 */
	Collection<Integer> findChildrenIds(Integer id);

	Set<Integer> findBestIds(String terms, Collection<Integer> existingIds);

	/**
	 * @param uses some logic to find the first/best match.
	 */
	Set<Integer> findBestIds(Set<String> names, Collection<Integer> existingIds);

	/**
	 * @param uses some logic to find the first/best match.
	 */
	int findBestId(String name, Collection<Integer> existingIds);

	Entry findTreeForNodes(Collection<Integer> ids, Entry existingRoot);

	CompleteEntry findTreeForNodes(Set<Integer> ids);

	/**
	 * I had intended to do this more efficiently...
	 */
	List<CompleteEntry> findEntries(Set<Integer> ids);

	CompleteEntry findEntry(Integer id);
	Entry findDepictedEntry(Entry entry);

	void clearCache();

	/**
	 * TODO get a more targeted set of attributes for different needs
	 */
	CompleteEntry findEntryByLatinName(String latinName);

	CompleteEntry findEntryByLatinName(String latinName,
			boolean getParentLatinName);

	void setDataSource(DataSource dataSource);

}