package absyn;

abstract public class Dec extends Absyn {
    public NameTy type; // Stores type information for a declaration

    public String getTypeName() {
        if (type != null) {
            return type.getTypeName();
        }
        return " ";
    }
}
