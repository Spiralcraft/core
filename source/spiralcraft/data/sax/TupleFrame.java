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
import spiralcraft.lang.Binding;
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
 * <p>Effective order of operations:
 * </p>
 * 
 * <ul>
 *   <li>openData(): A Tuple is either resolved the assignment channel, if
 *     specified.
 *   </li>
 *   <li>openData(): A new Tuple is created if no existing Tuple was resolved. 
 *   </li>
 *   <li>openFrame(): Attribute bindings are applied
 *   </li>
 *   <li> ... children are read ...
 *   </li>
 *   <li>closeFrame(): Any textual data is applied to the textBinding, if
 *     specified
 *   </li>
 *   <li>closeData(): DefaultAssignments are applied
 *   </li>
 *   <li>closeData(): The assignment, if specified, is updated with a reference
 *     to the Tuple.
 *   </li>
 *   <li>closeData(): The container, if specified, gets a reference to the Tuple
 *     added to it.
 *   </li>
 *   <li>closeFrame(): The parent is notified of the completed Frame.
 *   </li>
 * </ul>
 * <p> When the XML Frame is complete, optionally 
 *   updates or appends to a target.
 * </p>
 * 
 * <p>The AttributeBinding expressions are relative to a Focus on the Tuple
 *   reference by the current frame. 
 * </p>
 * 
 * @author mike
 *
 */
public class TupleFrame
  extends AbstractFrameHandler
{

  private Type<?> type;
  private Binding<Type<?>> typeX;
  
  private Expression<EditableAggregate<Tuple>> container;
  private Expression<Tuple> assignment;
  
  private Channel<Tuple> channel;
  private Channel<Tuple> primaryKeyChannel;
  
  private Channel<EditableAggregate<Tuple>> containerChannel;
  private Channel<Tuple> assignmentChannel;
  
  private boolean skipDuplicates;
  private boolean updateAssignment;
  
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  public void setTypeX(Binding<Type<?>> typeX)
  { this.typeX=typeX;
  }

  /**
   * <p>Indicate that an existing Tuple associated with the expression in the
   *   assignment property should be updated instead of being overwritten
   *   with a new Tuple
   * </p>
   * 
   * <p>When set to false, a new Tuple will always be created by this
   *   FrameHandler
   * </p>
   *   
   * 
   * @param updateAssignment
   */
  public void setUpdateAssignment(boolean updateAssignment)
  { this.updateAssignment=updateAssignment;
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
   * <p>An expression that references a field or property that will be updated
   *   with the populated Tuple on completion of the frame.
   * </p>
   * 
   */
  public void setAssignment(Expression<Tuple> assignment)
  { this.assignment=assignment;
  }
  
  @SuppressWarnings("unchecked") // getPrimaryKey() -> Key<Tuple>
  @Override
  public void bind()
    throws BindException
  {
    if (type==null && typeX!=null)
    { 
      typeX.bind(getFocus());
      type=typeX.get();
    }
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
    { primaryKeyChannel=key.bindChannel(channel,this.<Tuple>getFocus(),null);
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
    Tuple tuple=null;
    if (updateAssignment && assignmentChannel!=null)
    { tuple=assignmentChannel.get();
    }
      
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
          
          if (index.getFirst(new KeyTuple(primaryKeyChannel.get()))==null)
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
