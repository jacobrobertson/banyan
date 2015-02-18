package com.robestone.species;

import java.util.ArrayList;
import java.util.List;
import static com.robestone.species.CommonNameHintType.*;

public class CommonNameHint {

	public static final List<CommonNameHint> hints = buildHints(
//			"-grey", Disambiguation,
//			"Asian thrushes", Disambiguation,
//			"Bedstraw", Disambiguation,
//			"Caecilians", Disambiguation,
//			"Cattle", Disambiguation,
//			"Platynus", Disambiguation,
//			"Serranidae", Disambiguation,
//			"Even-toed hoofed mammals", Disambiguation,

			"Cayman Island", Locale,
			"Europe", Locale,
			"Italian", Locale,
			"Jamaica", Locale,
			"Lichens of N.A.", Locale,
			"New Zealand", Locale,
			"North America", Locale,			
			"Trinidad", Locale,
			"UK", Locale,
			"USA", Locale,
			"USDA", Locale,
			
			"amoeba", Disambiguation,
			"animal", Disambiguation,
			"antelope", Disambiguation,
			"anthropology", Disambiguation,
			"bacterium", Disambiguation,
			"beetle", Disambiguation,
			"biology", Disambiguation,
			"bird", Disambiguation,
			"bird genus", Disambiguation,
			"botany", Disambiguation,
			"butterfly", Disambiguation,
			"cicada", Disambiguation,
			"dinosaur", Disambiguation,
			"duck", Disambiguation,
			"family", Disambiguation,
			"fish", Disambiguation,
			"fishes", Disambiguation,
			"flower", Disambiguation,
			"fruit", Disambiguation,
			"fungi", Disambiguation,
			"fungus", Disambiguation,
			"genus", Disambiguation,
			"grass", Disambiguation,
			"herb", Disambiguation,
			"lizard", Disambiguation,
			"mammal", Disambiguation,
			"moth", Disambiguation,
			"orchid", Disambiguation,
			"owl", Disambiguation,
			"part", Disambiguation,
			"plant", Disambiguation,
			"plant genus", Disambiguation,
			"section", Disambiguation,
			"snake", Disambiguation,
			"spider", Disambiguation,
			"tern", Disambiguation,
			"tree", Disambiguation,
			"wild plant", Disambiguation,
			"worm", Disambiguation
	);
	private static List<CommonNameHint> buildHints(Object... args) {
		List<CommonNameHint> hints = new ArrayList<CommonNameHint>();
		for (int i = 0; i < args.length; i += 2) {
			hints.add(new CommonNameHint((CommonNameHintType) args[i + 1], (String) args[i]));
		}
		return hints;
	}
	
	private CommonNameHintType type;
	private String text;
	
	public CommonNameHint(CommonNameHintType type, String text) {
		this.type = type;
		this.text = text;
	}
	public CommonNameHintType getType() {
		return type;
	}
	public String getText() {
		return text;
	}
	
}
