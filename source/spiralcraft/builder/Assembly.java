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
package spiralcraft.builder;

import spiralcraft.lang.BaseFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.data.lang.TupleDelegate;
import spiralcraft.data.DataException;

import java.util.HashMap;

import spiralcraft.registry.RegistryNode;
import spiralcraft.registry.Registrant;


import spiralcraft.util.string.StringConverter;

import spiralcraft.lang.AccessException;

import spiralcraft.log.ClassLog;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * An Assembly is an 'instance' of an AssemblyClass. It is a scaffold which
 *   supports a unique Java Object instance.
 *
 * The instantiation sequence is as follows:
 *
 * 1. new Assembly(AssemblyClass class)
 *    Internal Java class is resolved 
 * 2. bind(parentFocus) is called 
 *    Links this Assembly to its parent focus
 *    Binds all of the parent's properties into an array of _propertyBindings
 *    Property source expressions are compiled
 *    Nested Assemblies are instantiated and bound
 * 3. resolve() is called
 *    The object is instantiated
 *    All properties are resolved
 *      Any nested Assemblies are resolved
 *      Property expressions are evaluated
 *      All values are applied
 *
 * After instantiation, the register(RegistryNode node) method is invoked 
 *   to provide context.
 */
@SuppressWarnings("unchecked") // Heterogeneous design- does not use generics
public class Assembly<T>
  implements Registrant,Lifecycle
{
  @SuppressWarnings("unused")
  private static ClassLog log=ClassLog.getInstance(Assembly.class);
  
  private final AssemblyClass _assemblyClass;
  // private Assembly<?> _parent;
  private PropertyBinding[] _propertyBindings;
  private HashMap<String,Assembly> _importedSingletons;
  private boolean bound=false;
  private boolean resolved=false;
  private final AssemblyFocus<T> focus;
  private boolean factoryMode=false;
  
  
  /**
   * Construct an instance of the specified AssemblyClass,
   *   without binding properties
   */
  Assembly(AssemblyClass assemblyClass,boolean factoryMode)
    throws BuildException
  {
    // log.fine("Plain Constructor");

    _assemblyClass=assemblyClass;
    this.factoryMode=factoryMode;

    Class javaClass=_assemblyClass.getJavaClass();
    if (javaClass==null)
    { throw new BuildException("No java class defined for assembly");
    }

    focus=new AssemblyFocus();
    if (factoryMode)
    {
      focus.setSubject
        (new ThreadLocalChannel(BeanReflector.getInstance(javaClass))
        );
    }
    else
    {
      focus.setSubject(new SimpleChannel(javaClass,null,false));
    }
        


  }

  
  /**
   * Called from resolve() to actually instantiate object
   * 
   * @throws BuildException
   */
  private void constructInstance()
    throws BuildException
  {
    Class javaClass=_assemblyClass.getJavaClass();
    if (javaClass==null)
    { throw new BuildException("No java class defined for assembly");
    }

    if (!javaClass.isInterface())
    { 
      try
      {
        String constructor=_assemblyClass.getConstructor();
        Object instance;
        if (constructor!=null)
        { instance=constructFromString(javaClass,constructor);
        }
        else
        { instance=javaClass.newInstance();
        }
        focus.getSubject().set((T) instance);
      }
      catch (AccessException x)
      { throw new BuildException("Error publishing instance in Focus chain",x);
      }
      catch (InstantiationException x)
      { throw new BuildException("Error instantiating assembly",x);
      }
      catch (IllegalAccessException x)
      { throw new BuildException("Error instantiating assembly",x);
      }
    }
    else
    {
      try
      { 
        // Use a proxy to auto-implement the interface
        focus.getSubject().set((T) new TupleDelegate(javaClass).get());
      }
      catch (AccessException x)
      { throw new BuildException("Error publishing instance in Focus chain",x);
      }
      catch (DataException x)
      { throw new BuildException("Error binding instance",x);
      }
      catch (BindException x)
      { throw new BuildException("Error binding instance",x);
      }
    }
    
  }
  
  boolean isFactoryMode()
  { return factoryMode;
  }
  
  Object constructFromString(Class clazz,String constructor)
    throws BuildException
  {
    StringConverter converter=StringConverter.getInstance(clazz);
    if (converter!=null)
    { return converter.fromString(constructor);
    }
    else
    { throw new BuildException("Can't construct a "+clazz+" from text"); 
    }
  }


  /**
   *@return Whether this Assembly's properties have been bound
   */
  boolean isBound()
  { return bound;
  }
  
  void bind(Focus<?> parentFocus)
    throws BuildException
  {
    if (bound)
    { throw new BuildException("Already bound properties");
    }
    bound=true;

    focus.setParentFocus(parentFocus);
    _propertyBindings=_assemblyClass.bindProperties(this);
  }
  
  PropertyBinding getPropertyBinding(int index)
  { return _propertyBindings[index];
  }
  
  boolean isResolved()
  { return resolved;
  }
  
  /**
   * Specify a default instance for this Assembly to apply properties to, as opposed to
   *   constructing a new instance. Must be called before resolve().
   */
  void setDefaultInstance(T val)
  {
    if (resolved)
    { throw new IllegalStateException("Cannot setDefaultInstance() when already resolved");
    }
    if (_assemblyClass.getJavaClass().isAssignableFrom(val.getClass()))
    { 
      try
      { focus.getSubject().set(val);
      }
      catch (AccessException x)
      { x.printStackTrace();
      }

    }
    else
    { 
      // XXX: Remove warning once functionality is well verified
      //
      // If we opt to create an incompatible type, that's still valid as long as it 
      //   is still compatible with the formal property type, not the actual type of the
      //   default object. 
      System.err.println
        ("Assembly: Default value of type "
        +val.getClass()
        +" is not compatible with "
        +_assemblyClass.getJavaClass()
        );
    }
  }
  
  /**
   * Recursively resolve all instances.
   * 
   * PropertyBinding.resolve() will:
   *  1. resolve sub-Assemblies
   *  2. retrieve the instance/value for the property and
   *  3. apply the value to this Assembly's bean.
   */
  public void resolve()
    throws BuildException
  {
    if (resolved)
    { throw new IllegalStateException("Already resolved");
    }
    if (!factoryMode)
    { resolved=true;
    }
    
    if (focus.getSubject().get()==null)
    { constructInstance();
    }
    
    if (_propertyBindings!=null)
    {
      for (PropertyBinding binding: _propertyBindings)
      { binding.resolve();
      }
    }
  }
  
  /**
   * Recursively release all instances
   *
   * PropertyBinding.release() will release sub-Assemblies
   */
  public void release()
  {
    focus.getSubject().set(null);
    if (_propertyBindings!=null)
    {
      for (PropertyBinding binding: _propertyBindings)
      { binding.release();
      }
    }
    
  }

  
  @Override
  public void register(RegistryNode node)
  {
    node.registerInstance(Assembly.class,this);

    T instance=focus.getSubject().get();
    if (instance instanceof Registrant)
    { ((Registrant) instance).register(node);
    }
    
    // Register the Object associated with this node as its
    //   concrete Class identity.
    // XXX We changed this from registering as Object.class- make sure
    //   nothing breaks
    node.registerInstance
      (_assemblyClass.getJavaClass()
      ,instance
      );

    for (int i=0;i<_propertyBindings.length;i++)
    { _propertyBindings[i].register(node);
    }
  }

  @Override
  public void start() throws LifecycleException
  {
    T instance=focus.getSubject().get();
    if (instance instanceof Lifecycle)
    { ((Lifecycle) instance).start();
    }
    
  }


  @Override
  public void stop() throws LifecycleException
  {
    T instance=focus.getSubject().get();
    if (instance instanceof Lifecycle)
    { ((Lifecycle) instance).stop();
    }
  }
  
  /**
   * Return the Assembly which contains this one
   */
//  public Assembly<?> getParent()
//  { return _parent;
//  }

  public void registerSingletons(Class[] singletonInterfaces,Assembly singleton)
  { 
    if (_importedSingletons==null)
    { _importedSingletons=new HashMap<String,Assembly>();
    }
    for (int i=0;i<singletonInterfaces.length;i++)
    { _importedSingletons.put(singletonInterfaces[i].getName(),singleton);
    }
    
  }
  
  /**
   * Return the singleton interfaces exported by this
   *   Assembly. 
   *
   * Called by PropertyBinding when it exports singletons from a
   *   'child' assembly in a property up to its parent assembly.
   */
  public Class[] getSingletons()
  { 
    // XXX Should we export the imported singletons as well? We would have
    //   to change that interface to map Classes to objects as opposed to
    //   names in order to do this.
    return _assemblyClass.getSingletons();
  }

  public AssemblyClass getAssemblyClass()
  { return _assemblyClass;
  }

  public Focus<T> getFocus()
  { return focus;
  }
  
  public T get()
  { return getFocus().getSubject().get();
  }
  
  class AssemblyFocus<tFocus>
    extends BaseFocus<tFocus>
    implements Focus<tFocus>
  {
    public boolean isFocus(URI uri)
    { 
      if (_assemblyClass.isFocus(uri))
      { return true;
      }
      
      
      if (subject==null)
      { return false;
      }
      
      try
      {
        URI shortURI
          =new URI(uri.getScheme(),uri.getAuthority(),uri.getPath(),null,null);
        if  (subject.getReflector().isAssignableTo(shortURI))
        { return true;
        }
      }
      catch (URISyntaxException x)
      { x.printStackTrace();
      }
      return false;
    }
    
    public Focus<?> findFocus(URI uri)
    {       
      if (isFocus(uri))
      {
        String query=uri.getQuery();
        String fragment=uri.getFragment();

        if (query==null)
        {
          if (fragment==null || fragment=="spiralcraft.builder")
          { return this;
          }
        }

      }
      
      if (parent!=null)
      { return parent.findFocus(uri);
      }
      else
      { return null;
      }
    }
    
  }
  
}


