package net.jplugin.core.das.route.impl.conn;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import net.jplugin.core.das.api.DataSourceFactory;
import net.jplugin.core.das.route.api.SqlHandleService;
import net.jplugin.core.das.route.api.TablesplitException;
import net.jplugin.core.das.route.impl.conn.mulqry.CombineStatementFactory;
import net.jplugin.core.das.route.impl.conn.mulqry.CombinedSqlParser;

public class RouterStatement extends EmptyStatement {
	protected RouterConnection connection;
	protected ExecuteResult executeResult = new ExecuteResult();

	public static Statement create(RouterConnection conn) {
		RouterStatement cs = new RouterStatement();
		cs.connection = conn;
		return cs;
	}

	@Override
	public final ResultSet executeQuery(String sql) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		ResultSet rs = stmt.executeQuery(sql);
		
		return rs;
	}

	@Override
	public final int executeUpdate(String sql) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		int cnt = stmt.executeUpdate(sql);
		
		return cnt;
	}

	@Override
	public final  boolean execute(String sql) throws SQLException {
		Result rr = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = rr.statement;
		sql = rr.resultSql;
		boolean r = stmt.execute(sql);
		
		return r;
	}

	@Override
	public final  ResultSet getResultSet() throws SQLException {
		return executeResult.getResult();
	}

	@Override
	public final  int getUpdateCount() throws SQLException {
		return executeResult.getUpdateCount();
	}

	@Override
	public final  Connection getConnection() throws SQLException {
		return connection;
	}

	@Override
	public final  int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		int cnt = stmt.executeUpdate(sql, autoGeneratedKeys);
		
		return cnt;
	}

	@Override
	public  final int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		int cnt = stmt.executeUpdate(sql, columnIndexes);
		
		return cnt;
	}

	@Override
	public final  int executeUpdate(String sql, String[] columnNames) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		int cnt = stmt.executeUpdate(sql, columnNames);
		
		return cnt;
	}

	@Override
	public  final boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		boolean b = stmt.execute(sql, autoGeneratedKeys);
		return b;
	}

	@Override
	public  final boolean execute(String sql, int[] columnIndexes) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		boolean b = stmt.execute(sql, columnIndexes);
		return b;
	}

	@Override
	public final  boolean execute(String sql, String[] columnNames) throws SQLException {
		Result r = genTargetNotPreparedStatement(connection, sql);
		Statement stmt = r.statement;
		sql = r.resultSql;
		boolean b = stmt.execute(sql, columnNames);
		return b;
	}

	@Override
	public final  boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public  final void close() throws SQLException {
		this.executeResult.clear();
	}
	
	@Override
	public  final boolean getMoreResults() throws SQLException {
		return this.executeResult.getMoreResults();
	}

	/**
	 * 这个返回用来执行带sql参数的情况
	 * @return
	 * @throws SQLException
	 */
	public  Result genTargetNotPreparedStatement(RouterConnection conn,String sql) throws SQLException {
		if (sql==null) throw new TablesplitException("No sql found");
		SqlHandleResult shr = SqlHandleService.INSTANCE.handle(conn,sql);
		
		String targetDataSourceName = shr.getTargetDataSourceName();
		Statement stmt;
		if (CombinedSqlParser.SPANALL_DATASOURCE.equals(targetDataSourceName)){
			stmt = CombineStatementFactory.create(connection);
		}else{
			DataSource tds = DataSourceFactory.getDataSource(targetDataSourceName);
			if (tds==null) 
				throw new TablesplitException("Can't find target datasource."+shr.getTargetDataSourceName());
			stmt = tds.getConnection().createStatement();
		}
		Result result = new Result();
		result.statement = stmt;
		this.executeResult.set(result.statement);
		result.resultSql = shr.resultSql;
		return result;
	}
	
	public static class Result{
		Statement statement;
		String resultSql;
	}
}
