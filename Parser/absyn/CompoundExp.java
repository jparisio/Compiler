package absyn;

public class CompoundExp extends Exp {
  public ExpList exps;
  public VarDecList decs;

  public CompoundExp( int row, int col, VarDecList decs, ExpList exps) {
    this.row = row;
    this.col = col;
    this.decs = decs;
    this.exps = exps;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
}

