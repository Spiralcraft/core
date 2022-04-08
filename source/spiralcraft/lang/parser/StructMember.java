//
// Copyright (c) 2022 Michael Toth
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

import java.util.ArrayList;

/**
 * Represents the declaration of a struct member
 * 
 * @author mike
 *
 */
public class StructMember
{
  String name;
  Node type;
  Node source;
  Node sourceFactory;
  boolean passThrough;
  boolean resolveInParent;
  
  void addSources(ArrayList<Node> ret)
  {  
    if (type!=null)
    { ret.add(type);
    }
    if (source!=null)
    { ret.add(source);
    }
    if (source!=null)
    { ret.add(sourceFactory);
    }
  }
  
  public Node getSource()
  { return source;
  }
  
  public StructMember copy(Object visitor)
  {
    StructMember copy=new StructMember();
    copy.name=name;
    if (type!=null)
    { copy.type=type.copy(visitor);
    }
    if (source!=null)
    { copy.source=source.copy(visitor);
    }
    if (sourceFactory!=null)
    { copy.sourceFactory=sourceFactory.copy(visitor);
    }
    copy.resolveInParent=resolveInParent;
    copy.passThrough=passThrough;
    if (copy.type==type && copy.source==source && copy.sourceFactory==sourceFactory)
    { return this;
    }
    return copy;
  }
  
  public void reconstruct(StringBuilder builder)
  {
    if (name!=null)
    {
      builder.append(name)
        .append(" : ");
      if (type!=null)
      { builder.append(type.reconstruct());
      }
      if (source!=null)
      { 
        builder.append(passThrough?"~":"=")
          .append(source.reconstruct());
      }
    }
    else if (source!=null)
    { builder.append(source.reconstruct());
    }
  
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  {
    out.append("StructMember  ");
    if (name==null)
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