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

import spiralcraft.log.ClassLogger;


public class ResolveNode<T>
  extends Node
{
  
  private static final ClassLogger log=ClassLogger.getInstance(ResolveNode.class);

  private final Node _source;
  private final String identifier;

  public ResolveNode(Node source,String identifier)
  { 
    _source=source;
    this.identifier=identifier;
  }

  @Override
  public String reconstruct()
  { return _source.reconstruct()+"."+identifier;
  }
  
  public String getIdentifierName()
  { return identifier;
  }

  public Node getSource()
  { return _source;
  }

  @Override
  public Channel<T> bind(final Focus<?> focus)
    throws BindException
  { 
    Channel<?> source;
    if (_source!=null)
    { 
      source=_source.bind(focus);
      if (source==null)
      { throw new BindException(_source+" returned null from bind()");
      }
    }
    else
    { 
      log.fine("ResolveNode:"+super.toString()+" DEFAULT Using Focus subject");
      source=focus.getSubject();
      if (source==null)
      { throw new BindException("Focus "+focus+" has no subject");
      }
    }

    
    Channel<T> ret=source.<T>resolve(focus,identifier,null);
    if (ret==null)
    { throw new BindException("Name '"+identifier+"' not found.");
    }
    return ret;
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Resolve");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
    out.append(prefix).append(".").append(identifier);
  }
  
}
