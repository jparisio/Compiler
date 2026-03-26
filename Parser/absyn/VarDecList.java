package absyn;

public class VarDecList extends Absyn {
  public VarDec head;
  public VarDecList tail;

  public VarDecList( VarDec head, VarDecList tail ) {
    this.head = head;
    this.tail = tail;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
  @Override
    public String toString() {
      String output = "";
      if(head != null){
        output = head.getTypeName();
        if(tail != null){
          output = output + ", " + tail;
        }
      }
      
      return output;      
    }
}

