# DBMS Online Messenger

DBMS Online Messenger is an instant messenging service developed at the University of California, Riverside that was built and tested on Linux and OS X. The features and abilities of this instand messenger are listed below.

  - Timed messages
  - Edit sent messages
  - Delete sent messages
  - Tab-to-complete commands ("leave" or "join" a channel for example)
  - Help menu displaying supported commands
  - Channel messages persist on remote server
  - PostgreSQL - stored procedures, indexes, foreign keys, primary keys, unique keys
  - Create / delete channels
  - Add friends to channels
  - Kick enemies from channels
  - Friends list (Add to and remove from)
  - Block list (Add to and remove from)
  - Hand over ownership of channel to another
  - Message statuses such as Read and Unread
  - Graphical User Interface (GUI)
  - Message notifications (Alerts visible in GUI)

** It is important to note that in the above list a "channel" is what is commonly known as a "chat room." This software uses the term "channel" instead.

### Demonstration
`YoutTube` [YouTube](https://youtu.be/GF6R70yiEfE)

![Demo GIF](https://raw.github.com/jpcastillo/DBMS-Online-Messenger/master/demo.gif)


### Version
1.0.0

### Tech

DBMS Online Messenger utilizes a number of languages and open-source tools:

* **Java 7** - used to program the user interface and communicate with remote server
* **Java Swing** - graphics library used for the GUI
* **PostgreSQL 8** - database management system to store and manipulate data
* **BASH** - scripting for database initialization/performance metrics and software execution
* **Makefile** - compilation rules

### Installation

You need to have an instance of PostgreSQL running and reachable. After this is accomplished we may initialize our database and load some test data sets. We must navigate to our scripts directory:

```sh
$ cd <path_to_code_dir>/sql/scripts
```

Now we can run our BASH script to build our database for us:

```sh
$ ./create_db.sh
```

It is important to note that this script makes use of some environment variables. These two variables must be named **PGPORT** and **DB_NAME**. To test the performance of the database against some test case queries with and without optimizations we can run the following script from the same directory.

```sh
$ ./measure.sh
```
Note that this script also makes use of those two variables mentioned above.

Moving on. Let's setup the GUI. We must first tell the Messenger what remote host to attempt connection with. This is done in Messenger.java. We must change this hostname "jpc.mine.nu" to whatever hostname or IP address you will be using:

```sh
$ cd <path_to_code_dir>/java/src
$ sed -i 's/jpc.mine.nu/<new_hostname>/g' Messenger.java
```
Now We must compile the source code. Make sure we navigated to the Makefile directory:

```sh
$ cd <path_to_code_dir>/java/src
```
And now we run make to compile DBMS Online Messenger. You should note that before GUI launch there is a check for PostgreSQL server connectivity and will not launch if this is not available.

```sh
$ make run
```
Note: The database name and PostgreSQL port are assigned as variables within the Makefile. It expects database to sucesfully validate user **postgres** with empty password. Feel free to change these values as needed.

### Development

This project was developed by John Castillo and Daniel Pasillas.

License
----

MIT
