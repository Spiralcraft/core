//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.command;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;

/**
 * A CommandFactory that gets its Command from the FocusChain
 * 
 * @author mike
 *
 */
public class BoundCommandFactory<T,R>
  implements CommandFactory<T,R>,FocusChainObject
{

  private Channel<Command<T,R>> commandChannel;
  private final Expression<Command<T,R>> commandX;
  
  public BoundCommandFactory(Expression<Command<T,R>> commandExpression)
  { this.commandX=commandExpression;
  }
  
  @Override
  public Command<T, R> command()
  { return commandChannel.get();
  }

  @Override
  public boolean isCommandEnabled()
  { return true;
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    commandChannel=focusChain.bind(commandX);
    return focusChain;
  }

}
