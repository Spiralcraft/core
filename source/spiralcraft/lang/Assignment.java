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

/**
 * <p>Represents the assignment of the value obtained from the source Expression
 *   to the target Expression. 
 * </p>
 * 
 * @author mike
 *
 */
public class Assignment<T>
{
  private Expression<? extends T> source;
  private Expression<T> target;
  
  public void setSource(Expression<? extends T> source)
  { this.source=source;
  }

  public void setTarget(Expression<T> target)
  { this.target=target;
  }
  
  public Expression<? extends T> getSource()
  { return source;
  }

  public Expression<T> getTarget()
  { return target;
  }
  
  public Setter<T> bind(Focus<?> focus)
    throws BindException
  {
    Channel<? extends T> sourceChannel=focus.bind(source);
    Channel<T> targetChannel=focus.bind(target);
    return new Setter<T>(sourceChannel,targetChannel);
  }
}
