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

import java.util.WeakHashMap;

import java.lang.ref.WeakReference;
import java.net.URI;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.TypeModel;

import spiralcraft.lang.kit.AbstractReflector;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.NullChannel;
//import spiralcraft.log.ClassLog;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.common.Constant;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Type;
import spiralcraft.data.core.ArrayType;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.session.BufferAggregate;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.BufferType;

/**
 * Abstract base class maps a DataComposite into the spiralcraft.lang namespace
 *
 * @author mike
 *
 * @param <T> The type of DataComposite we are mapping
 */
public abstract class DataReflector<T extends DataComposite>
  extends AbstractReflector<T>
  implements TypedDataReflector<T>
{
  private static final TypeModel TYPE_MODEL
    =DataTypeModel.getInstance();
  
//  private static final ClassLog log=ClassLog.getInstance(DataReflector.class);
  
  // 
  // XXX Use weak map
  private static final WeakHashMap<Type<?>,WeakReference<Reflector<?>>>
    SINGLETONS=new WeakHashMap<Type<?>,WeakReference<Reflector<?>>>();
  
  private static final WeakHashMap<Type<?>,WeakReference<DataReflector<?>>>
    EXT_SINGLETONS=new WeakHashMap<Type<?>,WeakReference<DataReflector<?>>>(); 
  
  protected final Type<T> type;
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // We only create Reflector with erased type
  public synchronized static final 
    <T> Reflector<T> getInstance(Type type)
    throws BindException
  { 
    
    if (type==null)
    { throw new BindException("Type cannot be null");
    }
    Reflector broker=null;
    
    WeakReference<Reflector<?>> ref=SINGLETONS.get(type);
    if (ref!=null)
    { broker=ref.get();
    }
    if (broker==null)
    {
      if (type.isPrimitive())
      { broker=new PrimitiveReflector(type);
      }
      else if (type.isAggregate())
      { 
        
        if (type instanceof BufferType)
        { broker=new AggregateReflector(type,BufferAggregate.class);
        }
        else if (type instanceof ArrayType)
        { 
//          log.fine
//            ("Using native array of "+type.getContentType()
//              +" for aggregate type "+type.getURI()
//            );
          broker=new ArrayReflector
            (DataReflector.getInstance(type.getContentType()));
        }
        else
        { broker=new AggregateReflector(type,Aggregate.class);
        }
      }
      else
      { 
        if (type instanceof BufferType)
        { broker=new BufferReflector(type,BufferTuple.class);
        }
        else if (type instanceof ArrayType)
        { 
          broker=new ArrayReflector
            (DataReflector.getInstance(type.getContentType()));
        }
        else
        { broker=new TupleReflector(type,Tuple.class);
        }
      }
      SINGLETONS.put(type,new WeakReference(broker));
    }
      
    return broker;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public synchronized static final <T extends DataComposite> 
    Reflector<T> getExternalizedInstance(Type type)
    throws BindException
  { 
    Reflector standardReflector=getInstance(type);
    if (standardReflector instanceof DataReflector)
    { return (DataReflector) standardReflector;
    }
    
    DataReflector broker=null;
    
    WeakReference<DataReflector<?>> ref=EXT_SINGLETONS.get(type);
    if (ref!=null)
    { broker=ref.get();
    }
    if (broker==null)
    {
      if (type.isAggregate())
      { 
        if (type instanceof BufferType)
        { broker=new AggregateReflector(type,BufferAggregate.class);
        }
        else
        { broker=new AggregateReflector(type,Aggregate.class);
        }
      }
      else
      {        
        if (type instanceof BufferType)
        { broker=new BufferReflector(type,BufferTuple.class);
        }
        else
        { broker=new TupleReflector(type,Tuple.class);
        }
      }
      EXT_SINGLETONS.put(type,new WeakReference(broker));
      
    }
    return broker;
  }
  
  public DataReflector(Type<T> type)
  { this.type=type;
  }
  
  @Override
  public TypeModel getTypeModel()
  { return TYPE_MODEL;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Reflector<T> subtype(T val)
  {
    if (val==null)
    { return null;
    }
    
    Type<T> type=(Type<T>) val.getType();
    try
    { 
      if (type!=null)
      { return DataReflector.getInstance(type);
      }
      else
      { return null;
      }
    }
    catch (BindException x)
    { 
      throw new AccessException
        ("Error retrieving type reflector: "+val.getType());
    }
  }

  @Override
  public boolean accepts(Object val)
  {
    if (val==null)
    { return true;
    }
    
    if (!(val instanceof DataComposite))
    { return false;
    }
    
    Type<?> type=((DataComposite) val).getType();
    if (type!=null)  
    { return this.type.isAssignableFrom(type);
    }
    return false;
  }
  
  @Override
  @Constant
  public Type<T> getType()
  { return type;
  }
  
  @Override
  @Constant
  public URI getTypeURI()
  {
    if (type!=null)
    { return type.getURI();
    }
    return null;
  }


  @Override
  public boolean isAssignableTo(URI typeURI)
  {
    if (type==null || !typeURI.isAbsolute())
    { return false;
    }
    try
    {
      Type<?> requestedType=Type.resolve(typeURI);
      return requestedType.isAssignableFrom(type);
    }
    catch (DataException x)
    { return false;
    }
    

  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public boolean isAssignableFrom(Reflector<?> other)
  { 
    if (other instanceof DataReflector<?>)
    { return getType().isAssignableFrom(((DataReflector<?>) other).getType());
    }
    else
    { 
      return Void.class==other.getContentType() 
        || Void.TYPE==other.getContentType()
        || (type.getClass()==ReflectionType.class 
            && type.getNativeClass()==(Class) Object.class
            );
    }
  }  
  
  @Override
  public String toString()
  { return super.toString();
  }
  
  /**
   * <p>Determines the Type of data represented by an Expression scoped against
   *   this Type's FieldSet.
   * 
   * @param expr
   * @return The type of an expression when scoped to a Focus that has a 
   *   Channel with this Reflector as its subject.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <X> Type<X> getTypeAsSubject(Expression<X> expr)
    throws DataException
  {
    Channel<?> channel=new NullChannel(this);
    
    Focus teleFocus=new TeleFocus(null,channel);
    try
    {
      Reflector reflector=teleFocus.bind(expr).getReflector();
      if (reflector instanceof TypedDataReflector)
      { return ((TypedDataReflector) reflector).getType();
      }
      else
      { 
        return Type.<X>resolve
          (ReflectionType.canonicalURI(reflector.getContentType()));
      }
    }
    catch (BindException x)
    { throw new DataException("Error binding expression "+expr+" to "+this,x);
    }
      
    
    
  }
  
  @Override
  public Reflector<?> disambiguate(Reflector<?> alternate)
  {
//    if (alternate instanceof PrimitiveReflector<?>)
//    { return this;
//    }
//    else 
    if (alternate instanceof BeanReflector<?>)
    { 
      // Don't hide BeanReflectors with tuples
      return alternate;
    }
    else if (getTypeModel()==alternate.getTypeModel())
    { return this;
    }
    else
    { 
      // Defer to dependent type model
      return alternate.disambiguate(this);
    }
  }  
  
  @Override
  public Reflector<?> getCommonType(Reflector<?> other)
  {
    if (other==this)
    { return this;
    }
    
    if (other instanceof DataReflector)
    {
     
      Type<?> otherType=((DataReflector<?>) other).getType();
      if (type.isAssignableFrom(otherType))
      { return this;
      }
      else if (otherType.isAssignableFrom(type))
      { return other;
      }
      else if (type.getBaseType()!=null)
      { 
        try
        {
          return DataReflector.getInstance(type.getBaseType())
            .getCommonType(other);
        }
        catch (BindException x)
        { 
          throw new RuntimeException
            ("Error getting DataReflector for type "
             +type.getBaseType().getURI(),x
            );
        }
      }
      else
      { return super.getCommonType(other);
      }
    }
    else
    { return super.getCommonType(other);
    }
      
  }  
}
