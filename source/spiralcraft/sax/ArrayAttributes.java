//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.sax;

import org.xml.sax.Attributes;

/**
 * An implementation of the SAX Attributes interface
 *   using an Array as a backing store
 */
public class ArrayAttributes
  implements Attributes
{
  public static final ArrayAttributes EMPTY_INSTANCE
    =new ArrayAttributes(null);
  
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

  @Override
  public int getLength()
  { return _attributes.length;
  }

  @Override
  public String getURI(int index)
  { return _attributes[index].getURI();
  }

  @Override
  public String getLocalName(int index)
  { return _attributes[index].getLocalName();
  }

  @Override
  public String getQName(int index)
  { return _attributes[index].getQName();
  }

  @Override
  public String getType(int index)
  { return _attributes[index].getType();
  }

  @Override
  public String getValue(int index)
  { return _attributes[index].getValue();
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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
