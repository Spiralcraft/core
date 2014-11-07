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

/**
 * Defines a field element of a Struct
 * 
 * @author mike
 *
 */
public class StructField
{
  int index;
  String name;
  Node type;
  Node source;
  boolean passThrough;
  boolean anonymous;
  boolean linked;
  
  public StructField copy(Object visitor)
  {
    StructField copy=new StructField();
    copy.index=index;
    copy.name=name;
    if (type!=null)
    { copy.type=type.copy(visitor);
    }
    if (source!=null)
    { copy.source=source.copy(visitor);
    }
    copy.passThrough=passThrough;
    copy.anonymous=anonymous;
    if (copy.type==type && copy.source==source)
    { return this;
    }
    return copy;
  }
  
  public String getName()
  { return name;
  }
  
  public Node getSource()
  { return source;
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append("StructField  ");
    if (anonymous)
    { out.append("(anon)");
    }
    out.append(name)
      .append(" : ");
    prefix=prefix+"  ";
    if (type!=null)
    { type.dumpTree(out,prefix);
    }
    else if (source!=null)
    { 
      out.append(passThrough?"~":"=");
      source.dumpTree(out,prefix);
    }  
  }
  
  @Override
  public String toString()
  { 
    StringBuffer out=new StringBuffer();
    dumpTree(out,"  ");
    return out.toString();
  }
}
