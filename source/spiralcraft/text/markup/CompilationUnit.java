package spiralcraft.text.markup;

/**
 * The root of a marked up document
 */
public class CompilationUnit
  extends Unit
{
  
  public String toString()
  { return super.toString()+"[root]";
  }  

  public boolean isOpen()
  { return true;
  }
}
