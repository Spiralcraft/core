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
    return copy;
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append("TupleField  ");
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
}
