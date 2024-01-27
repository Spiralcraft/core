//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.lang.spi;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.parser.StructNode;
import spiralcraft.log.ClassLog;

/**
 * A Channel that binds a set of named and positional arguments to
 *   some context
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tsource>
 */
public abstract class AbstractFunctorChannel<Tresult>
  extends AbstractChannel<Tresult>
{
  private static final ClassLog log
    =ClassLog.getInstance(AbstractFunctorChannel.class);
  
  protected Channel<?>[] bindings;
  private Focus<?> focus;
  
  protected AbstractFunctorChannel
    (Reflector<Tresult> result
    )
    throws BindException
  { super(result);
  }
  
  public void bind(Focus<?> focus,Channel<?>[] arguments)
    throws BindException
  {

    bindings=new Channel<?>[arguments.length];
    this.focus=bindImports(focus);
    setContext(this.focus);
    this.bindings=arguments;
    bindTarget(this.focus);
    
  }
  
  /**
   * The result of a Functor call cannot be modified
   */
  @Override
  public boolean isWritable()
  { return false;
  }
  
  protected Focus<?> bindImports(Focus<?> focus)
    throws BindException
  { return focus;
  }  
  
  /**
   * Override and call with the parameter context that parameter targets
   *   will be resolved against
   * 
   * @param contextFocus
   * @throws BindException
   */
  protected void bindTarget(Focus<?> contextFocus)
    throws BindException
  {
    int paramIndex=0;
    for (int i=0;i<bindings.length;i++)
    { 
      Channel<?> channel=bindings[i];
      if (channel instanceof BindingChannel<?>)
      { ((BindingChannel<?>) channel).bindTarget(contextFocus);
      }
      else
      { 
        if (paramIndex!=i)
        { 
          throw new BindException
            ("Indexed parameters must preceed named parameters: "+channel);
        }
        bindings[i]=bindPositionalArgument(contextFocus,paramIndex++,channel);
      }
    }
  }
  
  private Channel<?> bindPositionalArgument
    (Focus<?> contextFocus,int paramIndex,Channel<?> source)
      throws BindException
  {
    
    if (contextFocus.getSubject().getReflector() instanceof StructNode.StructReflector)
    { 
      StructNode.StructReflector reflector=
          (StructNode.StructReflector) contextFocus.getSubject().getReflector();
      if (paramIndex<reflector.getFields().length)
      {
        Channel<?> paramTarget;
        
        try
        { 
          paramTarget = contextFocus.bind
            (Expression.create(reflector.getFields()[paramIndex].getName()));
        }
        catch (BindException x)
        {    
          throw new BindException
          ("Error binding argument to functor parameter '"
            +reflector.getFields()[paramIndex].getName()+"' "+
            "Could not bind target parameter to call context "
          ,x
          );
        }
        
        try
        {
        
          return new AssignmentChannel
            (source
            ,paramTarget
            );
        }
        catch (BindException x)
        { 
          throw new BindException
            ("Error binding argument to functor parameter '"
              +reflector.getFields()[paramIndex].getName()+"'"
              +"("+source.getReflector()+")->"+"("+paramTarget.getReflector()+")"
            ,x
            );
        }
        
      }
      else
      {
        throw new BindException
          ("Error binding parameter number "+(paramIndex+1)+"."
          +" Functor does not accept more than "
          +reflector.getFields().length+" positional parameters"
          );
      }
    }
    
    try
    {
      return new AssignmentChannel
        (source,contextFocus.bind(Expression.create("_"+paramIndex)));
    }
    catch (BindException x)
    { 
      
      if (paramIndex==0)
      { 
        
        if (contextFocus.getSubject().getReflector()
             .isAssignableFrom(source.getReflector())
           )
        { 
          return new AssignmentChannel
            (source,contextFocus.getSubject());
        }
        else
        {
          throw new BindException
            ("Error binding parameter number "+(paramIndex+1)
            +". Functor does not accept any positional parameters and "
            +" context type "
            +contextFocus.getSubject().getReflector().getTypeURI()
            +" cannot be assigned from parameter of type "
            +source.getReflector().getTypeURI()
            );
        }
      }
      else
      {
        throw new BindException
          ("Error binding parameter number "+(paramIndex+1)+"."
          +" Functor does not accept more than "
          +paramIndex+" positional parameters"
          );
      }
    }
  }
  
  
  protected void applyContextBindings()
  {
    for (Channel<?> channel: bindings)
    { 
      Object val=channel.get();
      if (debug)
      { log.fine("Binding applied value '"+val+"': "+channel.toString());
      }
    }
  }
  
  

  
  
}
