package net.blay09.balybot.impl.base.script;

import net.blay09.balybot.Database;
import org.intellij.lang.annotations.Language;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseBinding {

	private String lastError;

	public boolean execute(@Language("SQL") String sql) {
		try {
			Database.execute(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return false;
		}
	}

	public ResultSet executeQuery(@Language("SQL") String sql) {
		try {
			Statement stmt = Database.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return null;
		}
	}

	public boolean createTable(String tableName, boolean withId, String[] fields) {
		try {
			Database.createTable(tableName, withId, fields);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return false;
		}
	}

	public String getLastError() {
		return lastError;
	}

}
