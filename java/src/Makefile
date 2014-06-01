main = Messenger_GUI
sources = $(main).java GhostText.java 
objects = $(sources:.java=.class)
#$(sources:.java=.class:./=../classes/)
flags = 

$(main).class: $(objects) $(main).java
	javac $(main).java $(flags)
	cp *.class ../classes/
	
run: $(main).class
	java $(main)

%.class: %.java
	javac $< $(flags)

clean:; rm *.class ../classes/*.class *~

strip:; strip $(main)
