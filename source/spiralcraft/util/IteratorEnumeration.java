package spiralcraft.util;

import java.util.Iterator;
import java.util.Enumeration;

/**
 * Provides an Iterator interface to an Enumeration
 */
public class IteratorEnumeration
  implements Enumeration
{

  private final Iterator _iterator;

  public IteratorEnumeration(Iterator iterator)
  { _iterator=iterator;
  }    

  public boolean hasMoreElements()
  { return _iterator.hasNext();
  }

  public Object nextElement()
  { return _iterator.next();
  }
}
