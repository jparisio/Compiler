
# Compilers Development

Made By ustin Parisio, Gavin Eugenio, and Aidan Wylie

A compiler implementation for the C Minus programming language using JFlex and CUP. Performs syntactic and semantic analysis, and generates abstract syntax trees, symbol tables, and TM assembly code.

Used the Warmup Package for this assignment.


## Build and Test Program:

To build the scanner and parser, type "make" in the "Parser" directory, which will generate the executables "Scanner" and "CM".

### Parser

#### Code Generation

To test compilation for source code like "booltest.cm", type 

```
$ java -cp /usr/share/java/cup.jar:. CM -c ../booltest.cm
```

A ".tm" file will be generated in the root directory containing the associated assembly code.

#### Semantic Analysis

To test the parser for source code like "booltest.cm", type 

```
$ java -cp /usr/share/java/cup.jar:. CM -s ../booltest.cm
```

A ".sym" file will be generated in the root directory containing the associated symbol table.

#### Syntactic Analysis

Alternatively, to make the compiler perform syntactic analysis, type

```
$ java -cp /usr/share/java/cup.jar:. CM -a ../booltest.cm
```

A ".abs" file will be generated in the root directory containing the associated abstract syntax tree.

To rebuild the parser, type "make clean" and type "make" again.

All the abstract syntax tree structures are defined under the directory "absyn"

Methods for showing a syntax tree is implemented by the visitor pattern in "ShowTreeVisitor.java".

Methods for traversing the syntax tree to perform type checking is implemented by the visitor pattern in "SemanticAnalyzer.java".

Methods for generating the assembly code is implemented by the visitor pattern in "CodeGenerator.java".

### Scanner

To test the scanner for source code like "booltest.cm", type 

```
$ java -cp /usr/share/java/cup.jar:. Scanner < ../booltest.cm
```

The token sequence will be displayed on the screen. 

To save the token sequence to an output file "booltest.out", just 
type 

```
$ java -cp /usr/share/java/cup.jar:. Scanner < ../booltest.cm > ../booltest.out
```

To rebuild the scanner, type "make clean" and type "make" again.



# Compiler
