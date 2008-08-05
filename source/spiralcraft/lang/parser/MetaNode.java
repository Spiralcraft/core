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

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import spiralcraft.lang.spi.SimpleChannel;

/**
 * Exposes metadata to expressions. Allows a binding to expose a metadata
 *   binding, and defaults to a bean reference to the source binding.
 *  
 * @author mike
 *
 * @param <T>
 */
public class MetaNode<T extends Channel<?>>
  extends Node
{

  private final Node _source;

  public MetaNode(Node source)
  { _source=source;
  }
  
  @Override
  public String reconstruct()
  { return _source+"@";
  }

  public Node getSource()
  { return _source;
  }

  @Override
  @SuppressWarnings("unchecked") // Heterogeneous operation
  public Channel<T> bind(final Focus focus)
    throws BindException
  { 
    Channel sourceOptic;
    if (_source!=null)
    { 
      sourceOptic=_source.bind(focus);
      if (sourceOptic==null)
      { throw new BindException(_source+" returned null from bind()");
      }
    }
    else
    { 
      System.out.println("MetaNode:"+super.toString()+" DEFAULT Using Focus subject");
      sourceOptic=focus.getSubject();
      if (sourceOptic==null)
      { throw new BindException("Focus "+focus+" has no subject");
      }
    }

    
    Channel<T> ret=sourceOptic.resolve(focus,"!",null);
    if (ret==null)
    { return new SimpleChannel<T>((T) sourceOptic,true);
    }
    return ret;
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Meta");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
  }
  
}
