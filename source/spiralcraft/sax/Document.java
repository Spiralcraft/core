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

  public Element getRootElement()
  { 
    if (getChildren().size()>0)
    { return (Element) getChildren().get(0);
    }
    else
    { return null;
    }
  }
}
