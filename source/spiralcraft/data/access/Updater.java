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
import java.util.HashMap;
import java.util.Map;

import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.Sequence;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.core.SequenceField;
import spiralcraft.data.lang.TupleReflector;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.rules.Inspector;
import spiralcraft.rules.RuleException;
import spiralcraft.rules.RuleSet;
import spiralcraft.rules.Violation;



/**
 * <p>Updates and validates field data before it is written to a store. 
 * </p> 
 * 
 * <p>Applies defaultExpression, fixedExpression and generates sequence
 *   data where applicable.
 * </p>
 * 
 * <p>May alter field data in Buffers to conform to Type constraints.
 * </p>
 * 
 * <p>Triggers validation of data in DeltaTuples according to Type constraints.
 * </p>
 * 
 * <p>This component is generally thread-safe, as long as backing channels are.
 * </p>
 * 
 * 
 * @author mike
 *
 * @param <T>
 */
public class Updater<T extends Tuple>
  implements DataConsumer<T>,Contextual
{

  protected static final ClassLog log
    =ClassLog.getInstance(Updater.class);

  private static final URI dataURI
    =URI.create("class:/spiralcraft/data/");

  private Focus<?> context;
  private Store store;
  
  protected SimpleFocus<T> localFocus;
  protected ThreadLocalChannel<T> localChannel;
  
  
  private ArrayList<Setter<?>> fixedSetters;
  private ArrayList<Setter<?>> defaultSetters;
  
  private Sequence sequence;
  private Field<Object> sequenceField;
  private Space space;
  protected boolean debug;
  private Inspector<Type<T>,T> inspector;

  private FieldSet fieldSet;
  
  /**
   * <p>Create a new Updater
   * </p>
   *   
   * @param context
   */
  public Updater()
  { 
  }

  public void setFieldSet(FieldSet fieldSet)
  { this.fieldSet=fieldSet;
  }
  
  @Override
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @Override
  public void dataAvailable(
    T tuple)
    throws DataException
  {
    if (debug)
    { log.fine("Got "+tuple);
    }
    localChannel.push(tuple);
    
    try
    { handleData(tuple);
    }
    finally
    { localChannel.pop();
    }
  }
  
  /**
   * <p>Performs the following standard operations in order
   *   <ul>
   *     <li>Apply relevant sequences
   *     </li>
   *     <li>Apply default values
   *     </li>
   *     <li>Apply fixed values
   *     </li>
   *     <li>Validate rules
   *     </li>
   *   </ul>
   * </p>
   * 
   * <p>This method is called by dataAvailable(Tuple t) with the subject
   *   tuple made available via the localChannel
   * </p>
   *  
   * @param tuple
   * @throws DataException
   */
  protected void handleData(T tuple)
    throws DataException
  {
    
    if (tuple.isMutable())
    {
      if (sequence!=null)
      {
        if (sequenceField.getValue(tuple)==null)
        { 
          String sequenceVal = Long.toString(sequence.next());
          if (debug)
          { 
            log.fine
              ("Generated sequence "+sequenceVal
              +" for "+sequenceField.getURI()
              );
          }
          
          sequenceField.setValue
            ((EditableTuple) tuple
              ,sequenceField.getType().fromString(sequenceVal)
            ); 
        }
        else
        { 
          if (debug)
          {
            log.fine
              ("Sequence field not null "+sequenceField.getURI());
          }
        }
      }
      else
      { 
        if (sequenceField!=null)
        { log.fine("Sequence is null for "+sequenceField);
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
    else
    {
      if (debug)
      {
        log.fine
          ("Not a mutable Tuple "+tuple);
      }
    }
     
    if (inspector!=null)
    {
      Violation<T>[] violations=inspector.inspect(tuple);
      if (violations!=null)
      { throw new DataException(null,new RuleException(violations));
      }
    }
    
  }

  @Override
  public void dataFinalize()
    throws DataException
  {

  }

  @Override
  public void dataInitialize(FieldSet fieldSet)
    throws DataException
  {
    if (localChannel==null)
    { 
      this.fieldSet=fieldSet;
      try
      { bind(context);
      }
      catch (BindException x)
      { throw new DataException("Error binding Updater for "+fieldSet);
      }
    }
    else
    { 
      if (this.fieldSet.getType()!=null 
          && fieldSet.getType()!=null
          && !this.fieldSet.getType().isAssignableFrom(fieldSet.getType())
         )
      { 
        throw new DataException
          ("Updator for "+this.fieldSet.getType().getURI()
            +" does not accept values of type "+fieldSet.getType().getURI()
          );
      }
      else if (!fieldSet.equals(this.fieldSet))
      { 
        throw new DataException
          ("Updator for "+this.fieldSet
            +" does not accept values with fields "+fieldSet
          );
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Setter<?> bindSetter(Field field,Expression expression)
    throws BindException
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
      throw new BindException
        ("Error binding Expression '"+expression+"' for "+field.getURI(),x);
    }
    
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    if (context==null)
    { context=focusChain;
    }
    
    if (fieldSet.getType()!=null && fieldSet.getType().getDebug())
    { debug=true;
    }
    
    try
    { localChannel=new ThreadLocalChannel(TupleReflector.getInstance(fieldSet));
    }
    catch (BindException x)
    { 
      throw new BindException
        ("Error creating channel for fieldSet "+fieldSet,x);
    }
    
    localFocus=new SimpleFocus<T>(context,localChannel);

    // XXX Deprecated
    localFocus.setNamespaceResolver
      (new PrefixResolver()
        {
          private PrefixResolver parent
            =localFocus.getNamespaceResolver();
          
          
          @Override
          public URI resolvePrefix(String namespace)
          {
            if (namespace.equals("data"))
            { return dataURI;
            }
            else if (namespace.equals(""))
            { return dataURI;
            }
            else if (parent!=null)
            { return parent.resolvePrefix(namespace);
            }
            else
            { return null;
            }
          }
          
          @Override
          public Map<String,URI> computeMappings()
          { 
            Map<String,URI> computedMappings=new HashMap<String,URI>();

            Map<String,URI> parentMappings
              =parent!=null?parent.computeMappings():null;
            if (parentMappings!=null)
            { computedMappings.putAll(parentMappings);
            }
            
            computedMappings.put("data",dataURI);
            computedMappings.put("",dataURI);
            return computedMappings;
          }            
          
        }
      );
    
    
    if (space==null)
    { space=Space.find(context);
    }

    if (store==null)
    { store=LangUtil.findInstance(Store.class,context);
    }
    
    // Takes care of key sequences? Nope.
    for (Field field: fieldSet.fieldIterable())
    {
      if (debug)
      { log.fine("Updater binding: "+field);
      }
      
      if (field instanceof SequenceField)
      {
        if (sequence!=null)
        { log.warning("Ignoring additional SequenceField "+field.getURI());
        }
        else if (store==null && space==null)
        {
          log.warning
            ("Ignoring SequenceField-"
            +" not attached to a Store or Space: "+field.getURI()
            );
        }
        else
        { 
          if (debug)
          { log.fine("Binding sequence field "+field.getURI());
          }
          
          try
          { 
            if (store!=null)
            { sequence=store.getSequence(field.getURI());
            }
            if (sequence==null && space!=null)
            { sequence=space.getSequence(field.getURI());
            }
          }
          catch (DataException x)
          { throw new BindException("Error getting sequence for  "+field.getURI(),x);
          }
          
          if (sequence==null)
          { throw new BindException("Sequence not found for "+field.getURI());
          }
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
    
    Type<T> type=(Type<T>) fieldSet.getType();
    if (type!=null)
    {
      RuleSet<Type<T>,T> ruleSet=type.getRuleSet();
      if (ruleSet!=null)
      { 
        try
        {
          if (debug)
          { log.fine("Binding type rules");
          }
          inspector
            =ruleSet.bind(localChannel.getReflector(), localFocus);
        }
        catch (BindException x)
        { 
          throw new BindException
            ("Error binding rules for Type "+type.getURI(),x);
        }

      }
      else
      {
        if (debug)
        { log.fine("Not binding type rules");
        }
      }
    }

    return localFocus;
  }

}
