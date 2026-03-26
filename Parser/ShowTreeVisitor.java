/*
  Created By: Group 9
  File Name: ShowTreeVisitor.java
*/

import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level, boolean flag ) {
    while( expList != null ) {
      if( expList.head != null ){
        expList.head.accept( this, level, flag );
      }
      expList = expList.tail;
    } 
  }

  public void visit( AssignExp exp, int level, boolean flag ) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level, flag );
    exp.rhs.accept( this, level, flag );
  }

  public void visit( IfExp exp, int level, boolean flag ) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level, flag );
    exp.thenpart.accept( this, level, flag );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level, flag );
  }

  public void visit( IntExp exp, int level, boolean flag ) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit( OpExp exp, int level, boolean flag ) {
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.MUL:
        System.out.println( " * " );
        break;
      case OpExp.DIV:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " = " );
        break;
      case OpExp.NE:
        System.out.println( " != " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.LE:
        System.out.println( " <= " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.GE:
        System.out.println( " >= " );
        break;
      case OpExp.UMINUS:
        System.out.println( " - " );
        break;
      case OpExp.NOT:
        System.out.println( " ~ " );
        break;
      case OpExp.AND:
        System.out.println( " && " );
        break;
      case OpExp.OR:
        System.out.println( " || " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    if (exp.left != null)
       exp.left.accept( this, level, flag );
    if (exp.right != null)
       exp.right.accept( this, level, flag );
  }

  public void visit( VarExp exp, int level, boolean flag ) {
    indent( level);
    System.out.print("VarExp: ");
    exp.name.accept( this, level, flag);
    System.out.println();
  }

  public void visit( SimpleVar var, int level, boolean flag){
    System.out.print( var.name );
  }

  public void visit( IndexVar var, int level, boolean flag){
    System.out.print( var.name );
    System.out.print(" [");
    if(var.index instanceof VarExp){
       VarExp varVal = (VarExp) var.index;
       varVal.name.accept(this, level, flag);
    }
    else if(var.index instanceof IntExp){
       IntExp intVal = (IntExp)var.index;
       System.out.print(intVal.value);
    }
    else if(var.index instanceof CallExp){
      CallExp callVal = (CallExp)var.index;
      System.out.print(callVal.function + "()");
    }
    System.out.print("]");
  }

  public void visit( NilExp exp, int level, boolean flag){
    //NilExp is supposed to do nothing
  }

  public void visit( BoolExp exp, int level, boolean flag){
    indent( level );
    System.out.println( "BoolExp: " + exp.value );
  }

  public void visit( CallExp exp, int level, boolean flag){
    indent( level );
    System.out.println("CallExp: " +  exp.function);
    level ++;
    exp.args.accept(this, level, flag);
  }

  public void visit( WhileExp exp, int level, boolean flag){
    indent( level );
    System.out.println("WhileExp: ");
    level++;
    exp.test.accept(this, level, flag);
    exp.body.accept(this, level, flag);
  }

  public void visit( ReturnExp exp, int level, boolean flag){
    indent( level );
    if( exp.value != null ){
      System.out.println( "ReturnExp: ");
      level ++;
      exp.value.accept(this, level, flag);
    }
    else{
      System.out.println( "Return Exp:");
    }
  }

  public void visit( CompoundExp exp, int level, boolean flag){
    indent( level );
    System.out.println( "CompoundExp: " );
    level++;
    exp.decs.accept( this, level, flag);
    exp.exps.accept( this, level, flag);
  }

  public void visit( FunctionDec dec, int level, boolean flag){
    indent( level );
    System.out.println( "FunctionDec: " + dec.func );
    level++;
    if (dec.params != null ){
       dec.params.accept( this, level, flag);
    }
    if (dec.body != null ){
       dec.body.accept( this, level, flag );
    }
  }

  public void visit( SimpleDec varDec, int level, boolean flag){
    if(varDec.name != null){
      indent(level);
      System.out.print( "SimpleDec: ");
      varDec.type.accept( this, level, flag);
      System.out.println( varDec.name );
      // end of var decl branch
    }
  }

  public void visit( NameTy type, int level, boolean flag){
    // print var type
    switch( type.type) {
      case NameTy.BOOL:
        System.out.print( "BOOL " );
        break;
      case NameTy.INT:
        System.out.print( "INT " );
        break;
      case NameTy.VOID:
        System.out.print( "VOID " );
        break;
      default:
        System.out.println( "Unrecognized Type at line " + type.row + " and column " + type.col);
    }
  }

  public void visit( ArrayDec varDec, int level, boolean flag){
    indent( level );
    System.out.print( "ArrayDec: ");
    varDec.type.accept( this, level, flag);
    if(varDec.size != null){
      System.out.println( varDec.name + " of size: " + varDec.size);
    }
    else{
      System.out.println( varDec.name );
    }
  }

  public void visit( DecList decList, int level, boolean flag){
    while( decList != null ) {
      if(decList.head != null){
        decList.head.accept( this, level, flag );
      }
      decList = decList.tail;
    }
  }

  public void visit( VarDecList varDecList, int level, boolean flag){
    while( varDecList != null ) {
      if(varDecList.head != null ){
        varDecList.head.accept( this, level, flag);
      }
      varDecList = varDecList.tail;
    }
  }

}
