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

import java.util.ArrayList;
import java.util.Iterator;

import spiralcraft.util.string.StringUtil;

import spiralcraft.annotation.PermittedDependency;

/**
 * An immutable sequence of Strings which represent elements in an abstract hierarchy.
 */ 
public class Path
  implements Iterable<String>
{
  private boolean _absolute=false;
  private boolean _container=false;
  private final String[] _elements;
  private final int _hashCode;
  private char _delimiter;
  
  /**
   * Construct an empty Path
   */
  public Path()
  { 
    _elements=new String[0];
    _hashCode=0;
  }

  /**
   * Construct a path made up of a set of tokens embedded in a String and
   *   separated by the specified delimiter
   */
  @PermittedDependency(StringUtil.class)
  public Path(String source,char delimiter)
  { 
    if (source.startsWith(Character.toString(delimiter)))
    { _absolute=true;
    }
    if (source.endsWith(Character.toString(delimiter)))
    { _container=true;
    }
    _delimiter=delimiter;
    
    ArrayList<String> elements=new ArrayList<String>();
    StringUtil.tokenize(source,Character.toString(delimiter),elements,false);
    if (elements.size()>0 && elements.get(0).equals(""))
    { elements.remove(0);
    }
    _elements=elements.toArray(new String[elements.size()]);
    _hashCode=computeHash();
  }
  
  private int computeHash()
  { return ArrayUtil.arrayHashCode(_elements)*(_absolute?13:1)*(_container?13:1);
  }
  
  /**
   * Construct a path made up of the set of tokens in the specified String[].
   */
  public Path(String[] elements,char delimiter,boolean absolute,boolean container)
  { 
    _absolute=absolute;
    _elements=elements;
    _delimiter=delimiter;
    _container=container;
    _hashCode=computeHash();
  }

  /**
   * Construct a path made up of the set of tokens in the specified String[].
   */
  public Path(String[] elements,boolean absolute)
  { 
    _absolute=absolute;
    _elements=elements;
    _delimiter='/';
    _container=false;
    _hashCode=computeHash();
  }

  /**
   * Append the specified array of elements to the path 
   */
  public Path append(String[] elements)
  { 
    return new Path
      ( (String[]) ArrayUtil.concat(_elements,elements)
      ,_delimiter
      ,_absolute
      ,false
      );
  }
  
  /**
   * Append the specified array of elements to the path 
   */
  public Path append(String[] elements,boolean container)
  { 
    return new Path
      ( (String[]) ArrayUtil.concat(_elements,elements)
      ,_delimiter
      ,_absolute
      ,container
      );
  }

  /**
   * Append a single element to the path
   */
  public Path append(String element)
  { 
    return new Path
      ( (String[]) ArrayUtil.append(_elements,element)
      ,_delimiter
      ,_absolute
      ,false
      );
  }
  
  /**
   * Append a single element to the path
   */
  public Path append(String element,boolean container)
  { 
    return new Path
      ( (String[]) ArrayUtil.append(_elements,element)
      ,_delimiter
      ,_absolute
      ,container
      );
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

  public String firstElement()
  {
    if (_elements.length==0)
    { return null;
    }
    else
    { return _elements[0];
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
      return new Path(newElements,_delimiter,_absolute,true);
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
      return new Path(newElements,_delimiter,false,_container);
    }
    else
    { return new Path();
    }
  }
  
  /**
   * Determine whether this path starts with the specified path
   * 
   * @param prefix
   * @return
   */
  public boolean startsWith(Path prefix)
  {
    if (prefix.isAbsolute() != isAbsolute())
    { return false;
    }
    int i=0;
    for (String element:prefix.elements())
    { 
      if (!_elements[i++].equals(element))
      { return false;
      }
    }
    return true;
  }
  
  public Path relativize(Path suffixed)
  { 
    if (suffixed.isAbsolute() != isAbsolute())
    { return null;
    }
    
    
    int i=0;
    for (String element:suffixed.elements())
    { 
      if (i==_elements.length)
      { return suffixed.subPath(i);
      }
      else if (!_elements[i++].equals(element))
      { return null;
      }
    }
    return new Path("",'/');
  }
  
  /**
   * Determine whether the specified name exactly matches a segment in the
   *   path.  
   * 
   * @param element
   * @return Whether the path contains an element matching the
   *   specified name
   */
  public boolean containsElement(String name)
  { 
    for (String element: _elements)
    { 
      if (element!=null && element.equals(name))
      { return true;
      }
    }
    return false;
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
   * @return Whether the ends with a trailing delimiter, signifying a
   *   container
   */
  public boolean isContainer()
  { return _container;
  }
  
  public String format(char separator)
  { return format(Character.toString(separator));
  }
  
  /**
   * 
   * @return A String representation of the path with the elements separated by the
   *   specified separator string
   */
  public String format(String separator)
  { 
    return (_absolute?separator:"")
           +ArrayUtil.format(_elements,separator,null)
           +(_container?separator:"");
  }
  
  public Iterator<String> iterator()
  { return ArrayUtil.<String>iterator(_elements);
  }
  
  @Override
  public String toString()
  { return format(new String(new char[] {_delimiter}));
  }
}
