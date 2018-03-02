// Reference: https://en.wikipedia.org/wiki/PageRank

package tw.cchi.prdemo.test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PageRankCalculator {
	
	private static final double DAMPING_FACTOR = 0.85;
	private static final int RUN_COUNT = 100;
	
	public static Map<Integer, Double> CalculatePR(Map<Integer, String> pages, Map<Integer, List<Integer>> links) {
		Map<Integer, Double> prMap = new HashMap<Integer, Double>();
		Map<Integer, List<Integer>> linkFrom = new HashMap<Integer, List<Integer>>(); // [id] = list of page id which has link to this page
		
		// find which page has link to a page (can be done with SQL query)
		for (Map.Entry<Integer, String> pagesEntry : pages.entrySet()) {
			int id = pagesEntry.getKey();
			List<Integer> list = new ArrayList<Integer>();
			for (Map.Entry<Integer, List<Integer>> link : links.entrySet()) {
				if (link.getValue().contains(id)) {
					list.add(link.getKey());
				}
			}
			System.out.println("Has link to " + id + ": " + list.toString());
			linkFrom.put(id, list);
		}
		
		// pr value initialization
		double initialPR = 1.0 / pages.size();
		System.out.println("Initial pr value=" + initialPR);
		for (Entry<Integer, String> pagesEntry : pages.entrySet()) {
			prMap.put(pagesEntry.getKey(), initialPR);
		}
		
		// calculate pr
		double prConst = (1 - DAMPING_FACTOR) / pages.size();
		for (int i = 0; i < RUN_COUNT; i++) {
			for (Map.Entry<Integer, String> pagesEntry : pages.entrySet()) {
				int id = pagesEntry.getKey();
				double pr = 0;
				for (int linkId : linkFrom.get(id)) {
					if (linkId == id) continue;
					pr += prMap.get(linkId) / links.get(linkId).size();
				}
				pr = prConst + DAMPING_FACTOR * pr;
				prMap.put(id, pr);
			}
		}
		return prMap;
	}
}
