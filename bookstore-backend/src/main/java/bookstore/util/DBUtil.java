package bookstore.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static HikariDataSource dataSource;

    static { 
        try {
            HikariConfig config = new HikariConfig();
            
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
            
            config.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=bookstore;trustServerCertificate=true");
            config.setUsername("owner"); 
            config.setPassword("12345"); 
            
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000); 

            dataSource = new HikariDataSource(config);
            
        } catch (Exception e) {
            System.err.println("HikariCP Initialization Failed!");
            e.printStackTrace();
            throw new RuntimeException("Database initialization error!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); 
    }
    
    public static void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.setAutoCommit(true); 
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}