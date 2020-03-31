package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import entity.LatencyDashboard;
import helper.JDBCHelper;

public class JDBCDaoImpl {
	public static final String INSERT_ORACLE_QUERY = "INSERT INTO LatencyDashbord(Date,SessionID,ServiceName,DiffWithLastRequest,Type,TransID) VALUES(?,?,?,?,?,?)";

	public static void insertLatencyRecord(LatencyDashboard latencyDashboard) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = JDBCHelper.getConnection();
			if (con == null) {
				System.out.println("Error getting the connection. Please check if the DB server is running");
				return;
			}
			con.setAutoCommit(false);
			ps = con.prepareStatement(INSERT_ORACLE_QUERY);
			
			ps.setString(1, latencyDashboard.getDate());
			ps.setString(2, latencyDashboard.getSessionID());
			ps.setString(3, latencyDashboard.getServiceName());
			ps.setString(4, latencyDashboard.getDiffWithLastRequest());
			ps.setString(5, latencyDashboard.getType());
			ps.setString(6, latencyDashboard.getTransID());

			ps.execute();
			System.out.println("insert => " + ps.toString());
			con.commit();

		} catch (SQLException e) {
			try {
				if (con != null) {
					con.rollback();
				}
			} catch (SQLException e1) {
				throw e1;
			}
			throw e;
		} finally {
			try {
				JDBCHelper.closePrepaerdStatement(ps);
				JDBCHelper.closeConnection(con);
			} catch (SQLException e) {
				throw e;
			}
		}

	}
}
