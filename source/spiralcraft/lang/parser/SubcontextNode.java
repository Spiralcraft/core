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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


/**
 * <p>Represents an Expression bound against a subcontext derived from another 
 *   Expression via a telescoped Focus-
 * </p> 
 *   
 * <p>For example:
 * </p>
 * 
 * <code>expr1{ expr2 }</code>
 *   
 * @author mike
 *
 * @param <T> The projection (return element) type
 * @param <S> The source type
 */
public class SubcontextNode<T,S>
  extends Node
{

  private final Node _source;
  private final Node _subcontext;

  public SubcontextNode(Node source,Node subcontext)
  { 
    _source=source;
    _subcontext=subcontext;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_source,_subcontext};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    SubcontextNode<T,S> copy
      =new SubcontextNode<T,S>
      (_source.copy(visitor),_subcontext.copy(visitor));
    
    if (copy._source==_source && copy._subcontext==_subcontext)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return _source.reconstruct()+" { "+_subcontext.reconstruct()+" } ";
  }
  
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
   
    Channel<S> sourceChannel=focus.<S>bind(new Expression<S>(_source,null));
    Channel<?> result
      =focus.telescope(sourceChannel).bind(new Expression<T>(_subcontext,null));
    return result;
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Subcontext ");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
    out.append(prefix+"{");
    _subcontext.dumpTree(out,prefix+"  ");
    out.append(prefix+"}");
  }
  

}