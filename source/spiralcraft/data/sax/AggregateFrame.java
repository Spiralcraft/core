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
public class AggregateFrame<Tcomponent>
  extends AbstractFrameHandler
{

  private Type<?> type;
  private Channel<EditableAggregate<Tcomponent>> channel;
  private Expression<Aggregate<Tcomponent>> assignment;
  private Channel<Aggregate<Tcomponent>> assignmentChannel;
  
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
    channel=new FrameChannel<EditableAggregate<Tcomponent>>
      (DataReflector.<EditableAggregate<Tcomponent>>getInstance(type));
    
    // These must bind to the parent focus, which is our own focus
    //   before we bind and extend the Focus chain.
    Focus<?> parentFocus=getFocus();

    if (assignment!=null)
    { assignmentChannel=parentFocus.bind(assignment);
    }
    
    setFocus(new SimpleFocus<EditableAggregate<Tcomponent>>(parentFocus,channel));
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
  public void setAssignment(Expression<Aggregate<Tcomponent>> assignment)
  { this.assignment=assignment;
  }
  
  @Override
  protected void openData()
    throws DataException
  {
    EditableAggregate<Tcomponent> aggregate=null;
    if (assignmentChannel!=null)
    { aggregate=(EditableAggregate<Tcomponent>) assignmentChannel.get();
    }
    
    if (aggregate==null)
    {    
      DataFactory<?> factory=getFrame().getDataHandler().getDataFactory();
      if (factory!=null)
      { aggregate=factory.<EditableAggregate<Tcomponent>>create(type);
      }
      else
      { aggregate=new EditableArrayListAggregate<Tcomponent>(type);
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
  
  @SuppressWarnings("unchecked")
  @Override
  public void capturedChildObject
    (Object childObject,ForeignDataHandler.HandledFrame myFrame)
  { 
    if (debug)
    { log.fine("Captured: "+childObject);
    }
    if (childObject!=null)
    { channel.get().add((Tcomponent) childObject);
    }
  } 
  
}
