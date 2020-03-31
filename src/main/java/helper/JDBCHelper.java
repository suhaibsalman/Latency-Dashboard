package helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCHelper
{
	private static final String URL = "URL";
	private static final String USERNAME = "USERNAME";
	private static final String PASSWORD = "PASSWORD";
	private static final String DRIVER_NAME = "DRIVER_NAME";
	
	private static Connection connection;
	
	private static JDBCConstants jdbcConstants = new JDBCConstants();

	static {
		try {
			Class.forName(jdbcConstants.getPram(DRIVER_NAME));
		} catch (ClassNotFoundException e) {
			System.out.println("Driver class not found");
		}
	}

	public static Connection getConnection() throws SQLException {
		connection = DriverManager.getConnection(jdbcConstants.getPram(URL), jdbcConstants.getPram(USERNAME), jdbcConstants.getPram(PASSWORD));
		return connection;
	}

	public static void closeConnection(Connection con) throws SQLException {
		if (con != null) {
			con.close();
		}
	}

	public static void closePrepaerdStatement(PreparedStatement stmt) throws SQLException {
		if (stmt != null) {
			stmt.close();
		}
	}

	public static void closeResultSet(ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
		}
	}

}
