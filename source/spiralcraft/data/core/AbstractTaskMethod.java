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
package spiralcraft.data.core;


import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.AssignmentChannel;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Scenario;
import spiralcraft.task.TaskCommand;

/**
 * Allows Task scenarios to implement data methods
 * 
 * @author mike
 *
 * @param <T>
 * @param <C>
 * @param <R>
 */
public abstract class AbstractTaskMethod<T,C,R>
  extends MethodImpl
{

  private boolean returnCommand;
  private Expression<Type<R>> returnTypeX;
  private boolean throwException;
  
  
  public void setReturnTypeX(Expression<Type<R>> returnTypeX)
  { this.returnTypeX=returnTypeX;
  }
  
  @Override
  public void subclassResolve()
    throws DataException
  { 
    if (this.returnType==null)
    { 
      if (returnTypeX!=null)
      { 
        try
        { 
          Binding<Type<R>> returnTypeX=new Binding<>(this.returnTypeX);
          returnTypeX.bind(selfFocus);
          if (!Type.class.isAssignableFrom(returnTypeX.getReflector().getContentType()))
          { 
//            if (returnTypeX.getReflector() instanceof DataReflector
//                && Type.class.isAssignableFrom
//                  (((DataReflector<?>) returnTypeX.getReflector()).getType().getNativeClass()
//                  )
//                )
//            { 
//              Tuple returnTypeT=(Tuple) returnTypeX.get();
//              if (returnTypeT !=null )
//              { returnType = (Type<?>) returnTypeT.getType().fromData(returnTypeT,null);
//              }
//            }
//            else
//            {
//              log.fine("Reflector is "+returnTypeX.getReflector());
//              log.fine(Type.class+" notassignable from "+returnTypeX.getReflector().getContentType());
              throw new DataException
                ("ReturnTypeX must resolve to a Type, not a "
                  +returnTypeX.getReflector().getTypeURI()
                  +" ("+returnTypeX.getReflector().getContentType().getName()+")"
                ,getDeclarationInfo()
                );
//            }
          }
          else
          { returnType=returnTypeX.get();
          }
          
          if (returnType==null)
          { 
            if (!this.getDataType().isAbstract())
            {
              throw new DataException
                ("Resolved null return type for "+getURI(),getDeclarationInfo());
            }
          } 
        }
        catch (BindException x)
        { 
          throw new DataException
            ("Error resolving return type for "+getURI(),getDeclarationInfo(),x);
        }
      }
    }
    
    if (this.returnType==null && !generic)
    { 
      log.warning("Method "+getURI()+" has no return type. This usage is"
        +" no longer supported"
        );
      this.returnType=ReflectionType.canonicalType(TaskCommand.class);
      this.returnCommand=true;
    }
  }
  
  /**
   * <p>Indicate that an exception should be thrown if the Command results in
   *   an exception.
   * </p>
   * 
   * <p>A value of false will cause any exception to be logged and the method
   *   will return null
   * </p>
   * 
   * @param throwException
   */
  public void setThrowException(boolean throwException)
  { this.throwException=throwException;
  }
  
  @Override
  public MethodImpl extend()
  {
    @SuppressWarnings("unchecked")
    AbstractTaskMethod<T,C,R> m=(AbstractTaskMethod<T,C,R>) super.extend();
    m.returnTypeX=returnTypeX;
    return m;
  }
  
  protected abstract Focus<Scenario<C,R>> bindTask(
    Focus<?> context,
    Channel<?> source,
    Channel<?>[] params)
    throws BindException;

  @SuppressWarnings({ "unchecked" })
  @Override
  public Channel<?> bind(
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  {
    
    // Use original binding context, never bind source expression in
    //   argument context
    Focus<?> context=source.getContext();
    if (context==null)
    { 
      throw new BindException
        ("No context for "+this+" "+source);
      // context=argFocus;
    }

    
    context=context.chain(source);
    if (source.getReflector() instanceof DataReflector)
    { 
      try
      { 
        context.addFacet
          (((DataReflector<?>) source.getReflector()).getType().getSelfFocus());
      }
      catch (DataException x)
      { throw new BindException("Error reflecting containing type ",x);
      }

    }
        

    context=bindTask(context,source,params);
    
    Channel<Scenario<C,R>> scenarioChannel
      =(Channel<Scenario<C,R>>) context.getSubject();
    
    final Channel<TaskCommand<C,R>> commandChannel
      =new TaskMethodChannel
        (scenarioChannel.<TaskCommand<C,R>>resolve
          (context
          ,"command"
          ,new Expression<?>[0]
          )
        ,params
        ,context
        );

    Scenario<C,R> scenario=scenarioChannel.get();

    if (returnCommand)
    { 
      log.warning("DEPRECATED: Returning Command Channel for "+getURI()+"."
        +" The returnCommand parameter in method is no longer supported"
      );
      return commandChannel;
    }
    else
    { 
      
      if (debug)
      { log.fine("Returning Exec Channel for "+getURI());
      }
      return new ExecChannel(scenario,context,commandChannel);
    } 

  }
  
  public class TaskMethodChannel
    extends SourcedChannel<TaskCommand<C,R>,TaskCommand<C,R>>
  {
    private final ThreadLocalChannel<C> contextChannel;
    private final Channel<?>[] params;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TaskMethodChannel
      (Channel<TaskCommand<C,R>> commandChannel
      ,Channel<?>[] params
      ,Focus<?> focus
      )
      throws BindException
    { 
      super(commandChannel.getReflector(),commandChannel);
      this.params=params;
      
      // CommandChannel always creates a new command
      // Therefore we can't bind the contextFocus directly
      contextChannel
        =new ThreadLocalChannel<C>
          (commandChannel.<C>resolve(focus,"context",null).getReflector());
      Focus<C> contextFocus
        =new SimpleFocus<C>(contextChannel);
    
      if (params!=null)
      { 
        int i=0;
        for (Channel<?> param: params)
        {
          if (contextChannel.getContentType()==Void.class)
          {
            throw new BindException
              ("Method does not accept any arguments");
          }
          
          if (param instanceof BindingChannel)
          { ((BindingChannel) param).bindTarget(contextFocus);
          }
          else
          { 
            try
            {
              Channel paramTarget=contextFocus.bind(Expression.create("_"+i));
              this.params[i]=new AssignmentChannel(param,paramTarget);
            }
            catch (BindException x)
            { throw new BindException("Error binding argument #"+i,x);
            }
          
          }
          i++;
        }
      }      
    }
    
    @Override
    protected TaskCommand<C,R> retrieve()
    { 
      // XXX: Must handle polymorphic method override by binding supertype
      //   method to subtype
      if (AbstractTaskMethod.this.debug)
      { log.fine("Marshalling "+getURI());
      }
      
      TaskCommand<C,R> command=source.get();
      contextChannel.push(command.getContext());
      try
      {
        for (Channel<?> param: params)
        { param.get();
        }
        command.setContext(contextChannel.get());
        command.encloseContext();
      }
      finally
      { contextChannel.pop();
      }
      return command;
    }

    @Override
    protected boolean store(
      TaskCommand<C,R> val)
      throws AccessException
    { return false;
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  class ExecChannel
    extends SourcedChannel<TaskCommand,Object>
  {
  
    public ExecChannel(Scenario scenario,Focus focus,Channel commandChannel)
      throws BindException
    { 
      super
        (scenario.getResultReflector()!=null
          ?scenario.getResultReflector()
          :commandChannel.resolve(focus,"result",null).getReflector()
        ,commandChannel
        );
    }
   
    @Override
    protected Object retrieve()
    { 
      
      TaskCommand command=source.get();
      if (AbstractTaskMethod.this.debug)
      { log.fine("Executing "+getURI());
      }
      command.execute();
      if (command.getException()!=null)
      { 

        if (throwException)
        {
          throw new AccessException
            ("Caught exception executing command",command.getException()
            );
        }
        else
        {
          log.log
            (Level.WARNING
            ,"Scenario threw exception: "+this,command.getException()
            );        
        }
      }
      return command.getResult();
    }

    @Override
    protected boolean store(
      Object val)
      throws AccessException
    { return false;
    }
    
    @Override
    public boolean isWritable()
    { return false;
    }
  }
       
}
  


