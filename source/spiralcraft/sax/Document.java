package spiralcraft.sax;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/**
 * Represents an XML Document
 */
public class Document
  extends Node
{

  private boolean _completed;

  public Document()
  {
  }

  public Document(Element root)
  { addChild(root);
  }

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

  public void playEvents(ContentHandler handler)
    throws SAXException
  { 
    handler.startDocument();
    Node root=getRootElement();
    if (root!=null)
    { root.playEvents(handler);
    }
    handler.endDocument();
  }
  
}
