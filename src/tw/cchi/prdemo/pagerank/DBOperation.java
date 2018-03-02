package tw.cchi.prdemo.pagerank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tw.cchi.prdemo.struct.PageInfo;

class DBOperation {
	
	private Connection conn = null; // MySQL connection object
	private Statement sqlStatement = null; 
	private PreparedStatement sqlPreparedStatement = null;
	private ResultSet sqlResultSet = null;
	
	public DBOperation(Connection conn) {
		this.conn = conn;
	}
	
	public Map<Integer, PageInfo> getClimbedPages() throws SQLException {
		Map<Integer, PageInfo> pages = new HashMap<Integer, PageInfo>();
		String sql = "select * from pages where last_climb is not null";
		sqlStatement = conn.createStatement();
		sqlResultSet = sqlStatement.executeQuery(sql);
		while (sqlResultSet.next()) {
			int id = sqlResultSet.getInt("id");
			pages.put(id, new PageInfo(id, 
					sqlResultSet.getString("url"), 
					sqlResultSet.getString("title"))
			);
		}
		return pages;
	}
	
	public Map<Integer, ArrayList<Integer>> getRelationships() throws SQLException {
		Map<Integer, ArrayList<Integer>> relationships = new HashMap<Integer, ArrayList<Integer>>();
		String sql = "select * from relationships order by parent_page_id";
		sqlStatement = conn.createStatement();
		sqlResultSet = sqlStatement.executeQuery(sql);
		int parent_page_id = -1;
		ArrayList<Integer> links = new ArrayList<Integer>();
		if (sqlResultSet.next()) {
			// first row
			parent_page_id = sqlResultSet.getInt("parent_page_id");
			links.add(sqlResultSet.getInt("child_page_id"));
			// following rows
			while (sqlResultSet.next()) {
				int id = sqlResultSet.getInt("parent_page_id");
				if (id != parent_page_id) {
					relationships.put(parent_page_id, links);
					links = new ArrayList<Integer>();
					parent_page_id = id;
				}
				links.add(sqlResultSet.getInt("child_page_id"));
			}
			if (!links.isEmpty()) {
				relationships.put(parent_page_id, links);
			}
		}
		return relationships;
	}
	
	public void updatePageRank(Map<Integer, PageInfo> pages) throws SQLException {
		for (Entry<Integer, PageInfo> pageEntry : pages.entrySet()) {
			int id = pageEntry.getKey();
			double pr = pageEntry.getValue().pagerank;
			String sql = "update pages set pagerank=?, last_calculate=current_timestamp() where id=?";
	        sqlPreparedStatement = conn.prepareStatement(sql);
	        sqlPreparedStatement.setDouble(1, pr);
	        sqlPreparedStatement.setInt(2, id);
	        sqlPreparedStatement.executeUpdate();
		}
	}
	
	public void close() {
		try {
			if (sqlStatement != null) sqlStatement.close();
			if (sqlPreparedStatement != null) sqlPreparedStatement.close();
			if (sqlResultSet != null) sqlResultSet.close();
			if (conn != null) conn.close();
		} catch (SQLException e) {
			System.out.println("Error closing database connection:" + e.toString());
		}
	}
	
}
