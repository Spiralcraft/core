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
package spiralcraft.lang;

import java.net.URI;

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.BeanReflector;

/**
 * <p>A Focus which provides access to static members of a Java class.
 * </p>
 * 
 * <p>This Focus response to searches in the form <code>
 *   [static:<i>&lt;classname&gt;</i>] </code> 
 *   (eg. <code>[static:foo.bar.myclass] </code>)
 * </p>
 */
public class StaticFocus<T>
  extends BaseFocus<T>
{
  
  private final StaticChannel<T> binding;
  
  public StaticFocus(Focus<?> parentFocus,Class<T> clazz)
    throws BindException
  { 
    setParentFocus(parentFocus);
    binding=new StaticChannel<T>(clazz);
    setSubject(binding);
  }

  @SuppressWarnings("unchecked") // Cast for requested interface
  @Override
  public <X> Focus<X> findFocus(URI specifier)
  {
    
    if (isFocus(specifier))
    { return (Focus<X>) this;
    }
    
    if (parent!=null)
    { return parent.<X>findFocus(specifier);
    }
    
    return null;
  }

  @Override
  public boolean isFocus(
    URI specifier)
  {
    return 
      specifier.getScheme()!=null 
        && specifier.getSchemeSpecificPart()!=null
        && specifier.getScheme().equals("static")
        && binding.getContentType().getName().equals
          (specifier.getSchemeSpecificPart())
    ;
       
  }

}

class StaticChannel<T>
  extends AbstractChannel<T>
{

  public StaticChannel(Class<T> clazz)
    throws BindException
  { super(BeanReflector.<T>getInstance(clazz));
  }
  
  @Override
  protected T retrieve()
  { return null;
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { return false;
  }
}
