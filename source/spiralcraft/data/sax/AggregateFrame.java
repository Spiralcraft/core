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

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataFactory;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;

/**
 * <p>Creates an Aggregate and publishes it in the Focus chain so it can receive
 *   updates. The AttributeBinding expressions reference the Aggregate by default.
 * </p>
 * 
 * @author mike
 *
 */
public class AggregateFrame
  extends AbstractFrameHandler
{

  private Type<?> type;
  private Channel<EditableAggregate<?>> channel;
  private Expression<Aggregate<?>> assignment;
  private Channel<Aggregate<?>> assignmentChannel;
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  @Override
  public void bind()
    throws BindException
  {
    if (type==null)
    { 
      throw new BindException
        ("TupleFrameHandler for elementURI="
        +getElementURI()+" requires a type"
        );
    }
    channel=new FrameChannel<EditableAggregate<?>>
      (DataReflector.<EditableAggregate<?>>getInstance(type));
    
    // These must bind to the parent focus, which is our own focus
    //   before we bind and extend the Focus chain.
    Focus<?> parentFocus=getFocus();

    if (assignment!=null)
    { assignmentChannel=parentFocus.bind(assignment);
    }
    
    setFocus(new SimpleFocus<EditableAggregate<?>>(parentFocus,channel));
    super.bind();
  }

  /**
   * <p>An expression that references a container, such as a List or an
   *   EditableAggregate, to which this this Tuple will be added on
   *   completion of the Frame
   * </p>
   * 
   * @param container
   */
  public void setAssignment(Expression<Aggregate<?>> assignment)
  { this.assignment=assignment;
  }
  
  @Override
  protected void openData()
    throws DataException
  {
    EditableAggregate<?> aggregate=null;
    if (assignmentChannel!=null)
    { aggregate=(EditableAggregate<?>) assignmentChannel.get();
    }
    
    if (aggregate==null)
    {    
      DataFactory<?> factory=getFrame().getDataHandler().getDataFactory();
      if (factory!=null)
      { aggregate=factory.<EditableAggregate<?>>create(type);
      }
      else
      { aggregate=new EditableArrayListAggregate<Object>(type);
      }
    }
    channel.set(aggregate);
    
  }
    
  
  @Override
  protected void closeData()
    throws DataException
  { 
    if (assignmentChannel!=null)
    { assignmentChannel.set(channel.get());
    }
    
    if (debug)
    { 
      Object val=channel.get();
      log.fine("elementURI="+getElementURI()+": "+(val!=null?val:""));
    }

  }
  

  
}
