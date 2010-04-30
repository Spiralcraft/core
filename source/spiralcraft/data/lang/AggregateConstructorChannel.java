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
package spiralcraft.data.lang;


import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Type;
import spiralcraft.data.session.BufferAggregate;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.spi.AbstractChannel;

/**
 * <p>Constructs an Aggregate and initializes field values to the result of
 *   Field.newExpression
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class AggregateConstructorChannel<T extends Aggregate<I>,I>
  extends AbstractChannel<T>
{
  private final Type<T> type;
  private final boolean buffer;
  private final Channel<DataSession> dataSessionChannel;
  private final IterationDecorator<T,I> iterable;
  
  @SuppressWarnings("unchecked")
  public AggregateConstructorChannel
    (AggregateReflector<T,I> reflector,Focus<?> context,Channel<?> dataChannel)
    throws BindException
  { 
    super(reflector);
      
    buffer=((Type) reflector.getType()) instanceof BufferType;
    if (buffer)
    { 
      this.type=reflector.getType();
      dataSessionChannel=DataSession.findChannel(context);
      if (dataSessionChannel==null)
      { throw new BindException("Buffer requires a DataSession in context");
      }
    }
    else
    { 
      this.type=reflector.getType();
      dataSessionChannel=null;
    }
    if (dataChannel!=null)
    { 
      iterable
        =dataChannel.<IterationDecorator>decorate(IterationDecorator.class);
    }
    else
    { 
      iterable=null;
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  { 
    EditableAggregate<I> aggregate =
      buffer
        ?new BufferAggregate(dataSessionChannel.get(),type)
        :new EditableArrayListAggregate<I>(type);
    if (iterable!=null)
    { aggregate.addAll(iterable.iterator());
    }
    return (T) aggregate;
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { 
    throw new UnsupportedOperationException
      ("Cannot assign a value to the output of a constructor");
  }

}
