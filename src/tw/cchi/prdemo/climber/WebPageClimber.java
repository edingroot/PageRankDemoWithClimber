package tw.cchi.prdemo.climber;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tw.cchi.prdemo.struct.PageInfo;
import tw.cchi.prdemo.util.MySQLconn;


public class WebPageClimber {
	
	private final int BATCH_CLIMB_COUNT = 10;
	
	private ArrayList<String> rootNodes = new ArrayList<String>();
	private long maxRelationshipsLimit;
	
	private Connection conn = null; // MySQL connection object
	private DBOperation db;
	
	
	public WebPageClimber(ArrayList<String> rootNodes, int maxRelationshipsLimit) {
		if (conn == null) conn = MySQLconn.getConnObject();
		db = new DBOperation(conn);
		this.rootNodes = rootNodes;
		this.maxRelationshipsLimit = maxRelationshipsLimit;
		httpTrustEveryone();
	}
	
	public void startClimbing() {
		try {
			for (String url : rootNodes)
				climbPage(url, 0);
			climbPagesFromDB();
		} catch (SQLException e) {
			System.out.println("DB Exception: " + e.toString());
		}
		
	}
	
	public void climbPagesFromDB() throws SQLException {
		ArrayList<PageInfo> pages;
		System.out.println("relationships count=" + db.getRelationshipsCount());
		while (db.getRelationshipsCount() < maxRelationshipsLimit) {
			pages = db.getPagesForClimbing(BATCH_CLIMB_COUNT);
			if (pages.size() == 0) break;
			/* if (!pages.next()) {
				break;
			} else {
				climbPage(pages.getString("url"), pages.getInt("climb_depth"));
				while (pages.next() && db.getRelationshipsCount() < maxRelationshipsLimit) {
					climbPage(pages.getString("url"), pages.getInt("climb_depth"));
				}
			}*/
			for (PageInfo pageInfo : pages) {
				climbPage(pageInfo.url, pageInfo.climbDepth);
			}
			System.out.println("relationships count=" + db.getRelationshipsCount());
		}
	}
	
	public void climbPage(String url, int pageDepth) throws SQLException {
		// check whether the page is exist and not climbed
		int pageId = db.getPageIdByUrl(url, pageDepth, true);
		if (pageId == -1) return;
		db.setClimbed(pageId);
		
		// fetch page content from Internet
		System.out.printf("Fetching %s\n", url);
        Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			System.out.println("Error fetching: " + url);
			db.setClimbFailed(pageId);
			//System.out.println("------------------------------------");
			// e.printStackTrace();
			return;
		}
		
        //System.out.println("Analyzing...");
        // update page content to db
        // TODO: check if the page title is included in the content
        db.updatePageInfo(pageId, doc.title(), doc.text());
        
        // store links to db
        ArrayList<Element> links = new ArrayList<Element>(new LinkedHashSet<Element>(doc.select("a[href]"))); // use LinkedHashSet to ignore duplicate elements 
        //System.out.printf("Links: (%d)\n", links.size());
        ArrayList<Integer> childIds = db.getChildIdsByParentId(pageId);
        for (Element link : links) {
        	// TODO: ignore html tag (#tagname) from url
        	if (link.attr("abs:href").length() == 0) continue;
        	//System.out.printf(" * a: <%s>  (%s)\n", link.attr("abs:href"), link.text());
        	int childId = db.getPageIdByUrl(link.attr("abs:href"), pageDepth + 1, false);
        	if (!childIds.contains(childId)) {
        		db.insertRelationship(pageId, childId);
        		childIds.add(childId);
        	}
        	
        }
        
        //System.out.println("------------------------------------");
        //System.out.println(doc.text());
	}
	
	public void close() {
		db.close();
	}
	
	private void httpTrustEveryone() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
