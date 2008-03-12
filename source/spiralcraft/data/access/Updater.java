//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.access;

import java.net.URI;
import java.util.ArrayList;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.Sequence;
import spiralcraft.data.Space;

import spiralcraft.data.core.SequenceField;
import spiralcraft.data.lang.TupleFocus;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.NamespaceResolver;
import spiralcraft.lang.Setter;
import spiralcraft.log.ClassLogger;



/**
 * <p>Updates "downstream" data from changes specified in DeltaTuples.
 * </p> 
 * 
 * <p>May alter field data in Buffers to conform to Type constraints.
 * </p>
 * 
 * <p>Triggers validation of data in DeltaTuples according to Type constraints.
 * </p>
 * 
 * 
 * 
 * @author mike
 *
 * @param <T>
 */
public class Updater<T extends DeltaTuple>
  implements DataConsumer<T>
{

  private static final ClassLogger log=new ClassLogger(Updater.class);

  private static final URI dataURI
    =URI.create("class:/spiralcraft/data/");

  private Focus<?> context;
  private TupleFocus<T> localFocus;
  
  
  private ArrayList<Setter<?>> fixedSetters;
  private ArrayList<Setter<?>> defaultSetters;
  
  private Sequence sequence;
  private Field sequenceField;
  private Space space;
  
  /**
   * <p>Create a new Updater, which uses the provided context to to resolve any
   *   external references associated with Tuple fields
   * </p>
   *   
   * 
   * @param context
   */
  public Updater(Focus<?> context)
  { this.context=context;
  }

  @Override
  public void dataAvailable(
    T tuple)
    throws DataException
  {
    localFocus.setTuple(tuple);
    
    if (tuple instanceof BufferTuple)
    {
      if (sequence!=null)
      {
        if (sequenceField.getValue(tuple)==null)
        { 
          sequenceField.setValue
          ((EditableTuple) tuple
            ,sequenceField.getType().fromString
            (Integer.toString(sequence.next()))
          );
        }
      }

      if (defaultSetters!=null)
      {
        for (Setter<?> setter : defaultSetters)
        { 
          if (setter.getTarget().get()==null)
          { setter.set();
          }
        }
      }

      if (fixedSetters!=null)
      {
        for (Setter<?> setter : fixedSetters)
        { setter.set();
        }
      }
    }
     
    
    
  }

  @Override
  public void dataFinalize()
    throws DataException
  {
    localFocus.setTuple(null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void dataInitialize(
    FieldSet fieldSet)
  throws DataException
  {
    localFocus=new TupleFocus<T>(context,fieldSet);
    localFocus.setNamespaceResolver
      (new NamespaceResolver()
        {
          private NamespaceResolver parent
            =localFocus.getNamespaceResolver();
          
          
          public URI resolveNamespace(String namespace)
          {
            if (namespace.equals("data"))
            { return dataURI;
            }
            else if (parent!=null)
            { return parent.resolveNamespace(namespace);
            }
            else
            { return null;
            }
          }
          
          public URI getDefaultNamespaceURI()
          { return dataURI;
          }
        }
      );
    if (space==null)
    {
      Focus<Space> spaceFocus=(Focus<Space>) context.findFocus(Space.SPACE_URI);
      if (spaceFocus!=null)
      { space=spaceFocus.getSubject().get();
      }
    }

    // Takes care of key sequences? Nope.
    for (Field field: fieldSet.fieldIterable())
    {
      if (field instanceof SequenceField)
      {
        if (sequence!=null)
        { 
          log.fine("Ignoring additional SequenceField "+field.getURI());
        }
        else if (space==null)
        {
          log.fine
            ("Ignoring SequenceField-"
            +" not attached to a Space: "+field.getURI()
            );
        }
        else
        { 
          sequence=space.getSequence(field.getURI());
          sequenceField=field;
        }
      }
      
      if (field.getDefaultExpression()!=null)
      { 
        if (defaultSetters==null)
        { defaultSetters=new ArrayList<Setter<?>>();
        }
        defaultSetters.add
          ( bindSetter(field,field.getDefaultExpression())
          );
      }


      // Takes care of timestamps
      if (field.getFixedExpression()!=null)
      {
        if (fixedSetters==null)
        { fixedSetters=new ArrayList<Setter<?>>();
        }
        fixedSetters.add
          (bindSetter(field,field.getFixedExpression()));
      }
    }

    
  }

  @SuppressWarnings("unchecked")
  private Setter<?> bindSetter(Field field,Expression expression)
    throws DataException
  {
    try
    {
      return new Assignment
        (Expression.create(field.getName())
        ,expression
        ).bind(localFocus);
    }
    catch (BindException x)
    { 
      throw new DataException
        ("Error binding Expression '"+expression+"' for "+field.getURI(),x);
    }
    
  }

}
