package spiralcraft.sax;

import org.xml.sax.Attributes;

/**
 * An implementation of the SAX Attributes interface
 *   using an Array as a backing store
 */
public class ArrayAttributes
  implements Attributes
{
  private Attribute[] _attributes;

  public ArrayAttributes(Attribute[] attribs)
  { 
    if (attribs==null)
    { _attributes=new Attribute[0];
    }
    else
    { _attributes=attribs;
    }
  }

  public int getLength()
  { return _attributes.length;
  }

  public String getURI(int index)
  { return _attributes[index].getURI();
  }

  public String getLocalName(int index)
  { return _attributes[index].getLocalName();
  }

  public String getQName(int index)
  { return _attributes[index].getQName();
  }

  public String getType(int index)
  { return _attributes[index].getType();
  }

  public String getValue(int index)
  { return _attributes[index].getValue();
  }

  public int getIndex(String uri,String localName)
  {
    for (int i=0;i<_attributes.length;i++)
    { 
      if (equals(uri,_attributes[i].getURI())
          && equals(localName,_attributes[i].getLocalName())
         )
      { return i;
      }
    }
    return -1;
  }

  public int getIndex(String qname)
  {
    for (int i=0;i<_attributes.length;i++)
    { 
      if (equals(qname,_attributes[i].getQName()))
      { return i;
      }
    }
    return -1;
  }

  public String getType(String uri,String localName)
  {
    int index=getIndex(uri,localName);
    if (index>-1)
    { return _attributes[index].getType();
    }
    else
    { return null;
    }
  }

  public String getType(String qname)
  {
    int index=getIndex(qname);
    if (index>-1)
    { return _attributes[index].getType();
    }
    else
    { return null;
    }
  }

  public String getValue(String uri,String localName)
  {
    int index=getIndex(uri,localName);
    if (index>-1)
    { return _attributes[index].getValue();
    }
    else
    { return null;
    }
  }

  public String getValue(String qname)
  {
    int index=getIndex(qname);
    if (index>-1)
    { return _attributes[index].getValue();
    }
    else
    { return null;
    }
  }

  private boolean equals(String one,String two)
  {
    if (one==two)
    { return true;
    }
    return one!=null && one.equals(two);
  }
}
