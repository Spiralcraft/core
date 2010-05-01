//
// Copyright (c) 2010 Michael Toth
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

/**
 * <p>An Operation is a generic extension point which allows an Expression 
 *   to be pre-defined and referenced from other Expressions.
 * </p>
 * 
 * @author mike
 *
 * @param <Tchannel>
 * @param <Tsource>
 */
public class Operation<Tchannel, Tsource>
  implements ChannelFactory<Tchannel, Tsource>, FocusChainObject
{
  
  private Expression<Tchannel> expression;
  
  public void setX(Expression<Tchannel> expression)
  { this.expression=expression;
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }

  @Override
  public Channel<Tchannel> bindChannel(
    Channel<Tsource> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    if (!focus.isContext(source) && source!=null)
    { focus=focus.chain(source);
    }
    return focus.bind(expression);
  }

}
