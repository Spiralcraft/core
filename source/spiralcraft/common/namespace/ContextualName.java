//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.common.namespace;

import spiralcraft.common.Immutable;

/**
 * <p>A String which resolves to a QName on construction
 * </p>
 * 
 * @author mike
 *
 */
@Immutable
public class ContextualName
{

  private final QName qName;
  private final PrefixedName pName;
  
  public ContextualName(String prefixedName,PrefixResolver resolver)
    throws UnresolvedPrefixException
  {
    NamespaceContext.push(resolver);
    try
    { 
      if (prefixedName.startsWith("{"))
      { 
        pName=null;
        qName=new QName(prefixedName);
      }
      else
      {
        pName=new PrefixedName(prefixedName);
        qName=pName.resolve();
      
      }
    }
    finally
    { NamespaceContext.pop();
    }
      
  }

  public ContextualName(String prefixedName)
    throws UnresolvedPrefixException
  {
    if (prefixedName.startsWith("{"))
    { 
      pName=null;
      qName=new QName(prefixedName);
    }
    else
    {
      pName=new PrefixedName(prefixedName);
      qName=pName.resolve();
      
    }
    
  }
  
  public PrefixedName getPrefixedName()
  { return pName;
  }
  
  public QName getQName()
  { return qName;
  }
  
  @Override
  public String toString()
  { return qName.toString();
  }
}
