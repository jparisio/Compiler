package absyn;

abstract public class Exp extends Absyn {

  public Dec dtype; // reference to Dec node to find type info
    
  public String getExpName()
  {
    if (this instanceof IntExp){
        IntExp temp = (IntExp)this;
        return temp.value;
    }
    else if (this instanceof BoolExp){
        BoolExp temp =(BoolExp)this;
        return temp.value;
    }
    else if (this instanceof VarExp){
        VarExp temp = (VarExp) this;
        return temp.name.name;
    }
    else if (this instanceof CallExp){
        CallExp temp = (CallExp) this;
        return temp.function + "()";
    }
    return "";
  }
}
