//
// Copyright (c) 2009,2009 Michael Toth
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
 * <p>Wraps a Focus to provide additional namespace mappings
 * </p>
 * 
 * @author mike
 *
 * @param <tFocus>
 */
public class NamespaceFocus<T>
  extends FocusWrapper<T>
{ 
  private PrefixResolver resolver;
  
  public NamespaceFocus(Focus<T> delegate,final PrefixResolver resolver)
  { 
    super(delegate);
    this.resolver=resolver;
  }
  
  @Override
  public PrefixResolver getNamespaceResolver()
  { return resolver;
  }

  @Override
  public String toString()
  { return super.toString()+"\r\n  ns="+resolver;
  }
}
