package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.robestone.species.Rank;

public class RankChecker extends AbstractWorker {

	public static void main(String[] args) {
		new RankChecker().run();
	}
	public void run() {
		Collection<Rank> ranks = speciesService.findUsedRanks();
		List<Rank> list = new ArrayList<Rank>(ranks);
		Collections.sort(list);
		for (Rank rank: list) {
			System.out.println(rank.getRankIndex() + " " + rank + "/" + rank.getCommonName());
		}
	}
	
}
