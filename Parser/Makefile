JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP=cup
# JFLEX=~/Projects/jflex/bin/jflex
# CLASSPATH=-cp /Users/gavin/Downloads/java-cup-bin-11b-20160615/java-cup-11b.jar:.
# CUP=$(JAVA) $(CLASSPATH) java_cup.Main
all: CM.class

CM.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java CodeGenerator.java Scanner.java SemanticAnalyzer.java CM.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	#$(CUP) -dump -expect 3 cm.cup
	$(CUP) -expect 3 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *~
