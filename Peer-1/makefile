JFLAGS = -g
JC = javac
RM = rm -f

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	PeerInformation.java\
        Handlers.java\
   	PThread.java\
        Peer.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class