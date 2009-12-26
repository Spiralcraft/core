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

public class SyntaxNode<T>
  extends Node
{

  private final Node _delegate;
  private final String _prefix;
  private final String _suffix;

  public SyntaxNode(String prefix,Node delegate,String suffix)
  { 
    this._delegate=delegate;
    this._prefix=prefix;
    this._suffix=suffix;
  }
  
  @Override
  public Node[] getSources()
  { return new Node[] {_delegate};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    SyntaxNode<T> copy=new SyntaxNode<T>(_prefix,_delegate.copy(visitor),_suffix);
    if (_delegate==copy._delegate)
    { return this;
    }
    else
    { return copy;
    }
  }

  @Override
  public String reconstruct()
  { 
    return (_prefix!=null?_prefix:"")
      +_delegate.reconstruct()
      +(_suffix!=null?_suffix:"");
  }
  


  @SuppressWarnings("unchecked")
  @Override
  public Channel<T> bind(final Focus<?> focus)
    throws BindException
  { return (Channel<T>) _delegate.bind(focus);
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Syntax ");
    prefix=prefix+"  ";
    if (_suffix!=null)
    { out.append(prefix+_prefix);
    }
    if (_delegate!=null)
    { _delegate.dumpTree(out,prefix);
    }
    if (_suffix!=null)
    { out.append(prefix+_suffix);
    }
  }
  
}
