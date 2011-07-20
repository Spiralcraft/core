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
package spiralcraft.command;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.GenericReflector;

/**
 * <p>Abstract implementation of the CommandFactory interface
 * </p>
 * 
 * <p>Primarily implements the Reflectable.reflect() method by incorporating
 *   specialized command reflectors into the self reflector when applicable. 
 * </p>
 * 
 * @author mike
 *
 * @param <Ttarget>
 * @param <Tcontext>
 * @param <Tresult>
 */
public abstract class AbstractCommandFactory<Ttarget,Tcontext,Tresult>
  implements CommandFactory<Ttarget, Tcontext, Tresult>
{
  protected Reflector<CommandFactory<Ttarget,Tcontext,Tresult>> reflector;

  
  public AbstractCommandFactory()
  {
  }
  
  @Override
  public abstract Command<Ttarget, Tcontext, Tresult> command();


  @Override
  public boolean isCommandEnabled()
  {
    // TODO Auto-generated method stub
    return true;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Class<Command<Ttarget,Tcontext,Tresult>> getCommandClass()
  { 
    Class clazz=Command.class;
    return clazz;
  }
  
  protected Reflector<Tcontext> getContextReflector()
  { return null;
  }

  protected Reflector<Tresult> getResultReflector()
  { return null;
  }
  
  @Override
  public Reflector<? extends Command<Ttarget,Tcontext,Tresult>> 
    getCommandReflector()
      throws BindException
  { 
    Reflector<Command<Ttarget,Tcontext,Tresult>> commandReflector
      =BeanReflector.<Command<Ttarget,Tcontext,Tresult>>getInstance
        (getCommandClass());
    
    final Reflector<Tcontext> contextReflector=getContextReflector();
    final Reflector<Tresult> resultReflector=getResultReflector();
    
    if (contextReflector!=null || resultReflector!=null)
    {
      GenericReflector<Command<Ttarget,Tcontext,Tresult>> gr
        =new GenericReflector<Command<Ttarget,Tcontext,Tresult>>
          (commandReflector.getTypeURI(),commandReflector);
      if (contextReflector!=null)
      { gr.enhance("context",null,contextReflector);
      }
      if (resultReflector!=null)
      { 
        gr.enhance
          ("result"
          ,null
          ,new ChannelFactory<Tresult,Tresult>()
          {
  
            @Override
            public Channel<Tresult> bindChannel(
              Channel<Tresult> source,
              Focus<?> focus,
              Expression<?>[] arguments)
              throws BindException
            {
              return new AspectChannel<Tresult>
                (resultReflector
                ,source
                );
            }
           
           }
         );
      }
      // gr.setDebug(true);
      commandReflector=gr;
    }
    return commandReflector;    
  }
  
  @Override
  public Reflector<CommandFactory<Ttarget, Tcontext, Tresult>> reflect()
    throws BindException
  {
    
    if (reflector==null)
    {
      Reflector<CommandFactory<Ttarget,Tcontext,Tresult>> base
        =BeanReflector.
          <CommandFactory<Ttarget,Tcontext,Tresult>>
            getInstance(getClass());
      
      Reflector<? extends Command<Ttarget,Tcontext,Tresult>> commandReflector
        =getCommandReflector();
      
      if (commandReflector==null
          || commandReflector
               ==BeanReflector
                .<Command<Ttarget,Tcontext,Tresult>>
                  getInstance(Command.class)
         )
      { reflector=base;
      }
      else
      {
        GenericReflector<CommandFactory<Ttarget,Tcontext,Tresult>> gr
          =new GenericReflector<CommandFactory<Ttarget,Tcontext,Tresult>>
            (base.getTypeURI()
            ,base
            );
        gr.enhance("command",new Reflector<?>[0],commandReflector);
        reflector=gr;
        
      }
    } 
    return reflector;
  }

  @Override
  public Channel<Tresult> bindChannel(Focus<?> focus,Channel<?>[] arguments)
    throws BindException
  {
    CommandFunctorChannel<Ttarget,Tcontext,Tresult> channel
      =new CommandFunctorChannel<Ttarget,Tcontext,Tresult>(this);
    channel.bind(focus,arguments);
    return channel;

  }
  
}
