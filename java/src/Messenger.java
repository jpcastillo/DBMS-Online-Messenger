/*
* This Messenger Class is a Modification of:
*
* Template JAVA User Interface
* =============================
*
* Database Management Systems
* Department of Computer Science & Engineering
* University of California - Riverside
*
* Target DBMS: 'Postgres 8.1.23'
*
*/

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


/**
* This class defines a simple embedded SQL utility class that is designed to
* work with PostgreSQL JDBC drivers.
*
*/
public class Messenger {

	// reference to physical database connection.
	private Connection _connection = null;

	/*
	*	Default constructor for Messenger
	*/
	public Messenger() {
		//
	}
	/**
	* Creates a new instance of Messenger
	*
	* @param hostname the MySQL or PostgreSQL server hostname
	* @param database the name of the database
	* @param username the user name used to login to the database
	* @param password the user login password
	* @throws java.sql.SQLException when failed to make a connection.
	*/
	public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

		System.out.print("Connecting to database...");
		try {
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
			this._connection = DriverManager.getConnection(url, user, passwd);
		}
		catch (Exception e) {
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
			System.out.println("Make sure you started postgres on this machine");
			System.exit(-1);
		}//end catch
	}//end Messenger

	/**
	* Method to execute an update SQL statement.  Update SQL instructions
	* includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	*
	* @param sql the input SQL string
	* @throws java.sql.SQLException when update failed
	*/
	public void executeUpdate (String sql) throws SQLException {
	  // creates a statement object
	  Statement stmt = this._connection.createStatement();

	  // issues the update instruction
	  stmt.executeUpdate (sql);

	  // close the instruction
	  stmt.close ();
	}//end executeUpdate

	/**
	* Method to execute an input query SQL instruction (i.e. SELECT).  This
	* method issues the query to the DBMS and outputs the results to
	* standard out.
	*
	* @param query the input query string
	* @return the number of rows returned
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public int executeQueryAndPrintResult (String query) throws SQLException {
	  // creates a statement object
	  Statement stmt = this._connection.createStatement ();

	  // issues the query instruction
	  ResultSet rs = stmt.executeQuery (query);

	  /*
	   ** obtains the metadata object for the returned result set.  The metadata
	   ** contains row and column info.
	   */
	  ResultSetMetaData rsmd = rs.getMetaData ();
	  int numCol = rsmd.getColumnCount ();
	  int rowCount = 0;

	  // iterates through the result set and output them to standard out.
	  boolean outputHeader = true;
	  while (rs.next()){
	 if(outputHeader){
		for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
		}
		System.out.println();
		outputHeader = false;
	 }
		 for (int i=1; i<=numCol; ++i)
			System.out.print (rs.getString (i) + "\t");
		 System.out.println ();
		 ++rowCount;
	  }//end while
	  stmt.close ();
	  return rowCount;
	}//end executeQuery

	/**
	* Method to execute an input query SQL instruction (i.e. SELECT).  This
	* method issues the query to the DBMS and returns the number of results
	*
	* @param query the input query string
	* @return the number of rows returned
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public int executeQuery (String query) throws SQLException {
	   // creates a statement object
	   Statement stmt = this._connection.createStatement ();

	   // issues the query instruction
	   ResultSet rs = stmt.executeQuery (query);

	   int rowCount = 0;

	   // iterates through the result set and count number of results.
	   if(rs.next()){
		  rowCount++;
	   }//end while
	   stmt.close ();
	   return rowCount;
	}

	public String executeQueryStr (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery (query);
		rs.next();

		String retVal = rs.getString("retVal");
		stmt.close();
		return retVal;
	}

	/**
	* Method to fetch the last value from sequence. This
	* method issues the query to the DBMS and returns the current 
	* value of sequence used for autogenerated keys
	*
	* @param sequence name of the DB sequence
	* @return current value of a sequence
	* @throws java.sql.SQLException when failed to execute the query
	*/
	public int getNextSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select nextval('%s')", sequence));
		if (rs.next())
			return rs.getInt(1);
		return -1;
	}

	/**
	* Method to close the physical connection if it is open.
	*/
	public void cleanup() {
		try{
			if (this._connection != null) {
				this._connection.close ();
			}//end if
		}
		catch (SQLException e) {
			;// ignored.
		}//end try
	}//end cleanup

	/*
	* Creates a new user with privided login, password and phoneNum
	* An empty block and contact list would be generated and associated with a user
	**/
	public static String CreateUser(Messenger esql,String un,String pw,String phone,String status) {
		try {
			String query = String.format("select newAccount('%s','%s','%s','%s') as retVal;",un,pw,phone,status);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end
	
	/*
	* Check log in credentials for an existing user
	* @return User login or null is the user does not exist
	**/
	public static String LogIn(Messenger esql, String un, String pw) {
		try {
			String query = String.format("select login('%s','%s') as retVal;", un, pw);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end

	public static String Logout(Messenger esql, String un) {
		try {
			String query = String.format("select logout('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end

	public static void AddToContact(Messenger esql) {
	  // Your code goes here.
	  // ...
	  // ...
	}//end

	public static void AddToBlock(Messenger esql) {
	  // Your code goes here.
	  // ...
	  // ...
	}//end

	public static void ListContacts(Messenger esql) {
	  // Your code goes here.
	  // ...
	  // ...
	}//end

	public static String ListChatMembers(Messenger esql, int chatID) {
		try {
			String query = String.format("select chatListMembers(%d) as retVal;", chatID);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end

	public static String ListUserChats(Messenger esql, String un) {
		try {
			String query = String.format("select userChatList('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end

	/*
		INPUT: MessageText, SelfDestructTimeStamp, SenderLogin, ChatID, RecipientLogin
		If ChatID < 0
			RecipientLogin may NOT be an EmptyString
			Function will create a new Chat and Add RecipientLogin to that Chat
			Notifications are sent out accordingly
		If ChatID >= 0
			RecipientLogin may be an EmptyString
			SenderLogin MUST already be a member of Chat
			Notification of new message is sent to existing Chat members
	*/
	public static void NewMessage(Messenger esql, String msgBody, String selfDestr, String senderUn, Int chatID, String recipientUn) {
		try {
			String query = String.format("select newMessage('%s','%s','%s',%d,'%s') as retVal;", msgBody, selfDestr, senderUn, chatID, recipientUn);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end 

	public static void ReadNotifications(Messenger esql) {
	  // Your code goes here.
	  // ...
	  // ...
	}//end

	public static String DeleteAccount(Messenger esql, String un) {
		try {
			String query = String.format("select deleteAccount('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);
			//System.out.println("retVal: "+retVal);

			return retVal;
		}
		catch(Exception e) {
			//System.err.println (e.getMessage());
			return e.getMessage();
		}
	}//end Query6

}//end Messenger
