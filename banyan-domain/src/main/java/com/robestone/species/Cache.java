package com.robestone.species;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache {

	private SpeciesService speciesService;
	private ImageService imageService;
	
	private Map<Integer, EntryProperties> entryProperties = new HashMap<Integer, EntryProperties>();
	private Map<Integer, Image> images = new HashMap<Integer, Image>();
	private Map<Integer, List<Integer>> childrenIds = new HashMap<Integer, List<Integer>>();
	
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
		if (images.isEmpty()) {
			Collection<Image> list = imageService.findAllImages();
			for (Image i: list) {
				images.put(i.getEntryId(), i);
			}
		}
		return images.get(id);
	}
	public EntryProperties getEntryProperties(Integer id) {
		EntryProperties p = entryProperties.get(id);
		if (p == null) {
			CompleteEntry e = speciesService.findEntryFromPersistence(id);
			p = e.getEntryProperties();
			p.image = getImage(e);
			entryProperties.put(id, p);
		}
		return p;
	}
	private Image getImage(CompleteEntry entry) {
		Image image = getImage(entry.getId());
		if (image != null && entry.getImageLink() != null) {
			image.setLink(entry.getImageLink());
			image.setLocalNameFromLatinName(entry.getLatinName());
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
