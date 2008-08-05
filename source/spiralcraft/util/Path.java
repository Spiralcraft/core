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

import java.util.Iterator;

/**
 * An immutable sequence of Strings which represent elements in an abstract hierarchy.
 */ 
public class Path
  implements Iterable<String>
{
  private boolean _absolute=false;
  private final String[] _elements;
  private final int _hashCode;
  
  /**
   * Construct an empty Path
   */
  public Path()
  { 
    _elements=new String[0];
    _hashCode=0;
  }

  /**
   * Construct a path made up of a set of tokens embedded in a String and separated by
   *   the specified delimiter
   */
  public Path(String source,char delimiter)
  { 
    if (source.startsWith(Character.toString(delimiter)))
    { _absolute=true;
    }
    _elements=StringUtil.tokenize(source,Character.toString(delimiter));
    _hashCode=ArrayUtil.arrayHashCode(_elements)*(_absolute?13:1);
  }
  
  /**
   * Construct a path made up of the set of tokens in the specified String[].
   */
  public Path(String[] elements,boolean absolute)
  { 
    _absolute=absolute;
    _elements=elements;
    _hashCode=ArrayUtil.arrayHashCode(_elements)*(_absolute?13:1);
  }

  /**
   * Append the specified array of elements to the path 
   */
  public Path append(String[] elements)
  { return new Path( (String[]) ArrayUtil.appendArrays(_elements,elements),_absolute);
  }

  /**
   * Append a single element to the path
   */
  public Path append(String element)
  { return new Path( (String[]) ArrayUtil.append(_elements,element),_absolute);
  }

  /**
   * 
   * @return the last element in the path
   */
  public String lastElement()
  { 
    if (_elements.length==0)
    { return null;
    }
    else
    { return _elements[_elements.length-1];
    }
  }

  /**
   * 
   * @return the path up to, but not including the last element
   */
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
  
  /**
   *@return the sub-path starting at the specified element
   */
  public Path subPath(int startElement)
  { 
    if (_elements.length>startElement)
    { 
      String[] newElements=new String[_elements.length-startElement];
      System.arraycopy(_elements,startElement,newElements,0,newElements.length);
      return new Path(newElements,false);
    }
    else
    { return new Path();
    }
  }
  
  /**
   * 
   * @return The element at the specified index in the path
   */
  public String getElement(int index)
  { return _elements[index];
  }
  
  /**
   * 
   * @return The number of elements in the path
   */
  public int size()
  { return _elements.length;
  }
  
  /**
   * 
   * @return The elements as a String[]
   */
  public String[] elements()
  { return _elements;
  }
  
  @Override
  public int hashCode()
  { return _hashCode;
  }
  
  @Override
  public boolean equals(Object o)
  { 
    if (!(o instanceof Path))
    { return false;
    }
    Path path=(Path) o;
    if (path==null)
    { return false;
    }
    if (path.isAbsolute()!=_absolute)
    { return false;
    }
    return ArrayUtil.arrayEquals(_elements,path.elements());
  }
  
  /**
   * @return Whether the path begins at a 'root' (not reprented as an element)
   */
  public boolean isAbsolute()
  { return _absolute;
  }
  
  /**
   * 
   * @return A String representation of the path with the elements separated by the
   *   specified separator string
   */
  public String format(String separator)
  { return (_absolute?separator:"")+ArrayUtil.format(_elements,separator,null);
  }
  
  public Iterator<String> iterator()
  { return ArrayUtil.<String>iterator(_elements);
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+ArrayUtil.format(_elements,",","\"");
  }
}
