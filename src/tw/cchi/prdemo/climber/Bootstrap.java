package tw.cchi.prdemo.climber;

import java.util.ArrayList;

public class Bootstrap {
	
	public static final int MAX_RELATIONSHIPS_LIMIT = 1000000;
	public static final int THREAD_COUNT = 5;
	
	private static ArrayList<String> rootNodes = new ArrayList<String>();
	
	public static void main(String[] args) throws InterruptedException {		
		initData();
		
		Thread[] climbThreads = new Thread[THREAD_COUNT];
		climbThreads[0] = new Thread(new ClimbThread(rootNodes), "ClimbThread 0");
		climbThreads[0].start();
		Thread.sleep(1000);
		for (int i = 1; i < THREAD_COUNT; i++) {
			climbThreads[i] = new Thread(new ClimbThread(new ArrayList<String>()), "ClimbThread" + i);
			climbThreads[i].start();
		}
		
		// waiting all thread finish running
		boolean finished = false;
		while (!finished) {
			finished = true;
			for (int i = 0; i < THREAD_COUNT; i++) {
				if (climbThreads[i].isAlive()) finished = false;
			}
		}
		
		// free resources
		System.out.println("Finished.");
	}
	
	private static void initData() {
		
		//rootNodes.add("http://csie.ntut.edu.tw/csie/Chinese/index.html");
		
		/*
		rootNodes.add("http://localhost/test/PageRank/sample-pages_xlsModel/a.html");
		rootNodes.add("http://localhost/test/PageRank/sample-pages_xlsModel/d.html");
		*/
		
	}
	
	private static class ClimbThread implements Runnable {
		
		private ArrayList<String> rootNodes;
		
		public ClimbThread(ArrayList<String> rootNodes) {
			this.rootNodes = rootNodes;
		}
		
		public void run() {
			System.out.println(Thread.currentThread().getName() + " start!");
			WebPageClimber wc = new WebPageClimber(rootNodes, MAX_RELATIONSHIPS_LIMIT);
			wc.startClimbing();
			wc.close();
			System.out.println(Thread.currentThread().getName() + " finished!");
		}
	}

}
