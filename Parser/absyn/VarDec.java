package absyn;

abstract public class VarDec extends Dec {
    public int nestLevel; // 0 - global, 1 - local
    public int offset;
}
