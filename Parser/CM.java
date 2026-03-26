/*
  Created by: Group 9
  File Name: CM.java
*/

import java.io.*;
import java.nio.file.*;
import absyn.*;
   
class CM {

  public static boolean SHOW_TREE = false;
  public static boolean SHOW_TABLE = false;
  public static boolean SHOW_CODE = false;//set back to false later

  static public void main(String argv[]) throws FileNotFoundException {  

    PrintStream outfile = null;
    String filename = "";
    String pneuma[] = argv[argv.length-1].split("/"); //currently just assumes that the last one is the filename
    filename = pneuma[pneuma.length-1].split(".cm")[0];
    if (argv.length > 1) {
      for(int i =0; i< argv.length-1; i++){
      switch (argv[i]) {
        case "-a":
          SHOW_TREE = true;
          break;
        case "-s":
          SHOW_TABLE = true;
          break;
        case "-c":
          SHOW_CODE = true;
          break;
        default:
          System.out.println("\n\"" + argv[i] + "\" is not a valid option.");
          printUsage();
          return;
      }
      }
    } else {
      printUsage();
      return;
    }

    // check if file exists
    if (!new File(argv[argv.length -1]).exists())
    {
      System.out.println(argv[argv.length -1] + " does not exist.");
      return;
    }


    PrintStream console = System.out;

    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[argv.length -1])));
      // parse the tree
      Absyn result = (Absyn)(p.parse().value);
      // outputs AST
      if (SHOW_TREE && result != null) {
        outfile = new PrintStream(new File("../"+filename+ ".abs"));
        System.setOut(outfile);
        //  System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0, false); 
        System.setOut(console);
      }
      if ((SHOW_TABLE||SHOW_CODE) && result != null && p.valid == true) {
        outfile = new PrintStream(new File("../"+filename+ ".sym"));
        System.setOut(outfile);
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        System.out.println("Entering the global scope");
        result.accept(semanticAnalyzer, 0,false);
        semanticAnalyzer.printScopeVar();
        System.out.println("Leaving the global scope");
        if(!semanticAnalyzer.hasMain()){
          System.err.println("INVALID PROGRAM! Program is missing Main function");
        }
        System.setOut(console);
        if (SHOW_CODE){
          if(!SHOW_TABLE){//syntax analysis should not be created
            Files.deleteIfExists(Paths.get("../"+filename+".sym"));
          }
          if(semanticAnalyzer.semanticErrors){
            System.err.println("\n\nSemantic errors detected. Unable to build assembly code.\n\n");
          }
          else{
            outfile = new PrintStream(new File("../"+filename+ ".tm"));
            System.setOut(outfile);
            CodeGenerator codeGenerator = new CodeGenerator(filename);
            codeGenerator.visit(result);
            System.setOut(console);
          }
          
        }
      }  
      else if (SHOW_TABLE && result != null && p.valid == false) {
        System.err.println("\n\nSyntactic errors detected. Unable to build symbol table.\n\n");
      }

    
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }

  private static void printUsage()
  {
    System.out.println("\nUsage: java -cp /usr/share/java/cup.jar:. CM <option> <filename>");
    System.out.println("\nWhere <option> is the instruction given to the compiler to perform \n" +
      "compilation up to this step, and <filename> is the name of the source file.");
    System.out.println("\nHere is a list of available options:");
    System.out.println("  -a : perform syntactic analysis and output an abstract syntax tree (.abs)");
    System.out.println("  -s : perform type checking and output symbol tables (.sym)");
    System.out.println("  -c : compile and output TM assembly language code (.tm)");
    System.out.println("\n\n");
  }
}


