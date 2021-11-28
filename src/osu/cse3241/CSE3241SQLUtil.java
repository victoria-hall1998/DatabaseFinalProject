package osu.cse3241;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public final class CSE3241SQLUtil {

    /**
     * Utility class for SQL operations: sqlQuery(String sql, Connection
     * connection, String[] vals) printResultSet(ResultSet results)
     */

    private CSE3241SQLUtil() {
    }
	
	/**
     * Connects to the database if it exists, creates it if it does not, and returns the connection object.
     * 
     * @param databaseFileName the database file name
     * @return a connection object to the designated database
     */
    public static Connection initializeDB(String databaseFileName) {
    	/**
    	 * The "Connection String" or "Connection URL".
    	 * 
    	 * "jdbc:sqlite:" is the "subprotocol".
    	 * (If this were a SQL Server database it would be "jdbc:sqlserver:".)
    	 */
        String url = "jdbc:sqlite:" + databaseFileName;
        Connection conn = null; // If you create this variable inside the Try block it will be out of scope
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
            	// Provides some positive assurance the connection and/or creation was successful.
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("The connection to the database was successful.");
            } else {
            	// Provides some feedback in case the connection failed but did not throw an exception.
            	System.out.println("Null Connection");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("There was a problem connecting to the database.");
        }
        return conn;
    }
    
    /**
     * Runs a sql query using a prepared statement
     *
     * @param query
     *            - the query
     * @return - result set containing the results of a query
     */
    public static ResultSet sqlQuery(PreparedStatement query, boolean needResults) {
        ResultSet queryResults = null;
        try {
        	if (needResults) {
        		queryResults = query.executeQuery();
        	}
        	else {
        		query.execute();
        	}
    	} catch (SQLException e) {
			printThrowable(e);
		}
        return queryResults;
    }
    
    /**
     * Set up the prepared statement
     */
    public static PreparedStatement setUpPS(Connection conn, String sql, ArrayList<Object> vals) {
    	PreparedStatement query = null;
    	try {
            query = conn.prepareStatement(sql);
            for (int i = 0; i < vals.size(); i++) {
            	query.setObject(i+1, vals.get(i));
            }
            
        }
        catch (SQLException e) {
        	printThrowable(e);
        }
		return query;
    }

    /**
     * Prints a ResultSet from a sql query in tabular form
     *
     * @param result
     *            the result set to print
     */
    public static void printResultSet(ResultSet result) {
        try {
        	//NEED TO CHECK IF THE RESULTS ARE EMPTY
        	ResultSetMetaData meta = result.getMetaData();
            int columns = meta.getColumnCount();
            // print column headers
            for (int i = 1; i <= columns; i++) {
                String value = meta.getColumnName(i);
                System.out.print(value);
                if (i < columns) {
                    System.out.print(",\t");
                }
            }
            System.out.println();
            // print data
            while (result.next()) {
                for (int i = 1; i <= columns; i++) {
                    System.out.print(result.getString(i));
                    if (i < columns) {
                    	System.out.print(",\t");
                    }
                }
                System.out.println();
            }
        }
        catch (SQLException e) {
        	printThrowable(e);
        }
    }
    
    /**
     * IO for error/exception handling
     *
     * @param e
     *            - exception or error to handle
     */
    public static void printThrowable(Throwable e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Queries the database and prints the results when the user gives an input.
     *
     * @param conn
     *            a connection object
     * @param sql
     *            a SQL statement that returns rows This query is written with
     *            the Statement class, typically used for static SQL SELECT
     *            statements
     */
    public static void sqlQuerySearchAndPrint(PreparedStatement ps) {
        ResultSet rs = sqlQuery(ps, true);
        printResultSet(rs);
    }
    
    /**
     * Converts string date to sql date object
     *
     * @param format
     *            {String} string format of the date
     * @param dateString
     *            {String} the string containing the date to be parsed
     * @returns {java.sql.Date} Date object
     */
    public static java.sql.Date strToDate(String format, String dateString) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        java.util.Date date = null;
        try {
            date = sdf1.parse(dateString);
        } catch (ParseException e) {
            printThrowable(e);
        }
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        return sqlDate;
    }
}
