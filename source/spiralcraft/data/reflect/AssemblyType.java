//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.data.reflect;

import java.net.URI;
import java.util.ArrayList;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.BuilderChannel;
import spiralcraft.data.DataException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.builder.BuilderType;
import spiralcraft.data.core.SchemeImpl;
import spiralcraft.data.core.TypeImpl;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.util.InstanceResolver;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Functor;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.GatherChannel;

/**
 * A Type defined by Java object managed by an AssemblyClass.
 * 
 * @author mike
 *
 * @param <T>
 */
public class AssemblyType<T>
  extends TypeImpl<T>
  implements Functor<T>
{

  private boolean linked;
  private boolean delegate;
  
  @SuppressWarnings("unchecked")
  public static <X> Type<X> canonicalType(AssemblyClass assemblyClass)
    throws DataException
  { 
    URI uri=null;
    Type<Assembly> builderType=BuilderType.canonicalType(assemblyClass);
    if (builderType!=null)
    { uri=TypeResolver.desuffix(builderType.getURI(),".assy");
    }
    if (uri==null)
    { 
      throw new DataException
        ("Can't resolve canonical type for "+assemblyClass);
    }
    return Type.resolve(assemblyClass.getContainerURI());
  }
  
  
  protected final AssemblyClass assemblyClass;

  
  @SuppressWarnings("unchecked")
  public AssemblyType
    (TypeResolver resolver
    ,URI uri
    ,AssemblyClass assemblyClass
    )
    throws DataException
  {
    super(resolver, uri);
    this.assemblyClass=assemblyClass;
    
      
    this.nativeClass=(Class<T>) assemblyClass.getJavaClass();
  }
  

  @SuppressWarnings("unchecked")
  @Override
  public T fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  {
    // Ignore intermediate AssemblyType archetypes, as they represent
    //   supertypes that are already factored into the AssemblyResolver
    //   that will be chained to the supplied InstanceResolver
    Type nextArchetype=archetype;
    while (nextArchetype instanceof AssemblyType)
    { nextArchetype=nextArchetype.getArchetype();
    }
    return (T) nextArchetype.fromData(data,new AssemblyResolver(resolver));

  }
  

  @SuppressWarnings("unchecked")
  @Override
  public DataComposite toData(T val)
    throws DataException
  {
    // Ignore intermediate AssemblyType archetypes, as they represent
    //   supertypes that are already factored into the AssemblyResolver
    //   that will be chained to the supplied InstanceResolver
    Type<T> nextArchetype=(Type<T>) archetype;
    while (nextArchetype instanceof AssemblyType)
    { nextArchetype=(Type<T>) nextArchetype.getArchetype();
    }
    DataComposite base=(DataComposite) nextArchetype.toData(val);
    if (base.isTuple())
    { 
      EditableArrayTuple ret=new EditableArrayTuple(this);
      ret.copyFrom(base.asTuple());
      return ret;
    }
    else
    { 
      throw new UnsupportedOperationException
        ("Aggregate assembly types not supported");
      
//      EditableArrayListAggregate ret
//        =new EditableArrayListAggregate(this);
//      
////      ret.copyFrom(base.asAggregate());
//      return ret;
    }
  }
  
  @Override
  public boolean isAssignableFrom(Type<?> type)
  {
    if (delegate)
    { return archetype.isAssignableFrom(type);
    }
    else
    { 
      if (super.isAssignableFrom(type))
      { return true;
      }
      else
      { return archetype.isAssignableFrom(type);
      }
    }
  }
    
  @SuppressWarnings("unchecked")
  @Override
  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;
    pushLink(getURI());
    try
    {
      if (assemblyClass.getBaseClass()!=null)
      { this.archetype=AssemblyType.canonicalType(assemblyClass.getBaseClass());
      }
      else if (assemblyClass.getJavaClass()!=null)
      { 
        this.archetype
          =new ReflectionType<T>
            (resolver
            ,getURI()
            ,(Class<T>) assemblyClass.getJavaClass()
            ,(Class<T>) assemblyClass.getJavaClass()
            );
        delegate=true;
      }
      
      if (debug)
      { log.fine("archetype of "+getURI()+" is  "+archetype);
      }
      
      setScheme(new SchemeImpl());
      archetype.link();
      super.link();
    }
    finally
    { popLink();
    }
  }  
  
  class AssemblyResolver
    implements InstanceResolver
  {
    private final InstanceResolver delegate;
    
    public AssemblyResolver(InstanceResolver resolver)
    { this.delegate=resolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T resolve(
      Class<?> clazz)
      throws DataException
    {
      T oldVal=null;
      if (delegate!=null)
      {
        oldVal=(T) delegate.resolve(clazz);
      }
      
      Assembly<T> assembly=null;
      try
      {
        if (oldVal!=null)
        { 
          assembly=assemblyClass.wrap(null,oldVal);
          if (debug)
          { log.fine("Wrapping "+oldVal+" in "+assembly);
          }
        }
        else
        { 
          assembly=(Assembly<T>) assemblyClass.newInstance(null);
          if (debug)
          { log.fine("Instantiated "+assembly);
          }
        }
      }
      catch (BuildException x)
      { throw new DataException("Error constructing Assembly for "+getURI(),x);
      }
      return assembly.get();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Channel<T> bindChannel(
    Focus<?> focus,
    Channel<?>[] arguments)
    throws BindException
  {
    
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
    
    BeanReflector<T> beanReflector
      =(BeanReflector) BeanReflector.getInstance(nativeClass);
    
    // Call constructor via BeanReflector
    Channel<T> constructorChannel
      =beanReflector.bindChannel
        (focus,indexedParamList.toArray(new Channel[indexedParamList.size()])
        );
    
        
    // Configure using builder assemblyClass
    constructorChannel
      =new BuilderChannel(focus,constructorChannel,assemblyClass);
        
    // Apply additional named parameters
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
