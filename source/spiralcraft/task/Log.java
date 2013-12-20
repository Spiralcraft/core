//
// Copyright (c) 2013 Michael Toth
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
package spiralcraft.task;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.util.string.StringConverter;

public class Log<Tcontext,Tmsg>
  extends Scenario<Tcontext,Tmsg>
{

  private Binding<Tmsg> message;
  private Level level=Level.INFO;
  private StringConverter<Tmsg> converter;
  
  public void setX(Binding<Tmsg> x)
  { this.message=x;
  }
  
  public void setLevel(Level level)
  { this.level=level;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bindImports(Focus<?> focus)
    throws ContextualException
  {
    if (message!=null)
    { 
      message.bind(focus);
    
      converter=message.getReflector().getStringConverter();
      if (converter==null)
      { converter=(StringConverter<Tmsg>) StringConverter.getOneWayInstance();
      }
    }
    return super.bindImports(focus);
  }
  
  @Override
  protected Task task()
  { 
    return new AbstractTask()
    {
      @Override
      protected void work()
        throws InterruptedException
      { 
        if (message!=null)
        { 
          log.log(level,getDeclarationInfo()+": "
              +converter.toString(message.get())
              );  
        }

      }
    };
  
  }

}
