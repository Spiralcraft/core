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
package spiralcraft.util;


/**
 * An immutable sequence of Strings which represent elements in a hierarchy.
 */ 
public class Path
{
  private boolean _absolute=false;
  private final String[] _elements;
  private final int _hashCode;
  
  public Path()
  { 
    _elements=new String[0];
    _hashCode=0;
  }
  
  public Path(String source,char delimiter)
  { 
    if (source.startsWith(Character.toString(delimiter)))
    { _absolute=true;
    }
    _elements=StringUtil.tokenize(source,Character.toString(delimiter));
    _hashCode=ArrayUtil.arrayHashCode(_elements)*(_absolute?13:1);
  }
  
  public Path(String[] elements,boolean absolute)
  { 
    _absolute=absolute;
    _elements=elements;
    _hashCode=ArrayUtil.arrayHashCode(_elements)*(_absolute?13:1);
  }

  public Path append(String[] elements)
  { return new Path( (String[]) ArrayUtil.appendArrays(_elements,elements),_absolute);
  }

  public Path append(String element)
  { return new Path( (String[]) ArrayUtil.append(_elements,element),_absolute);
  }
  
  public String lastElement()
  { 
    if (_elements.length==0)
    { return null;
    }
    else
    { return _elements[_elements.length-1];
    }
  }
  
  public Path parentPath()
  { 
    if (_elements.length>1)
    { 
      String[] newElements=new String[_elements.length-1];
      System.arraycopy(_elements,0,newElements,0,newElements.length);
      return new Path(newElements,_absolute);
    }
    else
    { return new Path();
    }
  }
  
  public String getElement(int index)
  { return _elements[index];
  }
  
  public int size()
  { return _elements.length;
  }
  
  public String[] elements()
  { return _elements;
  }
  
  public int hashCode()
  { return _hashCode;
  }
  
  public boolean equals(Path path)
  { 
    if (path==null)
    { return false;
    }
    return ArrayUtil.arrayEquals(_elements,path.elements());
  }
  
  public String format(String delimiter)
  { return (_absolute?delimiter:"")+ArrayUtil.format(_elements,delimiter,null);
  }
  
  public String toString()
  { return super.toString()+":"+ArrayUtil.format(_elements,",","\"");
  }
}
