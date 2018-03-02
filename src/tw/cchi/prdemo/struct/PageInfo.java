package tw.cchi.prdemo.struct;

public class PageInfo {
	public int id;
	public String url, title, content;
	public int climbDepth;
	public double pagerank;
	
	public PageInfo(int id, String url, String title) {
		this(id, url, title, null, 0);
	}
	
	public PageInfo(int id, String url, String title, String content, int climbDepth, double pagerank) {
		this.id = id;
		this.url = url;
		this.title = title;
		this.content = content;
		this.climbDepth = climbDepth;
		this.pagerank = pagerank;
	}
	
	public PageInfo(int id, String url, String title, String content, double pagerank) {
		this.id = id;
		this.url = url;
		this.title = title;
		this.content = content;
		this.pagerank = pagerank;
	}
}
