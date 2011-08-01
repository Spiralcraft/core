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
import spiralcraft.lang.Functor;
import spiralcraft.lang.Signature;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.GatherChannel;
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
  implements Functor<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(TupleReflector.class);
  
  private static boolean debug=false;
  
  private static final Expression<?>[] NULL_PARAMS = new Expression[0];
  
  private final FieldSet untypedFieldSet;
  
  private final Class<T> contentType;
  
  private final Functor<T> constructor;

  @SuppressWarnings({ "unchecked", "rawtypes" }) // We only create Reflector with erased type
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
  

  @SuppressWarnings("unchecked")
  TupleReflector(Type<T> type,Class<T> contentType)
  { 
    super(type);
    untypedFieldSet=null;
    this.contentType=contentType!=null?contentType:(Class<T>) Tuple.class;
    if (this.type!=null && this.type instanceof Functor)
    { this.constructor=(Functor<T>) this.type;
    }
    else
    { this.constructor=null;
    }
    
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
    this.contentType=contentType!=null?contentType:(Class<T>) Tuple.class;
    if (this.type!=null && this.type instanceof Functor)
    { this.constructor=(Functor<T>) this.type;
    }
    else
    { this.constructor=null;
    }

//    for (Field field : fieldSet.fieldIterable())
//    { fieldTranslators.put(field.getName(),new FieldTranslator(field));
//    }
  }

  public FieldSet getFieldSet()
  { return untypedFieldSet!=null?untypedFieldSet:type.getFieldSet();
  }
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
  @SuppressWarnings({ "unchecked", "rawtypes" }) // We haven't genericized the data package yet
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
  @SuppressWarnings({ "unchecked", "rawtypes" }) // We haven't genericized the data package yet
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
    
    
    
    Type type=getType();
    if (type!=null)
    { type.link();
    }
    Field field=null;
    
    

    if (params==null)
    {
      Channel binding=source.getCached(name);
      if (binding!=null)
      { return binding;
      }
      
      if (type!=null)
      { field=type.getField(name);
      }
      else
      { field=untypedFieldSet.getFieldByName(name);
      }
    
      if (field!=null)
      {
              
        binding=field.bindChannel(source,focus,null);
      
        if (binding!=null)
        { 
          source.cache(name,binding);
          return binding;      
        }
      }
    }
    else
    {
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
    if (type!=null)
    {
      Method[] methods=type.getMethods();
      if (methods!=null)
      { 
        for (Method method: methods)
        {
          signatures.addFirst(method.getSignature());
          
        }
      }
    }
    return signatures;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Channel bindMethods
    (Type type
    ,Channel source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {
    if (params==null)
    { params=NULL_PARAMS;
    }
    boolean staticMethod
      =(source.isConstant() 
       && source.get()==null
       && source.getContext()==focus
       );
    Channel[] paramChannels=new Channel[params.length];
    
    ArrayList<Channel> sigChannels=new ArrayList<Channel>();

    for (int i=0;i<params.length;i++)
    { 
      boolean endOfSig=false;
      paramChannels[i]=focus.bind(params[i]);
      if (paramChannels[i] instanceof BindingChannel)
      { endOfSig=true;
      }
      else
      { 
        if (!endOfSig)
        { sigChannels.add(paramChannels[i]);
        }
        else
        { 
          throw new BindException
            ("Positional arguments parameters must preceed named parameters in: "
            +params[i].getText()
            );
        }
      }
    }
    
    if (debug)
    { 
      log.fine("Looking for "+name
        +"("+sigChannels.size()+") in type "+type.getURI());
    }
    
    for (Method method : type.getMethods())
    {
      if (debug)
      { 
        log.fine("Checking "+method.getName()
          +"("+method.getParameterTypes().length+") in type "+type.getURI());
      }
      if (method.getName().equals(name) 
            && method.getParameterTypes().length==sigChannels.size()
            && method.isStatic()==staticMethod
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
          
          Channel paramChannel=sigChannels.get(i++);
            
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
          return method.bind(source, paramChannels);
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
          ?type.getURI()
          :"(untyped)["+untypedFieldSet.toString()+"]"
       );
  }

  

  @Override
  /**
   * Create a constructor channel
   */
  public Channel<T> bindChannel(
    Focus<?> focus,
    Channel<?>[] arguments)
    throws BindException
  {
    
    if (constructor!=null)
    { return constructor.bindChannel(focus,arguments);
    }
    
    ArrayList<Channel<?>> indexedParamList=new ArrayList<Channel<?>>();
    ArrayList<Channel<?>> namedParamList=new ArrayList<Channel<?>>();
    
    boolean endOfParams=false;
    for (Channel<?> chan : arguments)
    { 
      if (chan instanceof BindingChannel<?>)
      { 
        endOfParams=true;
        namedParamList.add(chan);
      }
      else
      {
        if (endOfParams)
        { 
          throw new BindException
            ("Positional parameters must preceed named parameters");
        }
        indexedParamList.add(chan);
        
      }
      
    }      
        
      
    Channel<?>[] indexedParams
      =indexedParamList.toArray(new Channel[indexedParamList.size()]);      

    if (indexedParams.length>0)
    { 
      throw new BindException
        ("Tuple constructor does not accept indexed parameters");
    }
    
    Channel<T> constructorChannel
      =new TupleConstructorChannel<T>(this,focus);
   
    if (namedParamList.size()>0)
    { 
      constructorChannel
        =new GatherChannel<T>
          (constructorChannel
          ,namedParamList.toArray
            (new BindingChannel[namedParamList.size()])
          );
    }
      
    return constructorChannel;    

  }

  
}
