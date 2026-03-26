package absyn;

public class NodeType {
    public String name;  
    public Dec def;  
    public int level;    

    public NodeType(String name, Dec def, int level) {
        this.name = name;
        this.def = def;
        this.level = level;
    }

    @Override
    public String toString() {
        if(def instanceof FunctionDec){
            FunctionDec fDec = (FunctionDec)def;
            String output =  "(" + fDec.params; 
            //need to figure out how to loop through paramaters 
            output = output + ") -> " + fDec.result.getTypeName();
            return output;
            //return fDec.result.getTypeName() + "(params) -> output"; 
        }
        else if(def instanceof SimpleDec){
            SimpleDec sDec = (SimpleDec)def;
            return sDec.type.getTypeName(); 
        }
        else if(def instanceof ArrayDec){
            ArrayDec aDec = (ArrayDec)def; 
            return aDec.type.getTypeName()+ " [" + aDec.size + "]"; 
        }
        return ""; //returns blank
    }
}
