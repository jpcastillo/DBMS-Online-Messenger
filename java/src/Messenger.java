/*
* By John Castillo and Daniel Pasillas
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
import java.util.*;


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
	/*public void executeUpdate (String sql) throws SQLException {
	  // creates a statement object
	  Statement stmt = this._connection.createStatement();

	  // issues the update instruction
	  stmt.executeUpdate (sql);

	  // close the instruction
	  stmt.close ();
	}//end executeUpdate*/

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
	   ** obtains the metadata object for the returned result set. The metadata
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

	/*
		Method to return as an Array the results of a query.
		Input: query (the sql query to execute), cols (a list of attributes to include in csv element of array)
	*/
	public String[] executeQueryArray (String query, String attrs) throws SQLException {
		// initialize our return list
		List<String> ret_list = new ArrayList<String>();
		// create a list of column names by splitting attributes string
		List<String> cols = new ArrayList<String>(Arrays.asList(attrs.split(",")));
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		String tmp = "";
		// iterates through the result set and build strings.
		while(rs.next()){
			// loop over list of columns/attributes and get the value of each
			// we may then append these values to a csv string
			for (String str: cols) {
				String tmp2 = rs.getString(str);
				tmp2 = (tmp2==null) ? "" : tmp2;
				tmp += tmp2.trim() + "\n";
			}
			// remove the extra delimiter at the end
			tmp = tmp.substring(0,tmp.length()-1);
			// add this csv string to our return list
			ret_list.add(tmp);
			tmp = "";
		}//end while
		stmt.close();
		return ret_list.toArray(new String[0]);
	}

	public String executeQueryStr (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);
		rs.next();

		String retVal = rs.getString("retVal");
		stmt.close();
		return retVal;
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
	* Creates a new user with provided login, password, phoneNum, and status
	* returns empty string on success. else error message.
	**/
	public static String CreateUser(Messenger esql,String un,String pw,String phone,String status) {
		try {
			String query = String.format("select newAccount('%s','%s','%s','%s') as retVal;",un,pw,phone,status);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
	* Check log in credentials for an existing user
	* Returns empty string on success, else error string.
	**/
	public static String LogIn(Messenger esql, String un, String pw) {
		try {
			String query = String.format("select login('%s','%s') as retVal;", un, pw);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Log user out.
		Returns empty string on success. Else error string.
	*/
	public static String Logout(Messenger esql, String un) {
		try {
			String query = String.format("select logout('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Add userB to userA's Chat
		Returns empty string on success. Else error string.
	*/
	public static String AddToChat(Messenger esql, int chatId, String userA, String userB) {
		try {
			String query = String.format("select editChatList(%d,'%s','%s',1) as retVal;",chatId,userA,userB);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Remove userB from userA's Chat
		Returns empty string on success. Else error string.
	*/
	public static String RemoveFromChat(Messenger esql, int chatId, String userA, String userB) {
		try {
			String query = String.format("select editChatList(%d,'%s','%s',0) as retVal;",chatId,userA,userB);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Add userB to userA's Contact List
	*/
	public static String AddToContact(Messenger esql, String userA, String userB) {
		try {
			String query = String.format("select addToContactBlock('%s','%s',1) as retVal;", userA,userB);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Add userB to userA's Block List
	*/
	public static String AddToBlock(Messenger esql, String userA, String userB) {
		try {
			String query = String.format("select addToContactBlock('%s','%s',0) as retVal;", userA,userB);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Removes userB from userA's contact list.
		Returns empty string on success. else error string.
	*/
	public static String DelFromContacts(Messenger esql, String userA, String userB) {
		try {
			String query = String.format("select delFromContactBlock('%s','%s',1) as retVal;", userA,userB);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Removes userB from userA's block list.
		Returns empty string on success. else error string.
	*/
	public static String DelFromBlocks(Messenger esql, String userA, String userB) {
		try {
			String query = String.format("select delFromContactBlock('%s','%s',0) as retVal;", userA,userB);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Returns a comma delimited list of contact list members
		for the specified user.
	*/
	public static String ListContacts(Messenger esql, String un) {
		try {
			String query = String.format("select listContactBlock('%s',%d) as retVal;", un, 1);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Returns a comma delimited list of block list members
		for the specified user.
	*/
	public static String ListBlocks(Messenger esql, String un) {
		try {
			String query = String.format("select listContactBlock('%s',%d) as retVal;", un, 0);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Returns a comma delimited list of chat members
		for the specified chat id.
	*/
	public static String ListChatMembers(Messenger esql, int chatID) {
		try {
			String query = String.format("select chatListMembers(%d) as retVal;", chatID);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Returns a comma delimited list of chat ids
		for the specified user.
	*/
	public static String ListUserChats(Messenger esql, String un) {
		try {
			String query = String.format("select userChatList('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
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
	public static String NewMessage(Messenger esql, String msgBody, String selfDestr, String senderUn, int chatID, String recipientUn) {
		try {
			String query = String.format("select newMessage('%s','%s','%s',%d,'%s') as retVal;", msgBody, selfDestr, senderUn, chatID, recipientUn);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Deletes a specified message from notification and messages if possible.
		Input message owner (un), message id (msgId)
		Returns empty string on success. else error string.
	*/
	public static String DeleteMessage(Messenger esql, String un, int msgId) {
		try {
			String query = String.format("select delMessage('%s',%d) as retVal;", un, msgId);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Returns a String array of messages for a given chat, sorted by msg_timestamp asc
		Returns null on failure
		Input: chatID, fromTimeStamp (YYYY-MM-DD HH:MI:SS)
		Note: fromTimeStamp will be ignored if it is empty string, thus return all history
	*/
	public static String[] GetChatHistory(Messenger esql, int chatID, String fromTimeStamp) {
		try {
			String query;
			if (fromTimeStamp.length() > 0) {
				//query = String.format("select m.* from chat_list cl join message m on cl.chat_id = m.chat_id where cl.chat_id = '%d' and m.msg_timestamp > '%s' order by m.msg_timestamp asc;",chatID,fromTimeStamp); 
				query = String.format("select distinct(m.msg_id),m.sender_login,m.msg_timestamp,m.msg_text,ma.media_type,ma.url from chat_list cl join message m on cl.chat_id = m.chat_id left join media_attachment ma on m.msg_id = ma.msg_id where cl.chat_id = '%d' and m.msg_timestamp > '%s' order by m.msg_timestamp asc;",chatID,fromTimeStamp);
			}
			else {
				//query = String.format("select m.* from chat_list cl join message m on cl.chat_id = m.chat_id where cl.chat_id = '%d' order by m.msg_timestamp asc;",chatID);
				query = String.format("select distinct(m.msg_id),m.sender_login,m.msg_timestamp,m.msg_text,ma.media_type,ma.url from chat_list cl join message m on cl.chat_id = m.chat_id left join media_attachment ma on m.msg_id = ma.msg_id where cl.chat_id = %d order by m.msg_timestamp asc;",chatID);
				//System.out.println("Q: "+query);
			}

			String column_names = "msg_id,sender_login,msg_timestamp,msg_text,media_type,url";
			String[] results = esql.executeQueryArray(query,column_names);
			return results;
		}
		catch(SQLException e) {
			//return e.getMessage();
			System.out.println("E: "+e.getMessage());
			return null;
		}
	}//end

	/*
		Returns string of notifications for user 'un'
		Error string if error
		msg_id\nchat_id\nsender_login\nmsg_timestamp [ |[(^#^)]| ... ]
	*/
	public static String ReadNotifications(Messenger esql, String un) {
		try {
			String query = String.format("select readNotifications('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Removes messages listed in msgIdList from user 'un' notificatoins
		Returns Empty String on Success and Error string else
	*/
	public static String MarkReadNotifications(Messenger esql, String un, String msgIdList) {
		try {
			String query = String.format("select markReadNotifications('%s','%s') as retVal;", un, msgIdList);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Deletes user account and all associations.
		Per the project specifications, cannot delete account if
		the user is owner of chats or attachments.
		Returns empty string on success and error string on failure.
	*/
	public static String DeleteAccount(Messenger esql, String un) {
		try {
			String query = String.format("select deleteAccount('%s') as retVal;", un);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Updates a user's status
		Returns empty string on success and error string on failure.
	*/
	public static String UpdateStatus(Messenger esql, String un, String status) {
		try {
			String query = String.format("select updateStatus('%s','%s') as retVal;", un, status);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

	/*
		Updates a user's message
		Returns empty string on success and error string on failure.
	*/
	public static String UpdateMessage(Messenger esql, String un, int msgId, String msgText) {
		try {
			String query = String.format("select updateMessage('%s',%d,'%s') as retVal;", un, msgId, msgText);
			String retVal = esql.executeQueryStr(query);

			return retVal;
		}
		catch(Exception e) {
			//return e.getMessage();
			return null;
		}
	}//end

}//end Messenger
