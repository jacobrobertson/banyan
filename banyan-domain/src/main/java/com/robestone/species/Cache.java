package com.robestone.species;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class Cache {

	private SpeciesService speciesService;
	private ImageService imageService;
	
	private Map<Integer, EntryProperties> entryProperties = new WeakHashMap<Integer, EntryProperties>();
	private Map<Integer, Image> images = new WeakHashMap<Integer, Image>();
	private Map<Integer, List<Integer>> childrenIds = new WeakHashMap<Integer, List<Integer>>();
	private Set<Integer> noImageIds = new HashSet<Integer>();
	
	private Integer[] allIds;
	
	/**
	 * DO NOT MUTATE THIS ARRAY.
	 * @return all ids.
	 */
	public Integer[] getAllIds() {
		if (allIds == null) {
			Collection<Integer> ids = speciesService.findAllIdsForCaching();
			allIds = new Integer[ids.size()];
			allIds = ids.toArray(allIds);
		}
		return allIds;
	}
	private Image getImage(Integer id) {
		if (noImageIds.contains(id)) {
			return null;
		}
		Image image = images.get(id);
		if (image == null) {
			image = imageService.findImage(id);
			if (image != null) {
				images.put(id, image);
			} else {
				noImageIds.add(id);
			}
		}
		return image;
	}
	public EntryProperties getEntryProperties(Integer id) {
		EntryProperties p = entryProperties.get(id);
		if (p == null) {
			Entry e = speciesService.findEntryFromPersistence(id);
			p = e.getEntryProperties();
			p.image = getImage(e);
			entryProperties.put(id, p);
		}
		return p;
	}
	private Image getImage(Entry entry) {
		Image image = getImage(entry.getId());
		if (image != null && entry.getImageLink() != null) {
			image.setLink(entry.getImageLink());
		} else {
			// see if we have a reference
			int linkedId = entry.getEntryProperties().linkedImageId;
			// 0 is the DB null value
			if (linkedId > 0) {
				EntryProperties props = getEntryProperties(linkedId);
				image = props.image;
			}
		}
		// if it's null here, just return null
		return image;
	}
	public List<Integer> getChildrenIds(Integer id) {
		List<Integer> ids = childrenIds.get(id);
		if (ids == null) {
			ids = speciesService.findChildrenIdsFromPersistence(id);
			childrenIds.put(id, ids);
		}
		return ids;
	}
	public void clear() {
		entryProperties.clear();
		images.clear();
		childrenIds.clear();
		allIds = null;
	}
	public void setSpeciesService(SpeciesService speciesService) {
		this.speciesService = speciesService;
	}
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}
	
}
