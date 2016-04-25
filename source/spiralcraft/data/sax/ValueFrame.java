//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.sax;

import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.lang.TypedDataReflector;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;

/**
 * <p>Associates a typed value with an XML element, mapping and translating 
 *   data into the spiralcraft.data Type space.
 * </p>
 * 
 * 
 * @author mike
 *
 */
public class ValueFrame<T>
  extends AbstractFrameHandler
{

  private Type<T> type;
  
  private Expression<EditableAggregate<T>> container;
  private Expression<T> assignment;
  
  private Channel<T> channel;
  
  private Channel<EditableAggregate<T>> containerChannel;
  private Channel<T> assignmentChannel;  
  
  public void setType(Type<T> type)
  { this.type=type;
  }
  
  public Type<?> getType()
  { return type;
  }


  /**
   * <p>An expression that references a container, such as a List or an
   *   EditableAggregate, to which this this value will be added on
   *   completion of the Frame
   * </p>
   * 
   * @param container
   */
  public void setContainer(Expression<EditableAggregate<T>> container)
  { this.container=container;
  }

  /**
   * <p>An expression that references a field or property that will be updated
   *   with the value on completion of the frame.
   * </p>
   * 
   */
  public void setAssignment(Expression<T> assignment)
  { this.assignment=assignment;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void bind()
    throws BindException
  {
    
    // These must bind to the parent focus, which is our own focus
    //   before we bind and extend the Focus chain.
    Focus<?> parentFocus=getFocus();
    if (container!=null)
    { containerChannel=parentFocus.bind(container);
    }
    if (assignment!=null)
    { assignmentChannel=parentFocus.bind(assignment);
    }

    if (type==null)
    {
      Reflector<T> reflector=null;
      if (assignment!=null)
      { 
        reflector=assignmentChannel.getReflector();
        if (reflector instanceof TypedDataReflector)
        { type=((TypedDataReflector) reflector).getType();
        }
        else
        { 
          try
          { type=ReflectionType.canonicalType(reflector.getContentType());
          } 
          catch (DataException x)
          { throw new BindException("ValueFrame for elementURI="
              +getElementURI()+": Error determining value type",x);
          }
        }
      }
    }
    
    if (type==null)
    { 
      throw new BindException
        ("ValueFrame for elementURI="
          +getElementURI()
          +": A ValueFrame without an assignment must be provided with a Type"
        );
    }
    
    channel=new FrameChannel<T>(DataReflector.<T>getInstance(type));
    setFocus(new SimpleFocus<T>(parentFocus,channel));
    setTextBinding(Expression.create("."));
    
    
    super.bind();
  }
  
  @Override
  protected void openData()
    throws DataException
  {

  }
  
  @Override
  protected void closeData()
    throws DataException
  {     

    Setter.applyArrayIfNull(defaultSetters);
    
    if (assignmentChannel!=null)
    { assignmentChannel.set(channel.get());
    }
    
    if (containerChannel!=null)
    { 
      EditableAggregate<T> container
        =containerChannel.get();
      
      T value=channel.get();
      
      if (value!=null)
      {
        if (container==null)
        { 
          if (!containerChannel.isWritable())
          { 
            throw new DataException
              ("Container for "
              +type.getURI()
              +" does not exist and could not be created because "
              +" the container channel is not writable." 
              );
          }
          
          container
            =new EditableArrayListAggregate<T>
              (Type.getAggregateType(type));
          
          if (!containerChannel.set(container))
          {
            throw new DataException
              ("Container for "
              +type.getURI()
              +" does not exist and could not be created because "
              +" the container channel did not accept the new Container. "
              );
          }
        }
        container.add(value);
      }
    }

    if (debug)
    { 
      Object val=channel.get();
      log.fine("elementURI="+getElementURI()+": "+(val!=null?val:""));
    }

  }
  
  
  
}
