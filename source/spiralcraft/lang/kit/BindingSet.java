//
// Copyright (c) 2011,2012 Michael Toth
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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.parser.BindingNode;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.ContextualFunction;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * <p>Encapsulates the use of multiple binding channels for a single operation,
 *   such as when a set of values are being applied to a single object.
 * </p>
 * 
 * <p>The result of this function is the same as its input
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 * @param <X>
 */
public class BindingSet<T>
  extends ContextualFunction<T,T,RuntimeException>
{

  private Expression<?>[] bindingExpressions;
  private BindingChannel<?>[] bindingChannels;
  private Reflector<T> inputReflector;
  
  public BindingSet
    (Expression<?>[] bindingExpressions)
  { 
    this.bindingExpressions=bindingExpressions;
    for (Expression<?> expr: bindingExpressions)
    {
      if (!(expr.getRootNode() instanceof BindingNode))
      { 
        throw new IllegalArgumentException
          ("Binding expressions must be in the form \"property := value\"");
      }
    }
    
  }
  
  public Expression<?>[] getBindingExpressions()
  { return bindingExpressions;
  }
  
  @Override
  public Focus<?> bind(Focus<?> context)
    throws BindException
  {
    
    bindingChannels=new BindingChannel<?>[bindingExpressions.length];
    int i=0;
    for (Expression<?> expr: bindingExpressions)
    {
      if (!(expr.getRootNode() instanceof BindingNode))
      { 
        throw new BindException
          ("Binding expressions must be in the form \"property := value\"");
      }
      BindingNode<?,?> bindingNode
        =(BindingNode<?,?>) expr.getRootNode();
      bindingChannels[i++]=(BindingChannel<?>) bindingNode.bind(context);
    }
    return super.bind(context);
  }
  
    
  @Override
  protected Reflector<T> getInputReflector()
    throws BindException
  { return inputReflector;
  }
  
  public void setInputReflector(Reflector<T> inputReflector)
  { this.inputReflector=inputReflector;
  }

  @Override
  protected Channel<T> bindResult(
    Focus<T> inputFocus)
    throws BindException
  {
    for (BindingChannel<?> bindingChannel: bindingChannels)
    { bindingChannel.bindTarget(inputFocus);
    }
    return new BindingSetChannel<T>
      (inputFocus.getSubject(),bindingChannels);
  }


}

class BindingSetChannel<T>
  extends SourcedChannel<T,T>
{
  private final BindingChannel<?>[] bindings;
  
  public BindingSetChannel(Channel<T> input,BindingChannel<?>[] bindings)
  { 
    super(input);
    this.bindings=bindings;
  }

  @Override
  protected T retrieve()
  {
    for (BindingChannel<?> binding: bindings)
    { binding.get();
    }
    return source.get();
  }

  @Override
  protected boolean store(
    T val)
      throws AccessException
  { return false;
  }
  
  public boolean isWritable()
  { return false;
  }
  
}