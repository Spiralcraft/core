package spiralcraft.text.markup;

import java.io.Writer;
import java.io.IOException;


/**
 * A Unit which contains content only, and has no children.
 */
public class ContentUnit
  extends Unit
{
  private CharSequence _content;
  
  public ContentUnit(CharSequence content)
  { _content=content;
  }
  
  public CharSequence getContent()
  { return _content;
  }
  
  public String toString()
  { return super.toString()+"[content]";
  }

}
