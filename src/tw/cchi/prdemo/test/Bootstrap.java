package tw.cchi.prdemo.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Bootstrap {
	
	private static Map<Integer, String> pages;
	private static Map<Integer, List<Integer>> links; // [id] = list of links in that page
	
	public static void main(String[] args) {		
		initObjects();
		initLinks();
		
		System.out.println("Start " + pages.size());
		Map<Integer, Double> prMap = PageRankCalculator.CalculatePR(pages, links);
		for (Entry<Integer, Double> entry : prMap.entrySet()) {
			System.out.printf("%d, %s\n", entry.getKey(), entry.getValue());
		}
	}
	
	private static void initObjects() {
		pages = new HashMap<Integer, String>();
		links = new HashMap<Integer, List<Integer>>();
	}
	
	private static void initLinks() {
		// generate pages
		pages.put(new Integer(0), "Site A");
		pages.put(new Integer(1), "Site B");
		pages.put(new Integer(2), "Site C");
		pages.put(new Integer(3), "Site D");
		
		// generate links
		/*links.put(1, Arrays.asList(0,2));
		links.put(2, Arrays.asList(0,3));
		links.put(3, Arrays.asList(0,1,2));*/
		links.put(0, Arrays.asList(1,2,3));
		links.put(1, Arrays.asList(2,3));
		links.put(2, Arrays.asList(0));
		links.put(3, Arrays.asList(0,2));
		/*
		links.put(0, Arrays.asList(1));
		links.put(1, Arrays.asList(2));
		links.put(2, Arrays.asList(3));
		links.put(3, Arrays.asList(0));*/
	}
}
