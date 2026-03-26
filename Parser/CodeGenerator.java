/*
  Created By: Group 9
  File Name: CodeGenerator.java
*/

import absyn.*;

public class CodeGenerator implements AbsynVisitor {
  /*
  Special registers:
  #define pc 7 - keep track of the current instruction during execution
  #define gp 6
  #define fp 5 - points to the current activation record
  #define ac 0
  #define ac1 1
 */
  private static final int pc = 7;
  private static final int gp = 6;
  private static final int fp = 5;
  private static final int ac0 = 0;
  private static final int ac1 = 1;

  private int emitLoc = 0;       /* points to the current instruction we are generating */
  private int highEmitLoc = 0;   /* points to the next available space */

  private int mainEntry = -1;    /* absolute address for main */
  private int globalOffset = 0;  /* next available loc after global frame */
  private int frameOffset = 0;   /* for handling local vars */
  private int tempOffset = -4; 

  private final int ofpFO = 0;   //use later (now)
  private final int retFO = -1;   
  private final int initFO = -2; /* first two locations reserved */


  public CodeGenerator(String filename) {
    System.out.println("* C-Minus Compilation to TM Code");
    //do we need dynamic code name? idk
    System.out.println("* File: " + filename + ".tm");
  }


  /*       PRELUDE       */
  private void emitPrelude() {
    emitComment("Standard prelude:");
    emitRM("LD", gp, 0, ac0, "load gp with maxaddress"); // reg[gp] = 1023
    emitRM("LDA", fp, 0, gp, "copy gp to fp"); // reg[fp] = 1023
    emitRM("ST", ac0, 0, ac0, "clear location 0"); // dMem[0] = 0
  }


  /*       FINALE       */
  private void emitFinale() {
    emitComment("Finale: call main and halt");
    //do we hardcode these?? idk yes
    // replace numbers with fp, pc, ac, etc. slide 31 week 11
    emitRM("ST", fp, globalOffset+ofpFO, fp, "push ofp");
    emitRM("LDA", fp, globalOffset, fp, "push frame");
    emitRM("LDA", ac0, ac1, pc, "load ac with ret ptr");
    emitRM_Abs("LDA", pc, mainEntry, "jump to main loc");
    emitRM("LD", fp, ofpFO, fp, "pop frame");
    emitComment("End of execution.");
    emitRO("HALT", 0, 0, 0, "");
    // done!
  }


  /*    Routines to generate different kinds of assembly instructions    */
  // (Register Only) instructions - halt,in,out,add,sub,mul,div
  // format: opcode r, s, t
  // HALT - stop exec
  // IN   - reg[r] <- read int from input
  // OUT  - reg[r] -> write to stdout
  // ADD  - reg[r] = reg[s] + reg[t]
  // SUB -  reg[r] = reg[s] - reg[t]
  // MUL -  reg[r] = reg[s] * reg[t]
  // DIV -  reg[r] = reg[s] / reg[t] (may generate ZERO_DIV)
  private void emitRO(String op, int r1, int r2, int r3, String comment) {
    System.out.printf("%3d:    %3s  %d,%d,%d \t%s\n", emitLoc++, op, r1, r2, r3, comment);
    if (emitLoc > highEmitLoc) highEmitLoc = emitLoc;
  }

  // (Register Memory) instructions - ld,lda,ldc,st,jlt,jle,jgt,jge,jeq,jne
  // format: opcode r, d(s)
  // a = d + reg[s] (generates DMEM_ERR if a<0 or a>=DADDR_SIZE)
  // LD  - reg[r] = dMem[a]
  // LDA - reg[r] = a
  // LDC - reg[r] = d
  // ST  - dMem[a] = reg[r]
  // JLT - if(reg[r]<0) reg[PC_REG] = a
  // JLE - if(reg[r]<=0) reg[PC_REG] = a
  // JGT - if(reg[r]>0) reg[PC_REG] = a
  // JGE - if(reg[r]>=0) reg[PC_REG] = a
  // JEQ - if(reg[r]==0) reg[PC_REG] = a
  // JNE - if(reg[r]!=0) reg[PC_REG] = a
  private void emitRM(String op, int r, int d, int s, String comment) {
    System.out.printf("%3d:    %3s  %d,%d(%d) \t%s\n", emitLoc++, op, r, d, s, comment);
    if (emitLoc > highEmitLoc) highEmitLoc = emitLoc;
  }

  // (Register Memory Absolute) instructions
  private void emitRM_Abs(String op, int r, int a, String comment) {
    System.out.printf("%3d:    %3s  %d,%d(%d) \t%s\n", emitLoc, op, r, a - (emitLoc + 1), pc, comment);
    emitLoc++;
    if (emitLoc > highEmitLoc) highEmitLoc = emitLoc;
  }


  /*    Routines to maintain the code space    */

  private int emitSkip(int howMany) {
    int i = emitLoc;
    emitLoc += howMany;
    if (emitLoc > highEmitLoc) highEmitLoc = emitLoc;
    return i;
  }

  private void emitBackup(int loc) {
    if (loc > highEmitLoc) highEmitLoc = loc;
    emitLoc = loc;
  }

  private void emitRestore() {
    emitLoc = highEmitLoc;
  }


  /*    Routine to generate one line of comment    */

  private void emitComment(String comment) {
    System.out.println("* " + comment);
  }


  public void visit(Absyn tree) {

    //prelude
    emitPrelude();

    // reserve this to jump (he has that jump in his output)
    emitComment("Jump around i/o routines here");
    int jumpAroundIOLoc = emitSkip(1);

    //input
    emitComment("code for input routine");
    emitRM("ST", 0, retFO, fp, "store return");
    emitRO("IN", 0, 0, 0, "input");
    emitRM("LD", pc, retFO, fp, "return to caller");

    // output
    emitComment("code for output routine");
    emitRM("ST", 0, retFO, fp, "store return");
    emitRM("LD", 0, -2, fp, "load output value");
    emitRO("OUT", 0, 0, 0, "output");
    emitRM("LD", pc, retFO, fp, "return to caller");

    int funcBodyStart = emitLoc;
    emitBackup(jumpAroundIOLoc);
    emitRM_Abs("LDA", pc, funcBodyStart, "jump around I/O code");
    emitRestore();

    // TODO: decrement globalOffset for each var declared in global scope

    //end prelude
    emitComment("End of standard prelude.");

    // go down the tree
    tree.accept(this, 0, false);

    //finale 
    emitFinale();

  }


  public void visit(DecList decList, int offset, boolean flag) {
    // process decs that are not main
    for (DecList current = decList; current != null; current = current.tail) {
      if (current.head != null &&
          !(current.head instanceof FunctionDec &&
            ((FunctionDec) current.head).func.equals("main"))) {
        current.head.accept(this, offset, flag);
      }
    }
    // process main
    for (DecList current = decList; current != null; current = current.tail) {
      if (current.head != null &&
          current.head instanceof FunctionDec &&
          ((FunctionDec) current.head).func.equals("main")) {
        current.head.accept(this, offset, flag);
      }
    }
  }

  public void visit(FunctionDec dec, int offset, boolean flag) {
    if(dec.body != null){//prototype
      dec.funaddr = emitLoc+1;
      emitComment("processing function: " + dec.func + " (" + dec.funaddr + ")");
      emitComment("jump around function body here");
      int jumpAroundFnBodyLoc = emitSkip(1); // reserve spot to jump around fn

      if (dec.func.equals("main")) {
        mainEntry = emitLoc;
      }

      // ret
      emitRM("ST", ac0, retFO, fp, "store return");

      // initFO = offset of -2
      offset = initFO;

      // args
      int numParams = 0;
      int tempInt = frameOffset;
      VarDecList temp = dec.params;
      while (temp != null) {
        if (temp.head != null && temp.head.getTypeName() != "void"){
            numParams++;
            temp.head.offset = offset+frameOffset; // assign param offset
            emitComment("processing local var (param): " + temp.head.getTypeName() + " (" + temp.head.offset + ")");
           frameOffset--; // dec frameOffset
        }
        temp = temp.tail;
      }      

      
      // go into body
      if (dec.body != null) dec.body.accept(this, offset, false);

      //reset
      frameOffset = tempInt;
      emitRM("LD", pc, retFO, fp, "return to caller");
    
      // backpatch jump-around
      //if (jumpAroundFnBodyLoc != -1) {
        int afterFn = emitLoc;//not sure if this would work for other ones
        emitBackup(jumpAroundFnBodyLoc);
        emitRM_Abs("LDA", pc, afterFn, "jump around fn body");//see if this works
        emitRestore();
        
      //}
    }
  }

  public void visit(CompoundExp exp, int offset, boolean flag) {
    emitComment("-> CompoundExp");
    if (exp.decs != null) exp.decs.accept(this, offset, flag);
    if (exp.exps != null) exp.exps.accept(this, offset, flag);
    emitComment("<- CompoundExp");
  }

  public void visit(SimpleDec varDec, int offset, boolean flag) {
    //check if global or vocal
    if(varDec.nestLevel ==0){//global
      //test
      varDec.offset = offset+globalOffset;
      emitComment("allocating global var: " + varDec.name + " (" + varDec.offset + ")");
      globalOffset--; // dec globalOffset
      emitComment("<- done global allocation");
    }
    else{
      varDec.offset = offset+frameOffset;
      emitComment("processing local var: " + varDec.name + " (" + varDec.offset + ")");
      frameOffset--; // dec frameOffset
    }
  }

  public void visit(ArrayDec varDec, int offset, boolean flag) {
    emitComment("processing local array: " + varDec.name);
    int arrSize = Integer.parseInt(varDec.size);
      
    if(varDec.nestLevel == 0){
      //global array 
      varDec.offset = offset+globalOffset;
      globalOffset -= (arrSize + 1);
      emitComment("allocating global array: " + varDec.name 
                    + " at offset " + offset + globalOffset
                    + ", size=" + arrSize);
      emitRM("LDC", ac0, arrSize, 0, "load array size");
      emitRM("ST", ac0, varDec.offset, gp, "store in global memory");
    } else {
      //local array 
      varDec.offset = offset+frameOffset;
      frameOffset -= (arrSize + 1);
      emitComment("allocating local array: " + varDec.name 
            + " at offset " + offset + frameOffset
            + ", size=" + arrSize);
     
      emitRM("LDC", ac0, arrSize, 0, "load array size");
      emitRM("ST", ac0, varDec.offset, fp, "store in local memory");
    }
    emitComment("<- ArrayDec: " + varDec.name);
  }

  public void visit(ExpList exp, int offset, boolean flag) {
    while (exp != null) {
      if (exp.head != null) exp.head.accept(this, offset, flag);
      exp = exp.tail;
    }
  }

  public void visit(NameTy type, int offset, boolean flag) {
    emitComment("-> NameTy: " + type.type);
    emitComment("<- NameTy: " + type.type);
  }

  public void visit(NilExp exp, int offset, boolean flag) {
    emitComment("-> NilExp");
    emitComment("<- NilExp");
  }

  public void visit(IntExp exp, int offset, boolean flag) {
    emitComment("-> IntExp: " + exp.value);
    emitRM("LDC", ac0, Integer.parseInt(exp.value), ac0, "Load int const");
    emitComment("<- IntExp: " + exp.value);
  }

  public void visit(BoolExp exp, int offset, boolean flag) {
    emitComment("-> BoolExp: " + exp.value);
    emitRM("LDC", ac0, exp.value.equals("true") ? 1 : 0, 0, "Load bool const");
    emitRM("ST", ac0, offset, fp, "op: push left");
    emitComment("<- BoolExp: " + exp.value);
  }

  public void visit(VarExp exp, int offset, boolean flag) {
    emitComment("-> VarExp offset:" + offset);
    // if (exp.name != null){
      //  if(((VarDec)exp.dtype).nestLevel == 0){
      //   exp.name.accept(this, offset, flag);
      //  }
      //  else{
        exp.name.accept(this, ((VarDec)exp.dtype).offset, flag);
      //  }
    // } 
    emitComment("<- VarExp");
  }

  public void visit(SimpleVar var, int offset, boolean flag) {
    emitComment("-> SimpleVar: looking up id: " + var.name + " (" + offset + ")");
    if (flag) {
      emitRM("LDA", ac0, offset, fp, "load id address");
    } else {
      emitRM("LD", ac0, offset, fp, "load id value");
    }
    // emitComment("<- SimpleVar");
  }

  public void visit(IndexVar var, int offset, boolean flag) {
    emitComment("-> IndexVar: " + var.name + " offset:" + offset);
    
    ArrayDec dec = (ArrayDec) var.dtype;
    int reg = dec.nestLevel == 0? gp : fp;
    /*
 20:     LD  0,-2(5) 	load id value
 21:     ST  0,-9(5) 	store array addr
    */
   System.out.println("* test vardec offset" + dec.offset);
   if (dec.nestLevel ==0) {
    emitRM("LDA", ac0, ac0, reg, "load array base address");
   }
   else{
    emitRM("LDA", ac0, dec.offset-1, reg, "load id value");
    System.out.println("* offset:"+offset);
    // emitRM("LDA", ac0, dec.offset-1, reg, "load array base address");
   }

     System.out.println("* justin:" + offset + " " + frameOffset);
    emitRM("ST", ac0, dec.offset + frameOffset, reg, "store array address");
    // go into index var
    if (var.index != null) var.index.accept(this, offset, false);

    //check if the index < 0
    emitRM("JLT", ac0, 1, pc, "halt if subscript < 0");
    emitRM("LDA", pc, 1, pc, "absolute jump if not");
    emitRO("HALT", 0, 0, 0, "halt if subscript < 0");
    //need to check if out of bounds above
    //run subtraction, then check if greater or equal to 0
    emitRM("LD", ac1, dec.offset, reg, "load size");
    emitRO("SUB", ac1, ac0, ac1, "subtract size from index");
    emitRM("JGE", ac1, 1, pc, "halt if subscript > size");
    emitRM("LDA", pc, 1, pc, "absolute jump if not");
    emitRO("HALT", 0, 0, 0, "halt if subscript < 0");


    emitRM("LD", ac1, dec.offset + frameOffset, fp, "load array base addr");
    // this line is broken lol nvm
    emitRO("SUB", ac0, ac1, ac0,"base is at the top of array");
    if(!flag){
      emitRM("LD", ac0, ac0, ac0, "load value at array index");//this line could be wrong
    }  
    
    emitComment("<- IndexVar: " + var.name);
  }

  public void visit(AssignExp exp, int offset, boolean flag) {
    emitComment("-> AssignExp");

    // if (!exp.rhs.getExpName().equals("input()")) //ignore for input()
      // frameOffset--; // dec frameOffset (make space for assignexp val)

    exp.lhs.accept(this, offset, true); // LHS: get address
    emitRM("ST", ac0, offset+frameOffset, fp, "assign: push left");

    exp.rhs.accept(this, offset-1, false);
    emitRM("LD", ac1, offset+frameOffset, fp, "assign: load left");
    // emitRM("LD", ac1, offset-2, fp, "assign: load right expr");
    emitRM("ST", ac0, ac0, ac1, "assign: store value");
    // emitRM("ST", ac1, offset, fp, "assign: store value");
    emitComment("<- AssignExp");
  }

  public void visit(OpExp exp, int offset, boolean flag) {
    emitComment("-> OpExp");
    exp.left.accept(this, offset, false);
    emitRM("ST", ac0, frameOffset+offset, fp, "op: push left");
    exp.right.accept(this, offset-1, false);

    // load each side of expr
    emitRM("LD", ac1, frameOffset+offset, fp, "op: load left");
    // emitRM("LD", ac1, offset-2, fp, "op: load right expr");

    // perform operation
    switch (exp.op) {
      case OpExp.PLUS:   emitRO("ADD", ac0, ac1, ac0, "op +"); break;
      case OpExp.MINUS:  emitRO("SUB", ac0, ac1, ac0, "op -"); break;
      case OpExp.MUL:    emitRO("MUL", ac0, ac1, ac0, "op *"); break;
      case OpExp.DIV:    emitRO("DIV", ac0, ac1, ac0, "op /"); break;

      // Relational operators
      case OpExp.GT: case OpExp.LT: case OpExp.EQ: case OpExp.NE:
      case OpExp.LE: case OpExp.GE: {
        String[] ops = {"JGT", "JLT", "JEQ", "JNE", "JLE", "JGE"};
        String[] opNames = {">", "<", "==", "!=", "<=", ">="};
        int opIndex = exp.op - OpExp.GT; 
        emitRO("SUB", ac0, 1, ac0, "op " + opNames[opIndex]);
        int trueLoc = emitLoc;
        //int falseJump = emitSkip(1);
        emitRM(ops[opIndex], 0, 2, pc, "jump if " + opNames[opIndex]);
        emitRM("LDC", ac0, 0, ac0, "false case");
        emitRM("LDA", pc, 1, pc, "unconditional jmp");//hardcoded, need to figure out values here
        emitRM("LDC", ac0, 1, ac0, "true case");
        // emitBackup(falseJump);
        emitRestore();
        break;
      }

      case OpExp.AND:     emitRO("AND", ac0, 1, ac0, "op &&"); break;
      case OpExp.OR:      emitRO("OR", ac0, 1, ac0, "op ||"); break;
      case OpExp.UMINUS:  emitRO("NEG", ac0, 0, ac0, "op unary -"); break;
      case OpExp.NOT:     emitRO("NOT", ac0, 0, ac0, "op !"); break;
      default:            emitComment("Unknown op: " + exp.op);
    }
    // emitRM("ST", ac0, offset, fp, "op: store value");
    emitComment("<- OpExp");
  }

  public void visit(IfExp exp, int offset, boolean flag) {
    emitComment("-> IfExp");
    exp.test.accept(this, offset, false);
    int savedLoc = emitSkip(1);
    exp.thenpart.accept(this, offset, flag);
    int thenEnd = emitSkip(1);
    int elseStart = emitLoc;
    emitBackup(savedLoc);
    emitRM_Abs("JEQ", ac0, elseStart, "jump to else");
    emitRestore();
    if (exp.elsepart != null) exp.elsepart.accept(this, offset, flag);
    int afterElse = emitLoc;
    emitBackup(thenEnd);
    emitRM_Abs("LDA", pc, afterElse, "jump past else");
    emitRestore();
    emitComment("<- IfExp");
  }

  public void visit(WhileExp exp, int offset, boolean flag) {
    emitComment("-> WhileExp");
    emitComment("WhileExp: jump after body comes back here");
    int testLoc = emitLoc;
    exp.test.accept(this, offset, false);
    emitComment("WhileExp: jump to end belongs here");
    int jumpToEnd = emitSkip(1);
    exp.body.accept(this, offset, flag);
    emitRM("LDA", pc, testLoc - emitLoc - 1, pc, "jump to test");
    int afterLoop = emitLoc;
    emitBackup(jumpToEnd);
    emitRM_Abs("JEQ", ac0, afterLoop, "exit loop");
    emitRestore();
    emitComment("<- WhileExp");
  }

  public void visit(CallExp exp, int offset, boolean flag) {
    emitComment("-> CallExp: call of function: " + exp.function + " " + frameOffset + " " + offset);
    // compute args
    int argOffset = 0;
    if (exp.args != null) {
      // argOffset = -2;
      ExpList temp = exp.args;
      while (temp != null) {
      if (temp.head != null){
        temp.head.accept(this, offset, flag);
        emitRM("ST", ac0, offset+frameOffset+initFO+argOffset--, fp, "store arg val");
      } 
      temp = temp.tail;
    }

      //old code
      //exp.args.accept(this, offset, false);
      //emitRM("ST", ac0, offset+frameOffset+initFO+argOffset--, fp, "store arg val");
    }

    // I just hard coded these not sure what were supposed to do
    // dw ill fix it (look at slide 51 week 11)
    emitRM("ST", fp, offset+frameOffset+ofpFO, fp, "push ofp");
    emitRM("LDA", fp, offset+frameOffset, fp, "push frame");
    emitRM("LDA", ac0, ac1, pc, "load ac with ret ptr");
    // If the function is input or output, jump to the appropriate routine.
    if (exp.function.equals("input")) {
      // calc loc of var to store input
      // ex. offset = 4 - (18 + 1) = -15       4= input function start line, 18 = current line
      int inpLoc = 4 - (emitLoc + 1); 
      emitRM("LDA", pc, inpLoc, pc, "jump to input routine");
    } else if (exp.function.equals("output")) {

      emitRM("LDA", pc, 7 - (emitLoc + 1), pc, "jump to fun loc");
    } else {
      FunctionDec dec = (FunctionDec) exp.dtype;
      emitRM("LDA", pc, dec.funaddr - (emitLoc + 1), pc, "call function (stub)"); 
    }
    emitRM("LD", fp, ofpFO, fp, "pop frame"); // return to caller
    emitComment("<- CallExp");
  }

  public void visit(ReturnExp exp, int offset, boolean flag) {
    emitComment("-> ReturnExp");
    if (exp.value != null) exp.value.accept(this, offset, flag);
    emitRM("LD", pc, retFO, fp, "return to caller");
    emitComment("<- ReturnExp");
  }

  public void visit(VarDecList varDecList, int offset, boolean flag) {
    while (varDecList != null) {
      if (varDecList.head != null) varDecList.head.accept(this, offset, flag);
      varDecList = varDecList.tail;
    }
  }
}

