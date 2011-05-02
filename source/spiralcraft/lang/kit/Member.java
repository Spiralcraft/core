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
package spiralcraft.lang.kit;

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

/**
 * <p>A named transformation implemented by a Reflector.
 * </p>
 *
 *
 * @author mike
 *
 * @param <Tresult>
 * @param <Tsource>
 */
public abstract class Member
  <Treflector extends Reflector<Tsource>,Tresult,Tsource>
{
  
  protected String name;
  
  public String getName()
  { return name;
  }
  
  /**
   * <p>Creates a new Channel given a Focus
   * </p>
   * 
   * @param focus
   * @return
   * @throws BindException
   */
  public abstract Channel<Tresult> resolve
    (Treflector reflector
    ,Channel<Tsource> source
    ,Focus<?> focus
    ,Expression<?>[] arguments
    )
    throws BindException;

  
  protected void assertRequiresSingleArgument(Expression<?>[] args,URI typeURI)
    throws BindException
  {
    if (args==null || args.length!=1)
    { 
      throw new BindException
        (name +" requires a single argument of type "+typeURI.toString()
        );
    }
  }
  
  protected void assertNoArguments(Expression<?>[] args)
    throws BindException
  {    
    if (args!=null && args.length>0)
    { throw new BindException(name+" does not accept any arguments");
    }
  }
  
}
