package spiralcraft.sax;

/**
 * Represents an XML Document
 */
public class Document
  extends Node
{

  private boolean _completed;
  
  public void complete()
  { _completed=true;
  }
}
