main = Messenger_GUI
sources = $(main).java GhostText.java ChatPane.java ChatBarResponder.java Chat.java Messenger.java Command.java MessengerUser.java Message.java Notification.java
objects = $(sources:.java=.class)
#$(sources:.java=.class:./=../classes/)
flags = -cp .:../lib/pg73jdbc3.jar

DB_NAME = mydb
PGPORT = 4956

$(main).class: $(objects) $(main).java
	javac $(flags) $(main).java
	mv *.class ../classes/
	
run: $(main).class
	java -cp ../classes:../lib/pg73jdbc3.jar $(main) $(DB_NAME) $(PGPORT) postgres ''

%.class: %.java
	javac $(flags) $<

clean:; rm *.class ../classes/*.class *~

strip:; strip $(main)
