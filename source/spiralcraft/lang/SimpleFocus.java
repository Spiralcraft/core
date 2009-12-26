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


import spiralcraft.common.namespace.PrefixResolver;

/**
 * Simple implementation of a Focus
 */
public class SimpleFocus<T>
  extends BaseFocus<T>
  implements Focus<T>
{

  private boolean namespaceRoot;
  
  public SimpleFocus()
  {
  }

  public SimpleFocus(Channel<T> subject)
  { this.subject=subject;
  }

  public SimpleFocus(Focus<?> parentFocus,Channel<T> subject)
  { 
    setParentFocus(parentFocus);
    this.subject=subject;
  }

    
  /**
   * Indicate that namespace resolution should end at this Focus node.
   * 
   * @param val
   */
  public void setNamespaceRoot(boolean val)
  { this.namespaceRoot=val;
  }

  
//  public synchronized void mapNamespace(String name,URI namespace)
//  {
//    if (namespaces==null)
//    { namespaces=new HashMap<String,URI>();
//    }
//    namespaces.put(name,namespace);
//  }
  
  


  @Override
  public PrefixResolver getNamespaceResolver()
  { 
    if (namespaceResolver!=null)
    { return namespaceResolver;
    }
    else if (!namespaceRoot && parent!=null)
    { return parent.getNamespaceResolver();
    }
    return null;
  }
  
}
