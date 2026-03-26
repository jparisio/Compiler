/*
  Created By: Group 9
  File Name: SemanticAnalyzer.java
*/

import absyn.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SemanticAnalyzer implements AbsynVisitor {
    private HashMap<String, ArrayList<NodeType>> table;
    public boolean semanticErrors = false;
    private boolean containsMain = false;
    public int currentScopeLevel = 0;
    private String funcType = "";
    final static int SPACES = 4;
    public SemanticAnalyzer() {
      table = new HashMap<>();
      // Int input(void) and void ouput(void) he had in his thing for booltest
      insert("input", new NodeType("input", new FunctionDec(0, 0, new NameTy(0, 0, NameTy.INT), "input", new VarDecList(new SimpleDec(0, 0, new NameTy(0,0, NameTy.VOID),null),null), null), 0));
      insert("output", new NodeType("output", new FunctionDec(0, 0, new NameTy(0, 0, NameTy.VOID), "output", new VarDecList(new SimpleDec(0, 0, new NameTy(0,0, NameTy.INT),null),null), null), 0));
  }

    private void indent( int level ) {
      for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    // ====================
    // Visitor methods
    // ====================
    
    public void visit(ExpList exp, int level, boolean flag) {
        while(exp != null) {
            if(exp.head != null) {
                exp.head.accept(this, level, flag);
            }
            exp = exp.tail;
        }
    }

    public void visit(AssignExp exp, int level, boolean flag) {
        exp.lhs.accept(this, level, flag);
        exp.rhs.accept(this, level, flag);
        
        String varName = getVarName(exp.lhs);
        NodeType lhsEntry = lookup(varName);
        if(!(lhsEntry == null)) {
            String lhsType = typeToString(lhsEntry.def);
            String rhsType = typeToString(exp.rhs.dtype);
            
            if(!lhsType.equals(rhsType) && exp.rhs.dtype != null) {
                reportError(exp, "Type mismatch in assignment to '" + varName + "': " + lhsType + " vs. " + rhsType);
            }
        }
    }

    public void visit(IfExp exp, int level, boolean flag) {
        exp.test.accept(this, level, flag);
        //test not void in if
        if(exp.test.dtype != null && (exp.test.dtype.getTypeName().equals("void"))){
            reportError(exp, "Cannot have a void in an if statement.");
        }
        indent(level+1);
        System.out.println("Entering a new block:");
        exp.thenpart.accept(this, level, flag);
        printScopeVar();
        indent(level+1);
        System.out.println("Leaving the block");

        if(exp.elsepart != null) {
          indent(level+1);
          System.out.println("Entering a new block:");
          exp.elsepart.accept(this, level, flag);
          printScopeVar();
          indent(level+1);
          System.out.println("Leaving the block");
        }
    }

    public void visit(IntExp exp, int level, boolean flag) {
        exp.dtype = makeSimpleDec("int", exp.row, exp.col);
    }

    public void visit(OpExp exp, int level, boolean flag) {
        exp.left.accept(this, level, flag);
        exp.right.accept(this, level, flag);
        String leftType = typeToString(exp.left.dtype);
        String rightType = typeToString(exp.right.dtype);
        if(leftType.equals(rightType)) {
          if(exp.op >4){
            exp.dtype = makeSimpleDec("bool", exp.row, exp.col);
          }
          else{
            exp.dtype = makeSimpleDec("int", exp.row, exp.col);
          }
        } else if(exp.left.dtype != null && exp.right.dtype != null && isConvertible(leftType, rightType)) {
            String wider = widerType(leftType, rightType);
            exp.dtype = makeSimpleDec(wider, exp.row, exp.col);
        } else {
            reportError(exp, "Incompatible types in operation: " + leftType + " vs. " + rightType);
        }
    }

    public void visit(VarExp exp, int level, boolean flag) {
        String varName = getVarName(exp);
        // System.err.println("hello " + varName);
        NodeType varEntry = lookup(varName);
        if(varEntry != null) {
            exp.dtype = varEntry.def; 
            exp.name.dtype = exp.dtype;
        }
        exp.name.accept(this, level, flag);
    }

    public void visit(NameTy type, int level, boolean flag) {
        
    }

    public void visit(SimpleVar var, int level, boolean flag) {
        if(lookup(var.name) == null){
            reportError(var, "Variable '" + var.name + "' is not declared.");
        }
    }

    public void visit(IndexVar var, int level, boolean flag) {
        int isInt=0;
        if(lookup(var.name) == null) {
            reportError(var, "Array '" + var.name + "' is not declared.");
        }
        if(var.index != null) {
            NodeType val = lookup(var.index.getExpName());
            if( val != null && val.def.type.type == 1){
                isInt =1;
            }
            if((!(var.index instanceof IntExp) && isInt==0)&& lookup(var.index.getExpName())!=null){
                reportError(var, " '" + var.name + "' is invalidly indexed.");
            }
            var.index.accept(this, level, flag);
        }
    }
    public void visit(NilExp exp, int level, boolean flag) {
        exp.dtype = makeSimpleDec("void", exp.row, exp.col);
    }

    public void visit(BoolExp exp, int level, boolean flag) {
        // should bool be assignable to int?
        exp.dtype = makeSimpleDec("bool", exp.row, exp.col);
    }

    public void visit(CallExp exp, int level, boolean flag) {
      
      NodeType funcEntry = lookup(exp.function);
      if (exp.args.head != null) {
          exp.args.accept(this, level, flag);
      }
      if (funcEntry == null) {
          reportError(exp, "Function '" + exp.function + "' is not declared.");
      } else if (!(funcEntry.def instanceof FunctionDec)) {
          reportError(exp, "'" + exp.function + "' is not a function.");
      } else {
          FunctionDec fdec = (FunctionDec) funcEntry.def;
          //exp.dtype = new FunctionDec(exp.row, exp.col, fdec.result, exp.function);
          exp.dtype = fdec;
          //how to get return type?
          exp.dtype.type = fdec.result;
          if(exp.args.head != null){
            if(!exp.args.toString().equals(fdec.params.toString())){
                reportError(exp, "Function call '" + exp.function + "' has unexpected arguments. Expected (" + fdec.params.toString() + ") but got (" + exp.args.toString() +").");
            }
          }
      }
  }

    public void visit(WhileExp exp, int level, boolean flag) {
        exp.test.accept(this, level, flag);
        if(exp.test.dtype != null && (exp.test.dtype.getTypeName().equals("void"))){
            reportError(exp, "Cannot have a void in a while statement.");
        }
        indent(level+1);
        System.out.println("Entering a new block:");
        exp.body.accept(this, level, flag);
        printScopeVar();
        indent(level+1);
        System.out.println("Leaving the block");
    }

    public void visit(ReturnExp exp, int level, boolean flag) {
        exp.value.accept(this, level, flag);
        // System.err.println(funcType + " vs. " + exp.value.dtype.type.getTypeName());
        if(!funcType.equals(exp.value.dtype.type.getTypeName())){
            reportError(exp, "Incompatable return type.");
        }
    }

    public void visit(CompoundExp exp, int level, boolean flag) {
        currentScopeLevel++; 
        if(exp.decs != null) {
            exp.decs.accept(this, level+1, flag);
        }
        if(exp.exps != null) {
            exp.exps.accept(this, level+1, flag);
        }
        currentScopeLevel--;  
    }

    public void visit(FunctionDec dec, int level, boolean flag) {
        currentScopeLevel++;
        boolean hasReturnStatement = false; 
        boolean alreadyDefined = false;
        NodeType exists = lookup(dec.func);

        if (exists == null) {
            insert(dec.func, new NodeType(dec.func, dec, level));
            if (dec.params != null) {
                dec.params.accept(this, level + 1, flag);
            }
        } else{
            if (exists.def instanceof FunctionDec) {
                FunctionDec fDec = (FunctionDec) exists.def;
                if (fDec.body == null && dec.body != null) {
                    // This is okay: function body follows prototype
                } else if (fDec.body != null && dec.body == null) {
                    reportError(dec, "Function '" + fDec.func + "' declared before prototype.");
                } else {
                    reportError(dec, "Function '" + fDec.func + "' is already declared.");
                    alreadyDefined = true;
                }
            }
        }
    
        if (dec.body != null && !alreadyDefined) {
            if(dec.func.equals("main")){
                containsMain = true;
            }
            funcType = dec.result.getTypeName();
            indent(level + 1);
            System.out.println("Entering the scope for function " + dec.func);
            
            hasReturnStatement = checkReturnStatement(dec.body);
            
            if (!funcType.equals("void") && !hasReturnStatement) {
                reportError(dec, "Function '" + dec.func + "' does not contain a return statement.");
            }
    
            dec.body.accept(this, level, flag);
            printScopeVar();
            indent(level + 1);
            System.out.println("Leaving the function scope");
        }
    
        currentScopeLevel--;
    }
    
    

    public void visit(SimpleDec varDec, int level, boolean flag) {
        if(varDec.name !=null){
            if(currentScopeLevel==0){
                varDec.nestLevel=0;
            }
            else{
                varDec.nestLevel=1;
            }
            ArrayList<NodeType> existing = table.get(varDec.name);
            if(existing != null && !existing.isEmpty()){
               if(existing.get(existing.size()-1).level == level){
                  reportError(varDec, "Variable '" + varDec.name + "' is already declared.");
               } else {
                    insert(varDec.name, new NodeType(varDec.name, varDec, level));
                 }
            } 
            else {
                insert(varDec.name, new NodeType(varDec.name, varDec, level));
            }
        }
    }

    public void visit(ArrayDec varDec, int level, boolean flag) {
        if(varDec.name !=null){
            ArrayList<NodeType> existing = table.get(varDec.name);
            if(currentScopeLevel==0){
                varDec.nestLevel=0;
            }
            else{
                varDec.nestLevel=1;
            }
            if(existing != null && !existing.isEmpty()){
              if(existing.get(existing.size()-1).level == level){
                reportError(varDec, varDec.name + ": is already declared");
              }  else {
               insert(varDec.name, new NodeType(varDec.name, varDec, level));
            }
         }  else {
                insert(varDec.name, new NodeType(varDec.name, varDec, level));
            }
       }
    }

    public void visit(DecList decList, int level, boolean flag) {
        while(decList != null) {
            if(decList.head != null) {
                decList.head.accept(this, level, flag);
            }
            decList = decList.tail;
        }
    }

    public void visit(VarDecList varDecList, int level, boolean flag) {
        while(varDecList != null) {
            if(varDecList.head != null) {
                varDecList.head.accept(this, level, flag);
            }
            varDecList = varDecList.tail;
        }
    }

    public boolean hasSemanticErrors() {
        return semanticErrors;
    }

    public boolean hasMain(){
        return containsMain;
    }

    // ====================
    // Helper Methods
    // ====================
    
    private void insert(String name, NodeType node) {
      if (name == null || name.isEmpty()) {
          System.err.println("Error: Attempted to insert a declaration with no name into the symbol table.");
          semanticErrors = true;
          return;
      }
      table.computeIfAbsent(name, k -> new ArrayList<>()).add(node);
  }
  

    private NodeType lookup(String name) {
        ArrayList<NodeType> list = table.get(name);
        return (list != null && !list.isEmpty()) ? list.get(list.size() - 1) : null;
    }
    
    //not needed
    private void delete(String name) {
        ArrayList<NodeType> list = table.get(name);
        if (list != null && !list.isEmpty()) {
            list.remove(list.size() - 1);
            if (list.isEmpty()) table.remove(name);
        }
    }
    
    private void reportError(Absyn node, String message) {
        System.err.println("Error at line " + node.row + ", column " + node.col + ": " + message);
        semanticErrors = true;
    }
    
    private boolean isConvertible(String type1, String type2) {
        return (type1.equals("int") && type2.equals("float")) ||
               (type1.equals("float") && type2.equals("int"));
    }
    
    private String widerType(String type1, String type2) {
        if(type1.equals("float") || type2.equals("float")) {
            return "float";
        }
        return "int";
    }
    
    private String getVarName(VarExp exp) {
        if(exp.name != null) {
            return exp.name.name;
        }
        return "";
    }
  
    private String typeToString(Dec d) {
        if(d == null) return "";
        if(d instanceof SimpleDec) {
            return ((SimpleDec)d).type.getTypeName();
        } else if(d instanceof ArrayDec) {
            return "" + ((ArrayDec)d).type.getTypeName();
        } else if(d instanceof FunctionDec) {
            return "" + ((FunctionDec)d).result.getTypeName();
        }
        return "";
    }
    
    // Create a new SimpleDec for a literal expression.
    private Dec makeSimpleDec(String typeName, int row, int col) {
        int code;
        if(typeName.equals("bool")) {
            code = NameTy.BOOL;
        } else if(typeName.equals("int")) {
            code = NameTy.INT;
        } else if(typeName.equals("void")) {
            code = NameTy.VOID;
        } else {
            code = NameTy.INT; // Default to int if unknown.
        }
        return new SimpleDec(row, col, new NameTy(row, col, code), typeName);
    }

    // ====================
    // Type-checking Helpers for Dec nodes
    // ====================
    public boolean isBoolean(Dec dtype) {
        if(dtype instanceof SimpleDec) {
            SimpleDec sType = (SimpleDec) dtype;
            return sType.type.type == NameTy.BOOL;
        } else if(dtype instanceof ArrayDec) {
            ArrayDec aType = (ArrayDec) dtype;
            return aType.type.type == NameTy.BOOL;
        } else if(dtype instanceof FunctionDec) {
            FunctionDec fType = (FunctionDec) dtype;
            return fType.result.type == NameTy.BOOL;
        }
        return false;
    }

    public boolean isInteger(Dec dtype) {
        if(dtype instanceof SimpleDec) {
            SimpleDec sType = (SimpleDec) dtype;
            return sType.type.type == NameTy.INT;
        } else if(dtype instanceof ArrayDec) {
            ArrayDec aType = (ArrayDec) dtype;
            return aType.type.type == NameTy.INT;
        } else if(dtype instanceof FunctionDec) {
            FunctionDec fType = (FunctionDec) dtype;
            return fType.result.type == NameTy.INT;
        }
        return false;
    }

    public boolean isVoid(Dec dtype) {
        if(dtype instanceof SimpleDec) {
            SimpleDec sType = (SimpleDec) dtype;
            return sType.type.type == NameTy.VOID;
        } else if(dtype instanceof ArrayDec) {
            ArrayDec aType = (ArrayDec) dtype;
            return aType.type.type == NameTy.VOID;
        } else if(dtype instanceof FunctionDec) {
            FunctionDec fType = (FunctionDec) dtype;
            return fType.result.type == NameTy.VOID;
        }
        return false;
    }

    //Only prints out lines of the scope
    public void printScopeVar(){
       int index=0;
       for(String key : table.keySet()){
         index = table.get(key).size()-1;
         if(!table.get(key).isEmpty()){
            if(table.get(key).get(index).level == currentScopeLevel){
            indent(table.get(key).get(index).level+1);
            System.out.println(table.get(key).get(index).name + ": " + table.get(key).get(index));
                  table.get(key).remove(index);
            }
         }
         
       }
    }


    private boolean checkReturnStatement(Exp exp) {
        if (exp instanceof ReturnExp) {
            return true; 
        }
        if (exp instanceof CompoundExp) {
            CompoundExp compExp = (CompoundExp) exp;
            if (compExp.exps != null) {
                ExpList current = compExp.exps;
                while (current != null) {
                    if (checkReturnStatement(current.head)) {
                        return true; 
                    }
                    current = current.tail;
                }
            }
        }
        if (exp instanceof IfExp) {
            IfExp ifExp = (IfExp) exp;
            // Both if and else parts need to contain a return so its always reached
            return checkReturnStatement(ifExp.thenpart) && checkReturnStatement(ifExp.elsepart);
        }
        if (exp instanceof WhileExp) {
            return checkReturnStatement(((WhileExp) exp).body);
        }
        return false;
    }
}


