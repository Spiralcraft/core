//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.lang;

import java.util.HashMap;
import java.util.HashSet;

public class CompoundFocus<T>
  extends SimpleFocus<T>
{
  private String name;
  private HashSet<String> namespaceAliases;
  private HashMap<String,Focus<?>> foci;

  
  public CompoundFocus
    (Focus<?> parentFocus
    ,String namespace
    ,String name
    ,Channel<T> subject
    ,Channel<?> context
    )
  { 
    setParentFocus(parentFocus);
    addNamespaceAlias(namespace);
    setName(name);
    setSubject(subject);
    setContext(context);
  }
  
  public CompoundFocus()
  {
  }
  
  public void setName(String name)
  { this.name=name;
  }

  /**
   * Specify that this Focus is addressed by the <CODE>[<I>name</I>]</CODE>
   *   operator.
   * 
   * @param name
   */
  public synchronized void addNamespaceAlias(String ... newNames)
  { 
    if (newNames!=null)
    {
      if (namespaceAliases==null)
      { namespaceAliases=new HashSet<String>();
      }
      for (String name: newNames)
      { namespaceAliases.add(name);
      }
    }
  }

  /**
   * Bind a Focus to a name that will referenced via findFocus(), or by the
   *   <CODE>[<I>name</I>]</CODE> or <CODE>[<I>namespace:name</I>]</CODE>
   *   operator in the expression language.
   */
  public synchronized void bindFocus(String name,Focus<?> focus)
    throws BindException
  { 
    if (foci==null)
    { foci=new HashMap<String,Focus<?>>();
    }
    if (foci.get(name)==null)
    { foci.put(name,focus);
    }
    else
    { throw new BindException("Name '"+name+"' already bound");
    }
  }
    
  private boolean hasNamespace(String namespace)
  {
    if (namespace==null)
    { return true;
    }
    else if (namespaceAliases!=null)
    { return namespaceAliases.contains(namespace);
    }
    else
    { return false;
    }
  }

  public Focus<?> findFocus(String namespace,String name)
  { 
    // System.err.println
    //   ("CompoundFocus["+this.namespaceAliases+":"+this.name
    //        +"].findFocus:["+namespace+"]:["+name+"]");
    if (hasNamespace(namespace))
    {

      if (this.name!=null && this.name.equals(name))
      { return this;
      }
      if (foci!=null)
      { 
        Focus<?> focus=foci.get(name);
        if (focus!=null)
        { return focus;
        }
      }
    }
      
    if (parent!=null)
    { return parent.findFocus(namespace,name);
    }
    else
    { return null;
    }
  }

}
