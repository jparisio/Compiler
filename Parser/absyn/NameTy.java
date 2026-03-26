package absyn;

public class NameTy extends Absyn {
  public final static int BOOL = 0;
  public final static int INT = 1;
  public final static int VOID = 2;

  public int type;

  public NameTy( int row, int col, int type) {
    this.row = row;
    this.col = col;
    this.type = type;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
   visitor.visit( this, level, flag );
  }

  public String getTypeName()
  {
    switch (type) {
      case 0:
        return "bool";
      case 1:
        return "int";
      case 2:
        return "void";
      default:
        return "unknown var type";
    }
  }
}
