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
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataFactory;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;

/**
 * <p>Associates a Tuple with an XML element, mapping and translating data into
 *   the spiralcraft.data Type space.
 * </p>
 * 
 * <p>Publishes the Tuple in the Focus chain under the Type URI so it can be
 *   updated by child FrameHandlers.
 * </p>
 * 
 * <p> When the XML Frame is complete, optionally 
 *   updates or appends to a target.
 * </p>
 * 
 * <p>
 * The AttributeBinding expressions are bound to a Focus on the Tuple.
 * </p>
 * 
 * @author mike
 *
 */
public class TupleFrameHandler
  extends FrameHandler
{

  private Type<?> type;
  
  private Expression<EditableAggregate<Tuple>> container;
  private Expression<Tuple> assignment;
  
  private Channel<Tuple> channel;
  private Channel<EditableAggregate<Tuple>> containerChannel;
  private Channel<Tuple> assignmentChannel;
  
  private Tuple initialValue;
  
  
  public void setType(Type<?> type)
  { this.type=type;
  }

  public void setInitialValue(Tuple initialValue)
  { this.initialValue=initialValue;
  }

  /**
   * <p>An expression that references a container, such as a List or an
   *   EditableAggregate, to which this this Tuple will be added on
   *   completion of the Frame
   * </p>
   * 
   * @param container
   */
  public void setContainer(Expression<EditableAggregate<Tuple>> container)
  { this.container=container;
  }

  /**
   * <p>An expression that references a container, such as a List or an
   *   EditableAggregate, to which this this Tuple will be added on
   *   completion of the Frame
   * </p>
   * 
   * @param container
   */
  public void setAssignment(Expression<Tuple> assignment)
  { this.assignment=assignment;
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
    channel=new FrameChannel<Tuple>(DataReflector.<Tuple>getInstance(type));
    
    // These must bind to the parent focus, which is our own focus
    //   before we bind and extend the Focus chain.
    Focus<?> parentFocus=getFocus();
    if (container!=null)
    { containerChannel=parentFocus.bind(container);
    }
    if (assignment!=null)
    { assignmentChannel=parentFocus.bind(assignment);
    }
    setFocus(new SimpleFocus<Tuple>(parentFocus,channel));
    super.bind();
  }
  
  @Override
  protected void openData()
    throws DataException
  {
    Tuple tuple=initialValue;
    if (tuple==null)
    {
    
      DataFactory<?> factory=getFrame().getDataHandler().getDataFactory();
      if (factory!=null)
      { tuple=factory.<Tuple>create(type);
      }
      else
      { tuple=new EditableArrayTuple(type);
      }
    }
    channel.set(tuple);
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
      EditableAggregate<Tuple> container
        =containerChannel.get();
      
      if (container!=null && channel.get()!=null)
      { container.add(channel.get());
      }
    }

    if (debug)
    { 
      Object val=channel.get();
      log.fine("elementURI="+getElementURI()+": "+(val!=null?val:""));
    }

  }
  
  
  
}
