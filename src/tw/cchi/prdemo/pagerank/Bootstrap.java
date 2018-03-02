package tw.cchi.prdemo.pagerank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import tw.cchi.prdemo.pagerank.DBOperation;
import tw.cchi.prdemo.pagerank.PageRankCalculator;
import tw.cchi.prdemo.struct.PageInfo;
import tw.cchi.prdemo.util.MySQLconn;

public class Bootstrap {
	
	private static Map<Integer, PageInfo> pages;
	private static Map<Integer, ArrayList<Integer>> links; // [id] = list of links in that page
	
	private static Connection conn = null; // MySQL connection object
	private static DBOperation db;
	
	public static void main(String[] args) {		
		init();
		loadDataFromDB();
		
		// print pages
		for (Entry<Integer, PageInfo> pageEntry : pages.entrySet()) {
			PageInfo info = pageEntry.getValue();
			System.out.printf("%d: %s : %s\n", info.id, info.title, info.url);
		}
		
		// print links
		for (Entry<Integer, ArrayList<Integer>> entry : links.entrySet()) {
			System.out.printf("%s : %s\n", pages.get(entry.getKey()).title, entry.getValue().toString());
		}
		
		// calculate page rank
		System.out.println("Page count: " + pages.size());
		PageRankCalculator.CalculatePR(pages, links);
		
		// write page rank back into db
		writeBack();
		
		// print result
		for (Entry<Integer, PageInfo> entry : pages.entrySet()) {
			PageInfo info = entry.getValue();
			System.out.printf("%s, %.10f\n", info.title, info.pagerank);
		}
	}
	
	private static void init() {
		// connect db
		if (conn == null) conn = MySQLconn.getConnObject();
		db = new DBOperation(conn);
	}
	
	private static void loadDataFromDB() {
		try {
			pages = db.getClimbedPages();
			links = db.getRelationships();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	private static void writeBack() {
		try {
			db.updatePageRank(pages);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
