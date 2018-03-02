package tw.cchi.prdemo.climber;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import tw.cchi.prdemo.struct.PageInfo;

class DBOperation {
	
	private Connection conn = null; // MySQL connection object
	private Statement sqlStatement = null; 
	private PreparedStatement sqlPreparedStatement = null;
	private ResultSet sqlResultSet = null;
	
	public DBOperation(Connection conn) {
		this.conn = conn;
	}
	
	public int getPageIdByUrl(String url, int pageDepth, boolean climbed) throws SQLException { // if not found, insert new row
		sqlPreparedStatement = conn.prepareStatement("select * from pages where url=?");
		sqlPreparedStatement.setString(1, url);
		sqlResultSet = sqlPreparedStatement.executeQuery();
		if (sqlResultSet.next() == false) { // no data found
			// insert new data row
			String sql;
			if (climbed)
				sql = "insert into pages (url, climb_depth, last_climb) values (?, ?, current_timestamp())";
			else
				sql = "insert into pages (url, climb_depth) values (?, ?)";
			sqlPreparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			sqlPreparedStatement.setString(1, url);
			sqlPreparedStatement.setInt(2, pageDepth);
			sqlPreparedStatement.executeUpdate();
			sqlResultSet = sqlPreparedStatement.getGeneratedKeys(); sqlResultSet.next();
		}
		return sqlResultSet.getInt(1);
	}
	
	public void updatePageInfo(int id, String title, String content) throws SQLException {
		String sql = "update pages set title=?, content=?, last_climb=current_timestamp() where id=?";
        sqlPreparedStatement = conn.prepareStatement(sql);
        sqlPreparedStatement.setString(1, title);
        sqlPreparedStatement.setString(2, content);
        sqlPreparedStatement.setInt(3, id);
        sqlPreparedStatement.executeUpdate();
	}
	
	/*public int getRelationshipId(int parentPageId, int childPageId) throws SQLException {
		String sql = "select * from relationships where parent_page_id=? and child_page_id=?";
		sqlPreparedStatement = conn.prepareStatement(sql);
		sqlPreparedStatement.setInt(1, parentPageId);
		sqlPreparedStatement.setInt(2, childPageId);
		sqlResultSet = sqlPreparedStatement.executeQuery();
		if (sqlResultSet.next() == false) { // no data found
			// insert new data row
			sql = "insert into relationships (parent_page_id, child_page_id) values (?, ?)";
			sqlPreparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			sqlPreparedStatement.setInt(1, parentPageId);
			sqlPreparedStatement.setInt(2, childPageId);
			sqlPreparedStatement.executeUpdate();
			sqlResultSet = sqlPreparedStatement.getGeneratedKeys(); sqlResultSet.next();
		}
		return sqlResultSet.getInt(1);
	}*/
	
	public ArrayList<Integer> getChildIdsByParentId(int parentPageId) throws SQLException {
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "select * from relationships where parent_page_id=?";
		sqlPreparedStatement = conn.prepareStatement(sql);
		sqlPreparedStatement.setInt(1, parentPageId);
		sqlResultSet = sqlPreparedStatement.executeQuery();
		while (sqlResultSet.next()) {
			list.add(sqlResultSet.getInt("child_page_id"));
		}
		return list;
	}
	
	public int insertRelationship(int parentPageId, int childPageId) throws SQLException {
		// insert new data row
		String sql = "insert into relationships (parent_page_id, child_page_id) values (?, ?)";
		sqlPreparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		sqlPreparedStatement.setInt(1, parentPageId);
		sqlPreparedStatement.setInt(2, childPageId);
		sqlPreparedStatement.executeUpdate();
		sqlResultSet = sqlPreparedStatement.getGeneratedKeys(); sqlResultSet.next();
		return sqlResultSet.getInt(1);
	}
	
	/*public int getPagesCount() throws SQLException {
		String sql = "select count(id) from pages";
		sqlStatement = conn.createStatement();
		sqlResultSet = sqlStatement.executeQuery(sql); sqlResultSet.next();
		return sqlResultSet.getInt(1);
	}*/
	
	public int getRelationshipsCount() throws SQLException {
		String sql = "select count(id) from relationships";
		sqlStatement = conn.createStatement();
		sqlResultSet = sqlStatement.executeQuery(sql); sqlResultSet.next();
		return sqlResultSet.getInt(1);
	}
	
	/*public int getUnclimbedPagesCount() throws SQLException {
		String sql = "select count(id) from pages where last_climb = null";
		sqlStatement = conn.createStatement();
		sqlResultSet = sqlStatement.executeQuery(sql); sqlResultSet.next();
		return sqlResultSet.getInt(1);
	}*/
	
	// return List<PageInfo> not included page content field to save memory
	public ArrayList<PageInfo> getPagesForClimbing(int limit) throws SQLException {
		conn.setAutoCommit(false); //transaction block start
		
		// set climbing field
		String selectSql = "select * from pages where last_climb is null and climbing is false order by id limit 0,?";
		sqlPreparedStatement = conn.prepareStatement(selectSql);
		sqlPreparedStatement.setInt(1, limit);
		sqlResultSet = sqlPreparedStatement.executeQuery();
		
		ArrayList<PageInfo> pagesInfo = new ArrayList<PageInfo>();
		String updateSql = "update pages set climbing=true where id=?";
		while (sqlResultSet.next()) {
			// store data for response
			pagesInfo.add(new PageInfo(sqlResultSet.getInt("id"), 
										sqlResultSet.getString("url"), 
										sqlResultSet.getString("title"),
										"", // content
										sqlResultSet.getInt("climb_depth"),
										sqlResultSet.getDouble("pagerank")));
			// set climbing
			sqlPreparedStatement = conn.prepareStatement(updateSql);
			sqlPreparedStatement.setInt(1, sqlResultSet.getInt(1));
			sqlPreparedStatement.executeUpdate();
		}
		
		conn.commit(); //transaction block end
		conn.setAutoCommit(true);
		
		return pagesInfo;
	}
	
	public void setClimbed(int pageId) throws SQLException {
		String sql = "update pages set climbing=false, last_climb=current_timestamp() where id=?";
		sqlPreparedStatement = conn.prepareStatement(sql);
		sqlPreparedStatement.setInt(1, pageId);
		sqlPreparedStatement.executeUpdate();
	}
	
	public void setClimbFailed(int pageId) throws SQLException {
		String sql = "update pages set climbing=false, climb_failed=true where id=?";
		sqlPreparedStatement = conn.prepareStatement(sql);
		sqlPreparedStatement.setInt(1, pageId);
		sqlPreparedStatement.executeUpdate();
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
