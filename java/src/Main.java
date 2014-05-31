/*
	Main Class is a driver class of Messenger
	implementation
*/

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main extends Messenger {

	// handling the keyboard inputs through a BufferedReader
	// This variable can be global for convenience.
	static BufferedReader in = new BufferedReader(
								new InputStreamReader(System.in));
	/**
	* The main execution method
	*
	* @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	*/
	public static void main (String[] args) {
		/* 
			NOTE:
			Command line arguement validation of args is done
			by the startup bash script.
		*/
	  
	  Greeting();
	  Messenger esql = null;
	  try{
		 // use postgres JDBC driver.
		 Class.forName ("org.postgresql.Driver").newInstance ();
		 // instantiate the Messenger object and creates a physical
		 // connection.
		 String dbname = args[0];
		 String dbport = args[1];
		 String user = args[2];
		 String passwd = args[3];
		 esql = new Messenger (dbname, dbport, user, passwd);

		 boolean keepon = true;
		 while(keepon) {
			// These are sample SQL statements
			System.out.println("MAIN MENU");
			System.out.println("---------");
			System.out.println("1. Create user");
			System.out.println("2. Log in");
			System.out.println("3. List Chat Members");
			System.out.println("4. List User Chats");
			System.out.println("5. Delete User Account");
			System.out.println("6. Log out");
			System.out.println("9. < EXIT");
			String authorisedUser = null;
			switch (readChoice()){
				case 1:
					System.out.println(esql.CreateUser(esql,"Torcherist3","jc7791","+1(626)532-2275","Online"));
				break;
				case 2:
					authorisedUser = esql.LogIn(esql,"Torcherist3","jc77912");
					System.out.println(authorisedUser);
				break;
				case 3:
					System.out.println(esql.ListChatMembers(esql,1));
				break;
				case 4:
					System.out.println(esql.ListUserChats(esql,"Norma"));
				break;
				case 5:
					System.out.println(esql.DeleteAccount(esql,"Karianne"));
				break;
				case 6:
					System.out.println(esql.Logout(esql,"Karianne2"));
				break;
				case 9:
					keepon = false;
				break;
				default:
					System.out.println("Unrecognized choice!");
				break;
			}//end switch
			if (authorisedUser != null) {
			  boolean usermenu = true;
			  while(usermenu) {
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add to contact list");
				System.out.println("2. Browse contact list");
				System.out.println("3. Write a new message");
				System.out.println("4. Read notification list");
				System.out.println(".........................");
				System.out.println("9. Log out");
				switch (readChoice()){
				   case 1: esql.AddToContact(esql); break;
				   case 2: esql.ListContacts(esql); break;
				   case 3: esql.NewMessage(esql,"Hello World!","2014-05-31 02:34:49","Torcherist3",0,""); break;
				   case 4: esql.ReadNotifications(esql); break;
				   case 9: usermenu = false; break;
				   default : System.out.println("Unrecognized choice!"); break;
				}
			  }
			}
		 }//end while
	  }catch(Exception e) {
		 System.err.println (e.getMessage ());
	  }finally{
		 // make sure to cleanup the created table and close the connection.
		 try{
			if(esql != null) {
			   System.out.print("Disconnecting from database...");
			   esql.cleanup ();
			   System.out.println("Done\n\nBye !");
			}//end if
		 }catch (Exception e) {
			// ignored.
		 }//end try
	  }//end try
	}//end main


	public static void Greeting(){
	  System.out.println(
		 "\n\n*******************************************************\n" +
		 "              User Interface      	               \n" +
		 "*******************************************************\n");
	}//end Greeting

	/*
	* Reads the users choice given from the keyboard
	* @int
	**/
	public static int readChoice() {
	  int input;
	  // returns only if a correct value is given.
	  do {
		 System.out.print("Please make your choice: ");
		 try { // read the integer, parse it and break.
			input = Integer.parseInt(in.readLine());
			break;
		 }catch (Exception e) {
			System.out.println("Your input is invalid!");
			continue;
		 }//end try
	  }while (true);
	  return input;
	}//end readChoice

}