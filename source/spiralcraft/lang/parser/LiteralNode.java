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
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleBinding;

public class LiteralNode<X>
  extends Node<X>
{

  private final SimpleBinding<X> _optic;

  public LiteralNode(X value,Class<X> valueClass)
  { 
    try
    { 
//      System.out.println("LiteralNode: Creating SimpleBinding with "+value);
      _optic=new SimpleBinding<X>(valueClass,value,true);
    }
    catch (BindException x)
    { throw new IllegalArgumentException(x.toString());
    }
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("Literal: ").append(_optic.getContentType().getName())
      .append(":[")
      .append(_optic.get().toString())
      .append("]");
      ;
  }

  public synchronized Optic<X> bind(final Focus focus)
    throws BindException
  { 
//    System.out.println("LiteralNode: Returning "+_optic.toString());
    return _optic;
  }
}
