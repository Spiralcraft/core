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
package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

import spiralcraft.lang.spi.SimpleChannel;

public class LiteralNode<X>
  extends Node
{

  private final SimpleChannel<X> _optic;

  @SuppressWarnings("unchecked") // Type check
  public LiteralNode(X value)
  { _optic=new SimpleChannel<X>((Class<X>) value.getClass(),value,true);
  }

  public LiteralNode(X value,Class<X> valueClass)
  { _optic=new SimpleChannel<X>(valueClass,value,true);
  }
  
  LiteralNode(SimpleChannel<X> _optic)
  { this._optic=_optic; 
  }

  @Override
  public Node[] getSources()
  { return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { return new LiteralNode<X>(_optic);
  }
  
  @Override
  public String reconstruct()
  { 
    X val=_optic.get();
    return val!=null?val.toString():"null";
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("Literal: ").append(_optic.getContentType().getName())
      .append(":[")
      .append(_optic.get().toString())
      .append("]")
      ;
  }

  @Override
  public synchronized Channel<X> bind(final Focus<?> focus)
    throws BindException
  { 
//    System.out.println("LiteralNode: Returning "+_optic.toString());
    return _optic;
  }
 
  
}
