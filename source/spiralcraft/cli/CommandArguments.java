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
package spiralcraft.cli;

import java.util.ArrayList;

import spiralcraft.command.Command;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.util.Configurator;
import spiralcraft.util.string.StringConverter;

/**
 * Apply arguments to the object in a channel.
 */
public class CommandArguments<T>
  extends ReflectorArguments<T>
{
  
  private ArrayList<String> arguments=new ArrayList<String>();
  private T context;
  private Reflector<T> reflector;
  private Command<?,T,?> command;
  
  
  public CommandArguments(Reflector<T> reflector,Command<?,T,?> command)
  { 
    super(new Configurator<T>(reflector));
    this.context=command.getContext();
    configurator.set(context);
    this.reflector=reflector;
    this.command=command;
  }
  
  
  @Override
  protected boolean processOption(String option)
  {
    if (context==null)
    {
      throw new IllegalArgumentException
        ("Only a single argument of type "+reflector.getTypeURI()+" is "
          +"accepted here"
        );
    }
    
    if (!arguments.isEmpty())
    { 
      throw new IllegalArgumentException
        ("Option "+option+" must preceed positional argument "+arguments.get(0));
    }
    return super.processOption(option);
  }
  
  

  
  @Override
  protected boolean processArgument(String arg)
  { 
    if (context==null && !arguments.isEmpty())
    {
      throw new IllegalArgumentException
        ("Only a single argument of type "+reflector.getTypeURI()+" is "
        +"accepted here"
        );
    }
    arguments.add(arg);
    return true;
  }
  
  @Override
  protected void completed()
  {
    if (context==null)
    { 
      if (!arguments.isEmpty())
      { 
        StringConverter<T> converter
          =reflector.getStringConverter();
        if (converter!=null)
        { 
          context=converter.fromString(arguments.get(0));
          command.setContext(context);
          
        }
        else
        { 
          throw new IllegalArgumentException
            ("Unable to convert argument '"+arguments.get(0)+"' to type "
            +reflector.getTypeURI()
            );
        }
      }
    }
    else
    { 
      super.completed();
      if (!arguments.isEmpty())
      { 
        int i=0;
        for (String argument:arguments)
        {
          String key="_"+i;
          configurator.set(key,argument);
        }        
      }
      command.setContext(context);
    }
  }

}
