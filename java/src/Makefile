main = Messenger_GUI
sources = $(main).java GhostText.java ChatPane.java ChatBarResponder.java Chat.java Messenger.java Command.java MessengerUser.java Message.java
objects = $(sources:.java=.class)
#$(sources:.java=.class:./=../classes/)
flags = -cp .:../lib/pg73jdbc3.jar

$(main).class: $(objects) $(main).java
	javac $(flags) $(main).java
	cp *.class ../classes/
	
run: $(main).class
	java $(main)

%.class: %.java
	javac $(flags) $<

clean:; rm *.class ../classes/*.class *~

strip:; strip $(main)
