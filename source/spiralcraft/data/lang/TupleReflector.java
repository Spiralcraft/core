//
// Copyright (c) 1998,2005 Michael Toth
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

import java.util.ArrayList;
import java.util.LinkedList;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Signature;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.Assignment;

import spiralcraft.log.ClassLog;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Method;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;
import spiralcraft.data.Type;
import spiralcraft.data.reflect.ReflectionType;


/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class TupleReflector<T extends Tuple>
  extends DataReflector<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(TupleReflector.class);
  
  private static boolean debug;
  
  private static final Expression<?>[] NULL_PARAMS = new Expression[0];
  
  private final FieldSet untypedFieldSet;
  
  private final Class<T> contentType;

  @SuppressWarnings("unchecked") // We only create Reflector with erased type
  public synchronized static final 
    <T extends Tuple> TupleReflector<T> getInstance(FieldSet fieldSet)
    throws BindException
  { 
    if (fieldSet.getType()!=null)
    { return (TupleReflector) DataReflector.getInstance(fieldSet.getType());
    }
    else
    { return new TupleReflector(fieldSet,Tuple.class);
    }
  }
  

  TupleReflector(Type<T> type,Class<T> contentType)
  { 
    super(type);
    untypedFieldSet=null;
    this.contentType=contentType;
//    for (Field field : fieldSet.fieldIterable())
//    { fieldTranslators.put(field.getName(),new FieldTranslator(field));
//    }
  }
  
  @SuppressWarnings("unchecked")
  public TupleReflector(FieldSet fieldSet,Class<T> contentType)
  { 
    super((Type<T>) fieldSet.getType());
    if (fieldSet.getType()==null)
    { this.untypedFieldSet=fieldSet;
    }
    else
    { this.untypedFieldSet=null;
    }
    this.contentType=contentType;
//    for (Field field : fieldSet.fieldIterable())
//    { fieldTranslators.put(field.getName(),new FieldTranslator(field));
//    }
  }

  public FieldSet getFieldSet()
  { return untypedFieldSet!=null?untypedFieldSet:type.getFieldSet();
  }
  
  
  @SuppressWarnings("unchecked")
  public Assignment<?>[] getNewAssignments()
  {
    ArrayList<Assignment<?>> assignments=new ArrayList<Assignment<?>>();
    
    for (Field<?> field: getFieldSet().fieldIterable())
    {
      if (field.getNewExpression()!=null)
      { 
        assignments.add
          (new Assignment
            (Expression.create(field.getName())
            ,field.getNewExpression()
            )
          );
      }
    }
    return assignments.toArray(new Assignment[assignments.size()]);
  }
  
  @SuppressWarnings("unchecked")
  public Assignment<?>[] getDefaultAssignments()
  {
    ArrayList<Assignment<?>> assignments=new ArrayList<Assignment<?>>();
    
    for (Field<?> field: getFieldSet().fieldIterable())
    {
      if (field.getDefaultExpression()!=null)
      { 
        assignments.add
          (new Assignment
            (Expression.create(field.getName())
            ,field.getDefaultExpression()
            )
          );
      }
    }
    return assignments.toArray(new Assignment[assignments.size()]);
  }
  
  @SuppressWarnings("unchecked")
  public Assignment<?>[] getFixedAssignments()
  {
    ArrayList<Assignment<?>> assignments=new ArrayList<Assignment<?>>();
    
    for (Field<?> field: getFieldSet().fieldIterable())
    {
      if (field.getFixedExpression()!=null)
      { 
        assignments.add
          (new Assignment
            (Expression.create(field.getName())
            ,field.getFixedExpression()
            )
          );
      }
    }
    return assignments.toArray(new Assignment[assignments.size()]);    
  }

  /**
   * Resolve a meta name
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  @Override
  public synchronized <X> Channel<X> resolveMeta
    (final Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  {  
    Channel<X> channel=super.<X>resolveMeta(source,focus,name,params);
    if (channel!=null)
    { return channel;
    }
    if (name.equals("@tuple"))
    { 
      // Provide access to 
      Channel binding=source.getCached("_tuple");
      if (binding==null)
      { 
        binding=new AspectChannel
          (BeanReflector.getInstance(contentType)
          ,source
          );
        source.cache("_tuple",binding);
      }
      return binding;
    }
    return null;
  }

  /**
   * Resolve a Binding that provides access to a member of a Tuple given a 
   *   source that provides Tuples.
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  @Override
  public synchronized <X> Channel<X> resolve
    (final Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  {    
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
    
    if (name.equals("_tuple"))
    { 
      // Provide access to 
      Channel binding=source.getCached("_tuple");
      if (binding==null)
      { 
        binding=new AspectChannel
          (BeanReflector.getInstance(contentType)
          ,source
          );
        source.cache("_tuple",binding);
      }
      return binding;
    }
    
    
    Field field=null;
    
    Type type=getType();
    if (type!=null)
    { field=type.getField(name);
    }
    else
    { field=untypedFieldSet.getFieldByName(name);
    }
    
    if (field!=null)
    {
      Focus tupleFocus;
      
      // Make sure the Focus for evaluating expressions in the Scheme is 
      //   consistent with the source
      if (focus.getContext()!=source
          || focus.getSubject()!=source
          )
      { tupleFocus=new SimpleFocus(focus,source);
      }
      else
      { tupleFocus=focus;
      }
              
      Channel binding=field.bindChannel(tupleFocus);
      
      
      if (binding!=null)
      { return binding;      
      }
    }
    
    if (type!=null)
    { 
      Type archetype=type;
      Channel binding=null;
      while (archetype!=null && binding==null)
      {
        if (debug)
        { log.fine("Checking methods for name '"+name+"' in "+archetype);
        }
        binding=bindMethods(archetype,source,focus,name,params);
        archetype=archetype.getArchetype();
      }
      if (binding!=null)
      { return binding;
      }
    }
        
    return null;
  }

  @Override
  public LinkedList<Signature> getSignatures(Channel<?> source)
    throws BindException
  { 
    LinkedList<Signature> signatures=super.getSignatures(source);
    signatures.addFirst
      (new Signature("@tuple",BeanReflector.getInstance(contentType)));
    
    
    for (Field<?> field : getFieldSet().fieldIterable())
    { 
      signatures.addFirst
        (new Signature
          (field.getName(),DataReflector.getInstance(field.getType())
          )
        );
    }
    return signatures;
  }
  
  @SuppressWarnings("unchecked")
  private Channel bindMethods
    (Type type,Channel source,Focus focus,String name,Expression[] params)
    throws BindException
  {
    if (params==null)
    { params=NULL_PARAMS;
    }
    Channel[] paramChannels=new Channel[params.length];
    for (int i=0;i<params.length;i++)
    { paramChannels[i]=focus.bind(params[i]);
    }
    if (debug)
    { 
      log.fine("Looking for "+name
        +"("+params.length+") in type "+type.getURI());
    }
    
    for (Method method : type.getMethods())
    {
      if (debug)
      { 
        log.fine("Checking "+method.getName()
          +"("+method.getParameterTypes().length+") in type "+type.getURI());
      }
      if (method.getName().equals(name) 
            && method.getParameterTypes().length==params.length
            )
      {
        
        if (debug)
        {
          log.fine("Possible method "+method.getName()
            +" in type "+type.getURI()
            );
        }
        int i=0;
        boolean match=true;
        for (Type<?> formalType : method.getParameterTypes())
        {
          if (debug)
          {
            log.fine("Checking param  "+i+" in "+method.getName()
              +" in type "+type.getURI()
              );
          }
          Channel paramChannel=paramChannels[i++];
            
          Reflector paramReflector=paramChannel.getReflector();
          Type paramType;
          if (paramReflector instanceof TypeReflector)
          { paramType=((TypeReflector) paramReflector).getType();
          }
          else
          { 
            try
            {
              paramType
                =Type.resolve
                  (ReflectionType.canonicalURI(paramReflector.getContentType())
                  );
            }
            catch (DataException x)
            { 
              x.printStackTrace();
              match=false;
              break; // Parameter compare loop
            }
          }
          if (debug)
          { log.fine(formalType+" <-- "+paramType);
          }
          if (!formalType.isAssignableFrom(paramType))
          { 
            match=false;
            break; // Parameter compare loop
          }
        }
         
        if (match)
        { 
          if (debug)
          {
            log.fine
              ("Found match for "+name
               +": return type is "+method.getReturnType());
          }
          return method.bind(focus, source, paramChannels);
          // Matching method- note: 
          // XXX Method resolution is first declared to match, not best fit
        }
      }
        
    }
    return null;  
    
  }
  
  
  @Override
  public <D extends Decorator<T>> D 
    decorate(Channel<T> binding,Class<D> decoratorInterface)
  { 
    // This depends on a system for registering and mapping decorators
    //   to Tuple constructs.
    return null;
  }
  
  @Override
  public Class<T> getContentType()
  { return contentType;
  }


  @Override
  public String toString()
  { 
    return super.toString()
      +(type!=null
          ?type.toString()
          :"(untyped)["+untypedFieldSet.toString()+"]"
       );
  }
  


  
}
