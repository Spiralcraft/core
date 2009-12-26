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

/**
 * <P>A Node which binds to the Context of the provided Focus
 * 
 */
public class ContextNode
  extends Node
{

  private final FocusNode _source;

  public ContextNode(FocusNode source)
  { _source=source;
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    if (_source!=null)
    { 
      FocusNode copy=(FocusNode) _source.copy(visitor);
      if (copy==_source)
      { return this;
      }
      else
      { return new ContextNode(copy);
      }
    }
    else
    { return this;
    }
  }

  @Override
  public String reconstruct()
  { return _source!=null?_source.reconstruct()+" ":"";
  }
  
  public FocusNode getSource()
  { return _source;
  }
  
  @Override
  public Node[] getSources()
  { return new Node[] {_source};
  }
  
  @Override
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 

    Focus<?> specifiedFocus
      =_source!=null
      ?_source.findFocus(focus)
      :focus;

    Channel<?> ret=specifiedFocus.getContext();
    
    if (ret==null)
    { ret=specifiedFocus.getSubject();
    }
    
    if (ret==null)
    {
      throw new BindException
        ("Focus does not have a Subject or a Context:"
        + specifiedFocus.toString()
        );
    }
    return ret;  
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Context");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }

  }
  
}
