//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.lang.parser;

//import spiralcraft.log.ClassLog;
import java.util.Iterator;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Reflector;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.string.StringConverter;

/**
 * Holds named data elements defined by a Struct expression
 * 
 * @author mike
 *
 */
public class Struct
  implements Iterable<Object>
{
//  private static final ClassLog log
//    =ClassLog.getInstance(Struct.class);
  
// TODO: Implement Reflectable but check for side effects in things that
//   automatically detect and call that interface

  public final StructNode.StructReflector reflector;
  public final Object baseExtent;
  private final Object[] data;
  private boolean frozen;
      
  public Struct(StructNode.StructReflector reflector,Object[] data,Object baseExtent)
  { 
    this.data=data;
    this.reflector=reflector;
    this.baseExtent=baseExtent;
  }
      
  public Reflector<Struct> getReflector()
  { return reflector;
  }
  
  @Override
  public int hashCode()
  { 
    return ArrayUtil.arrayHashCode(data)
      +(baseExtent!=null?13*baseExtent.hashCode():0);
  }

  @Override
  public boolean equals(Object o)
  { 
    if (o==this)
    { return true;
    }
    
    if (o instanceof Struct)
    {
      Struct struct=(Struct) o;
      return reflector.isAssignableFrom(struct.reflector)
        && (baseExtent==null || baseExtent.equals(struct.baseExtent))
        && ArrayUtil.arrayEquals(data,struct.data);
        
    }
    else
    { 
//      log.fine(toString()+"\r\n   !=\r\n    "+o.toString());
      return false;
    }
  }

  public void freeze()
  { frozen=true;
  }
  
  public boolean isFrozen()
  { return frozen;
  }
  
  @Override
  public Iterator<Object> iterator()
  { return ArrayUtil.iterator(data);
  }
  
  public void set(int index,Object val)
  {
    if (!frozen)
    { data[index]=val;
    }
    else
    { throw new AccessException("Struct is frozen, cannot modify data");
    }
  }
  
  public int size()
  { return data.length;
  }
  
  public Object get(int index)
  { return data[index];
  }
  
  /**
   * Obtain a field value by name programmatically
   * 
   * @param fieldName
   * @return
   */
  public Object getValue(String fieldName)
  { return reflector.getValue(this,fieldName);
  }
    
  @SuppressWarnings("unchecked")
  @Override
  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(getClass().getName()+"@"+System.identityHashCode(this)+":"+hashCode()+":("+reflector.getTypeURI()+"):{ ");
    
    
    if (baseExtent!=null)
    { buf.append(" {= "+baseExtent.toString()+" } ");
    }
    
    int i=0;
    for (StructField field : reflector.getFields())
    { 
      if (i>0)
      { buf.append(" , ");
      }
      
      @SuppressWarnings("rawtypes")
      StringConverter converter
        =reflector.getChannel(field).getReflector().getStringConverter();
      if (converter!=null)
      { buf.append(field.name+"=["+converter.toString(data[i])+"]");
      }
      else
      { buf.append(field.name+"=["+ data[i]+"]");
      }
      i++;
    }
    buf.append(" }");
    return buf.toString();
  }
}