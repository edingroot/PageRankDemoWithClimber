// Reference: https://en.wikipedia.org/wiki/PageRank

package tw.cchi.prdemo.pagerank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tw.cchi.prdemo.struct.PageInfo;

public class PageRankCalculator {
	
	private static final double DAMPING_FACTOR = 0.85;
	private static final double DELIM = 0.00001;
	
	public static void CalculatePR(Map<Integer, PageInfo> pages, Map<Integer, ArrayList<Integer>> links) {
		Map<Integer, ArrayList<Integer>> linkFrom = new HashMap<Integer, ArrayList<Integer>>(); // [id] = list of page id which has link to this page
		Map<Integer, Double> lastPr = new HashMap<>();
		
		// find which page has link to a page (can be done with SQL query)
		for (Entry<Integer, PageInfo> pageEntry : pages.entrySet()) {
			int id = pageEntry.getKey();
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (Map.Entry<Integer, ArrayList<Integer>> link : links.entrySet()) {
				if (link.getValue().contains(id)) {
					list.add(link.getKey());
				}
			}
			System.out.println("Has link to " + id + ": " + list.toString());
			linkFrom.put(id, list);
		}
		
		// pr value and lastPR hashmap initialization
		//-------- double initialPR = 1.0 / pages.size();
		double initialPR = 1.0;
		System.out.println("Initial pr value=" + initialPR);
		for (Entry<Integer, PageInfo> pageEntry : pages.entrySet()) {
			int id = pageEntry.getKey();
			PageInfo pageInfo = pageEntry.getValue();
			pageInfo.pagerank = initialPR;
			pages.put(id, pageInfo);
			
			lastPr.put(id, 0.0);
		}
		
		// calculate pr
		boolean finishCalculate = false;
		double prConst = (1 - DAMPING_FACTOR) / pages.size();
		while (!finishCalculate) {
			double sum = 0;
			for (Entry<Integer, PageInfo> pageEntry : pages.entrySet()) {
				sum += pageEntry.getValue().pagerank;
			}
			System.out.println("sum=" + sum);
			finishCalculate = true;
			for (Entry<Integer, PageInfo> pageEntry : pages.entrySet()) {
				int id = pageEntry.getKey();
				double pr = 0;
				for (int linkId : linkFrom.get(id)) {
					if (linkId == id) continue;
					pr += pages.get(linkId).pagerank / links.get(linkId).size();
				}
				pr = prConst + DAMPING_FACTOR * pr;
				PageInfo pageInfo = pageEntry.getValue();
				pageInfo.pagerank = pr;
				pages.put(pageInfo.id, pageInfo);
				if (Math.abs(pr - lastPr.get(id)) > DELIM)
					finishCalculate = false;
				lastPr.put(id, pr);
			}
		}
	}
}
