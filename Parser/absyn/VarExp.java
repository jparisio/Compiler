package absyn;

public class VarExp extends Exp {
  public Var name;

  public VarExp( int row, int col, Var name ) {
    this.row = row;
    this.col = col;
    this.name = name;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
}
