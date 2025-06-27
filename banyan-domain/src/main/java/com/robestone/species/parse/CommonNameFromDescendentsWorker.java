package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.robestone.species.CommonNameHint;
import com.robestone.species.CommonNameSimilarityChecker;
import com.robestone.species.CommonNameSplitter;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.species.Tree;

/**
 * See doc for notes.
 *  
 * @author jacob
 */
public class CommonNameFromDescendentsWorker extends AbstractWorker {

	public static void main(String[] args) {
		new CommonNameFromDescendentsWorker().run();
	}
	
	private static int MAX_COMMON_NAME_LENGTH = CommonNameSplitter.MAX_KEEP_LENGTH;
	
	public void run() {
		Tree all = speciesService.findCompleteTreeFromPersistence();
		for (Entry e: all.getEntries()) {
			CommonNameSplitter.assignCommonNames(e);
		}
		for (Entry e: all.getEntries()) {
			boolean indicator = nameHasIndicator(e.getCommonName());
			if (e.getCommonName() != null && !indicator) {
				continue;
			}
			if ("Cetartiodactyla".equals(e.getLatinName())) {
				System.out.println("found");
			}
			String before = e.getCommonName();
			createCommonName(e);
			String after = e.getCommonName();
			
			
			boolean update = false;
			if (after != null && !after.equals(before)) {
				update = true;
			} else if (after != null && after.startsWith(EntryUtilities.COMMON_NAME_FROM_DESCENDENTS_INDICATOR)) {
				// this is to repair names before I changed the convention - delete this code later
				e.setCommonName(null);
				update = true;
			}
			
			if (update) {
				speciesService.updateCommonName(e);
			}
		}
	}
	private static final int MAX_GATHER_DEPTH = 5;
	private static final int MAX_GATHER_NAMES = 100;
	private void createCommonName(Entry e) {
		List<Score> names = new ArrayList<>();
		// first gather all names from the max depth
		List<Entry> start = new ArrayList<>();
		start.add(e);
		gatherCommonNames(start, names, 0);
		normalizeNames(names);
		splitCommonNames(names);
		
		// second create final name suggestions from those
		createCommonName(e, names);
	}
	private boolean nameHasIndicator(String name) {
		return name != null &&
				name.indexOf(EntryUtilities.COMMON_NAME_FROM_DESCENDENTS_INDICATOR) >= 0;
	}
	private void gatherCommonNames(List<Entry> entries, List<Score> names, final int currentDepth) {
		List<Entry> allChildren = new ArrayList<>();
		entries.forEach(e -> {
			if (e.getCompleteEntryChildren() != null) {
				allChildren.addAll(e.getCompleteEntryChildren());
			}
			String name = e.getCommonName();
			if (name != null && !nameHasIndicator(name)) {
				List<String> split = CommonNameSplitter.splitCommonName(e, -1);
				String sname;
				if (split == null) {
					sname = name;
				} else {
					sname = split.get(0);
				}
				boolean boring = e.isBoring(); // CommonNameSimilarityChecker.isCommonNameBoring(sname, e.getLatinName());
				names.add(new Score(sname, currentDepth, boring, e.getParentId()));
			}
		});
		if (currentDepth <= MAX_GATHER_DEPTH && names.size() < MAX_GATHER_NAMES) {
			gatherCommonNames(allChildren, names, currentDepth + 1);
		}
	}
	
	private void splitCommonNames(List<Score> names) {
		List<Score> splits = new ArrayList<>();
		
		for (Score score: names) {
			splitCommonName(score, splits);
		}
		
		names.addAll(splits);
	}

	private void splitCommonName(Score score, List<Score> splits) {
		String name = score.name;
		while (true) {
			int spos = name.indexOf(' ');
			int dpos = name.indexOf('-');
			if (dpos > 0 && dpos < spos) {
				spos = dpos;
			}
			if (spos > 0) {
				name = name.substring(spos + 1);
				name = CommonNameSplitter.trimPart(name);
				splits.add(new Score(name, score.depth, score.boring, score.parentEntryId));
			} else {
				break;
			}
		}
	}
	
	private void createCommonName(Entry e, List<Score> names) {
		
		int maxLength = MAX_COMMON_NAME_LENGTH;
		List<String> keys = toKeys(names);
		Map<String, Score> scoreMap = new HashMap<>();
		Map<String, Integer> parentIdAndNameToCountMap = new HashMap<>();
		for (int i = 0; i < names.size(); i++) {
			Score name = names.get(i);
			if (isIgnored(name.name)) {
				continue;
			}
			String initialKey = keys.get(i);
			String key = toKey(initialKey, keys);
			Score score = scoreMap.get(key);
			if (score == null) {
				// this will be inconsistent if we have names like "Red Shark" and "Blue shark"
				// we prefer to use proper case if that ever appears, but for now just do lowercase, and it gets fixed later
				score = new Score(name.name.toLowerCase(), -1, name.boring, name.parentEntryId);
			}
			
			String parentIdAndName = name.parentEntryId + "/" + score.name;
			Integer parentIdAndNameCount = parentIdAndNameToCountMap.get(parentIdAndName);
			if (parentIdAndNameCount == null) {
				parentIdAndNameCount = 0;
			}
			int siblingWeight = parentIdAndNameCount;
			score.increment(name.depth, siblingWeight, score.boring);
			scoreMap.put(key, score);
			parentIdAndNameToCountMap.put(parentIdAndName, parentIdAndNameCount + 1);
		}
		
		List<Score> scoreList = new ArrayList<>(scoreMap.values());
		Collections.sort(scoreList);
		
		// find names until I reach maxLength
		StringBuilder buf = new StringBuilder();
		List<String> selectedNames = new ArrayList<>();
		for (Score score : scoreList) {
			// we don't want to add common names that just repeat the child if we already have a name
			// this didn't work, for example if there are 3 children with different names
//			if (score.count == 1 && buf.length() > 1) {
//				break;
//			}
			if (
					(buf.length() + 2 + score.name.length() > maxLength)
					// don't skip if the very first name is too long
					&& buf.length() > 0) {
				break;
			}	
			// checks whether the name is a subset of a name already chosen
			if (buf.indexOf(score.name) < 0) {
				if (buf.length() > 0) {
					buf.append(", ");
				}
				buf.append(score.name);
				selectedNames.add(score.name);
			}
		}
		
		// TODO verify that we actually want to use this name
		selectedNames = selectedNames.stream().map(n -> n.toUpperCase()).
				collect(Collectors.toList());
		float matchPercent = getPercentUsed(selectedNames, e);
		float minPercent = .85f;
		boolean useName = (matchPercent >= minPercent);
		
		// name should be good, just assign it
		// we won't necessarily use this look and feel, but want a way to clearly show what this is
		if (useName && buf.length() > 0) {
			String fname = EntryUtilities.fixCommonName(buf.toString());
			e.setCommonName(fname);
			CommonNameSplitter.assignCommonNames(e, -1);
			e.setCommonName(e.getCommonName() + EntryUtilities.COMMON_NAME_FROM_DESCENDENTS_INDICATOR);
			
			StringBuilder toLog = new StringBuilder();
			scoreList.forEach(score -> toLog.append(score.name).append(", "));
			LogHelper.speciesLogger.info(e.getCommonName() + "/" + e.getLatinName() + " << " + toLog);
		} else if (!useName && nameHasIndicator(e.getCommonName())) {
			// need this to remove any that were created under different rules (i.e. prior to this version of code)
			e.setCommonName(null);
		}
	}
	
	private float getPercentUsed(List<String> names, Entry e) {
		Set<Entry> children = EntryUtilities.getEntries(e);
		
		float count = 0;
		float found = 0;
		
		for (Entry c : children) {
			String compare = c.getCommonNameClean();
			if (compare != null) {
				count++;
				if (isUsed(names, compare)) {
					found++;
				}
			}
		}
		
		return found / count;
	}
	/**
	 * @param names already uppercase
	 * @param compare not null
	 */
	private boolean isUsed(List<String> names, String compare) {
		for (String name : names) {
			if (compare.indexOf(name) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Goal is to turn "crane fly" into "crane flies", but NOT to include as the name,
	 * and ONLY if there is that other name to match against.
	 */
	private String toKey(String key, List<String> keys) {
		for (int i = 0; i < keys.size(); i++) {
			String comp = keys.get(i);
			if (CommonNameSimilarityChecker.isFirstPluralOfSecond(comp, key)) {
				return comp;
			}
		}
		return key;
	}
	private List<String> toKeys(List<Score> names) {
		List<String> keys = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			String key = toKey(names.get(i).name);
			keys.add(key);
		}
		return keys;
	}
	private String toKey(String name) {
		String key = name.replaceAll("[ -]", "").toUpperCase();
		return key;
	}

	private static final String[] IGNORE_TOKENS = {"\"", "("};
	private boolean isIgnored(String name) {
		for (int i = 0; i < IGNORE_TOKENS.length; i++) {
			if (name.indexOf(IGNORE_TOKENS[i]) >= 0) {
				return true;
			}
		}
		return CommonNameHint.isHint(name);
	}

	/**
	 * Attempts to resolve the following names when taken together
	 * Crane fly
	 * Crane flies
	 * Craneflies
	 * Cranefly
	 * Brown craneflies
	 * Black cranefly
	 * Blue crane flies
	 * Red crane fly
	 * 
	 * Actual situation:
	 * Crane fly
	 * Crane flies
	 * Brown craneflies
	 * I want the result to be "Crane flies, Brown craneflies"
	 * 
	 * We are only going to normalize the last name
	 */
	private void normalizeNames(List<Score> names) {
		// TODO this was abandoned - should delete
//		for (int i = 0; i < names.size(); i++) {
//			normalizeNames(names.get(i).name, names);
//		}
//	}
//	private void normalizeNames(String name, List<Score> names) {
//		for (int i = 0; i < names.size(); i++) {
//			String check = names.get(i).name;
//			if (check.equals(name)) {
//				continue;
//			}
//			
//		}
	}

	private class Score implements Comparable<Score> {
		String name;
		
		private float score = 0;
		private int depth;
		boolean boring;
		Integer parentEntryId;

		public Score(String name, int depth, boolean boring, Integer parentEntryId) {
			this.name = name;
			this.depth = depth;
			this.boring = boring;
			this.parentEntryId = parentEntryId;
		}
		
		public void increment(int depth, int sameSiblingCount, boolean boring) {
			if (!boring) {
				score += 1f / ((float) depth + (float) sameSiblingCount);
			}
		}
		
		@Override
		public int compareTo(Score o) {
			// sort high to low
			int comp = Float.compare(o.score, this.score);
			if (comp == 0) {
				// sort longest name first so "Crested tit" is chosen over "tit"
				// could sort a little more smartly, but this is okay for now
				return o.name.length() - this.name.length();
			} else {
				return comp;
			}
		}
	}
	
}
