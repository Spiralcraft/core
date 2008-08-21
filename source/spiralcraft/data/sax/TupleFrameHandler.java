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
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataFactory;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.EditableKeyedListAggregate;
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
 * The AttributeBinding expressions are scoped to the Tuple.
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
  private Channel<Tuple> primaryKeyChannel;
  
  private Channel<EditableAggregate<Tuple>> containerChannel;
  private Channel<Tuple> assignmentChannel;
  
  private boolean skipDuplicates;
  
  
  public void setType(Type<?> type)
  { this.type=type;
  }

  /**
   * <p>Indicate whether duplicate keys should be skipped when
   *   adding Tuples to a container.
   * </p>
   * 
   * @param skipDuplicates
   */
  public void setSkipDuplicates(boolean skipDuplicates)
  { this.skipDuplicates=skipDuplicates;
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
  
  @SuppressWarnings("unchecked") // getPrimaryKey() -> Key<Tuple>
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
    
    Key<Tuple> key=(Key<Tuple>) type.getPrimaryKey();
    if (key!=null)
    { primaryKeyChannel=key.bindChannel(this.<Tuple>getFocus());
    }
    else
    {
      if (skipDuplicates)
      {
        throw new BindException
          ("Type "+type.getURI()+" does not have a primary key." +
          " skipDuplicates cannot be set to true"
          );
      }
    }
    
    super.bind();
  }
  
  @Override
  protected void openData()
    throws DataException
  {
    Tuple tuple;
    DataFactory<?> factory=getFrame().getDataHandler().getDataFactory();
    if (factory!=null)
    { tuple=factory.<Tuple>create(type);
    }
    else
    { tuple=new EditableArrayTuple(type);
    }
    channel.set(tuple);
  }
  
  @SuppressWarnings("unchecked") // getPrimaryKey() -> Key<Tuple>
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
      
      Tuple tuple=channel.get();
      
      if (tuple!=null)
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
            =new EditableKeyedListAggregate<Tuple>
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
        
        if (skipDuplicates)
        {
          
          Aggregate.Index<Tuple> index
            =container.getIndex
              ((Key<Tuple>) type.getPrimaryKey(), true);
          
          if (index==null)
          { 
            throw new DataException
              ("Could not get an Index to check for duplicates");
          }
          
          if (index.getOne(new KeyTuple(primaryKeyChannel.get()))==null)
          { container.add(tuple);
          }
          else
          {
            log.fine("Skipping duplicate "+tuple);
          }
        }
        else
        {
          container.add(tuple);
        }
      }
    }

    if (debug)
    { 
      Object val=channel.get();
      log.fine("elementURI="+getElementURI()+": "+(val!=null?val:""));
    }

  }
  
  
  
}
