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
import spiralcraft.lang.BindException;

public class ParentFocusNode
  extends FocusNode
{
  
  private final FocusNode _child;

  public ParentFocusNode(FocusNode child)
  { 
    if (child==null)
    { throw new IllegalArgumentException("ParentFocusNode- child cannot be null");
    }
    _child=child;
    this.hashCode=_child.hashCode*31;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_child};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    FocusNode copy=(FocusNode) _child.copy(visitor);
    if (_child==copy)
    { return this;
    }
    else
    { return new ParentFocusNode(copy);
    }
  }
  
  @Override
  public String reconstruct()
  { return ".";
  }
  
  @Override
  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  { 
    Focus<?> childFocus=_child.findFocus(focus);

    Focus<?> parentFocus=childFocus.getParentFocus();
    if (parentFocus!=null)
    { return parentFocus;
    }
    else
    { 
      StringBuffer out=new StringBuffer();
      dumpTree(out,"");
      throw new BindException("Focus has no parent ("+focus.toString()+")");
    }
  }


  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("ParentFocus");
    if (_child!=null)
    {
      prefix=prefix+"  ";
      _child.dumpTree(out,prefix);
    }
  }
  
  @Override
  protected boolean equalsNode(Node node)
  { return _child.equals( ((ParentFocusNode) node)._child);
  }
  
}
