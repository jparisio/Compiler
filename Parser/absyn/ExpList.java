package absyn;

public class ExpList extends Absyn {
  public Exp head;
  public ExpList tail;

  public ExpList( Exp head, ExpList tail ) {
    this.head = head;
    this.tail = tail;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag);
  }
    @Override
    public String toString() {
      String output = "";
      if (head.dtype != null) {
        output = head.dtype.getTypeName();//fix this part
        if(tail != null){
        output = output + ", " + tail;
      }
      }
      
      return output;      
    }
}
