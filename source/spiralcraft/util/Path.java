package spiralcraft.util;

import java.lang.reflect.Array;
import java.util.LinkedList;

/**
 * An immutable sequence of Strings which represent elements in a hierarchy.
 */ 
public class Path
{
  private final String[] _elements;
  private final int _hashCode;
  
  public Path()
  { 
    _elements=new String[0];
    _hashCode=0;
  }
  
  public Path(String source,char delimiter)
  { 
    _elements=StringUtil.tokenize(source,Character.toString(delimiter));
    _hashCode=ArrayUtil.arrayHashCode(_elements);
  }
  
  public Path(String[] elements)
  { 
    _elements=elements;
    _hashCode=ArrayUtil.arrayHashCode(_elements);
  }

  public Path append(String[] elements)
  { return new Path( (String[]) ArrayUtil.appendArrays(_elements,elements));
  }

  public Path append(String element)
  { return new Path( (String[]) ArrayUtil.append(_elements,element));
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
  
  public String toString()
  { return super.toString()+":"+ArrayUtil.formatToString(_elements,",","\"");
  }
}
