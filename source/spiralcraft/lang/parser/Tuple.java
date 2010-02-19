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
import spiralcraft.util.ArrayUtil;

/**
 * Holds named data elements defined by a Tuple expression
 * 
 * @author mike
 *
 */
public class Tuple
{
//  private static final ClassLog log
//    =ClassLog.getInstance(Tuple.class);
  
  public final Object[] data;
  public final TupleNode.TupleReflector reflector;
  public final Object baseExtent;
      
  public Tuple(TupleNode.TupleReflector reflector,Object[] data,Object baseExtent)
  { 
    this.data=data;
    this.reflector=reflector;
    this.baseExtent=baseExtent;
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
    
    if (o instanceof Tuple)
    {
      Tuple tuple=(Tuple) o;
      return reflector==tuple.reflector
        && (baseExtent==null || baseExtent.equals(tuple.baseExtent))
        && ArrayUtil.arrayEquals(data,tuple.data);
        
    }
    else
    { 
//      log.fine(toString()+"\r\n   !=\r\n    "+o.toString());
      return false;
    }
  }
  
  @Override
  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(super.toString()+":("+reflector.getTypeURI()+"):{ ");
    
    
    if (baseExtent!=null)
    { buf.append(" {= "+baseExtent.toString()+" } ");
    }
    
    int i=0;
    for (TupleField field : reflector.getFields())
    { 
      if (i>0)
      { buf.append(" , ");
      }
      buf.append(field.name+"=["+data[i]+"]");
      i++;
    }
    buf.append(" }");
    return buf.toString();
  }
}