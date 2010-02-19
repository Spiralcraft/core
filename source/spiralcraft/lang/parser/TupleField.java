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
 * Defines a field element of a Tuple
 * 
 * @author mike
 *
 */
public class TupleField
{
  int index;
  String name;
  TypeFocusNode type;
  Node source;
  boolean passThrough;
  boolean anonymous;
  
  public TupleField copy(Object visitor)
  {
    TupleField copy=new TupleField();
    copy.index=index;
    copy.name=name;
    if (type!=null)
    { copy.type=(TypeFocusNode) type.copy(visitor);
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
  
  public Node getSource()
  { return source;
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append("TupleField  ");
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
