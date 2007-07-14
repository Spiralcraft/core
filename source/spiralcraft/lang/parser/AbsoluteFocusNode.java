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

/**
 * An expression node which resolves a Focus from somewhere in the hierarchy
 *   using a qualified name (ie. <code>[<I>namespace</I>:<I>name</I>]</code>)
 * 
 * @author mike
 *
 */
public class AbsoluteFocusNode
  extends FocusNode
{

  private final String name;
  private final String namespace;

  public AbsoluteFocusNode(String qname)
  { 
    int colonPos=qname.indexOf(':');
    if (colonPos>-1)
    {
      this.namespace=qname.substring(0,colonPos);
      this.name=qname.substring(colonPos+1);
    }
    else
    { 
      this.namespace=null;
      this.name=qname;
    }

  }

  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  { 
    if (namespace==null || namespace.equals(""))
    { return focus;
    }
    
    Focus<?> newFocus=focus.findFocus(namespace,name);
    if (newFocus!=null)
    { return newFocus;
    }
    else
    { throw new BindException("Focus '"+namespace+":"+name+"' not found.");
    }
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Focus");
    prefix=prefix+"  ";
    out.append(prefix).append("namespace="+(namespace!=null?namespace:"(default)"));
    if (name!=null)
    { out.append(prefix).append("name="+name);
    }
  }
  
}
