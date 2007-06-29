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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleBinding;

import spiralcraft.data.lang.TupleDelegate;
import spiralcraft.data.DataException;

import java.util.HashMap;

import spiralcraft.registry.RegistryNode;
import spiralcraft.registry.Registrant;


import spiralcraft.util.StringConverter;

/**
 * An Assembly is an 'instance' of an AssemblyClass. It is a scaffold which
 *   supports a unique Java Object instance.
 *
 * The instantiation sequence is as follows:
 *
 * 1. new Assembly(AssemblyClass class)
 *    Internal Java class is resolved and the Object is instantiated.
 * 2. bind(parent) is called 
 *    Links this Assembly to its parent
 *    Binds all of the parent's properties into an array of _propertyBindings
 *    Property source expressions are compiled
 *    Nested Assemblies are instantiated and bound
 * 3. resolve() is called
 *    All properties are resolved
 *      Any nested Assemblies are resolved
 *      Property expressions are evaluated
 *      All values are applied
 *
 * After instantiation, the register(RegistryNode node) method is invoked 
 *   to provide context.
 */
public class Assembly<T>
  implements Focus<T>,Registrant
{
  private final AssemblyClass _assemblyClass;
  private Assembly _parent;
  private final SimpleBinding<T> _optic;
  private PropertyBinding[] _propertyBindings;
  private HashMap<String,Assembly> _importedSingletons;
  private HashMap<Expression,Channel> _channels;
  private Optic<?> _context;
  private boolean bound=false;
  private boolean resolved=false;
  
  
  /**
   * Construct an instance of the specified AssemblyClass,
   *   without binding properties
   */
  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
  Assembly(AssemblyClass assemblyClass)
    throws BuildException
  {
    _assemblyClass=assemblyClass;

    Class javaClass=_assemblyClass.getJavaClass();
    if (javaClass==null)
    { throw new BuildException("No java class defined for assembly");
    }
    try
    { _optic=new SimpleBinding(javaClass,null,false);
    }
    catch (BindException x)
    { throw new BuildException("Error binding class "+javaClass.getName()+": "+x,x);
    }

  }

  
  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
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
        _optic.set((T) instance);
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
        _optic.set((T) new TupleDelegate(javaClass).get());
      }
      catch (DataException x)
      { throw new BuildException("Error binding instance",x);
      }
      catch (BindException x)
      { throw new BuildException("Error binding instance",x);
      }
    }
    
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
   * Construct an instance of the specified AssemblyClass
   */
  Assembly(AssemblyClass assemblyClass,Assembly parent)
    throws BuildException
  { 
    this(assemblyClass);
    bind(parent);
  }

  /**
   *@return Whether this Assembly's properties have been bound
   */
  boolean isBound()
  { return bound;
  }
  
  void bind(Assembly parent)
    throws BuildException
  {
    if (bound)
    { throw new BuildException("Already bound properties");
    }
    bound=true;
    _parent=parent;
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
  
  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
  void setDefaultInstance(T val)
  {
    if (resolved)
    { throw new IllegalStateException("Cannot setDefaultInstance() when already resolved");
    }
    if (_assemblyClass.getJavaClass().isAssignableFrom(val.getClass()))
    { _optic.set(val);
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
  void resolve()
    throws BuildException
  {
    if (resolved)
    { throw new IllegalStateException("Already resolved");
    }
    resolved=true;
    
    if (_optic.get()==null)
    { constructInstance();
    }
    
    if (_propertyBindings!=null)
    {
      for (PropertyBinding binding: _propertyBindings)
      { binding.resolve();
      }
    }
  }
  

  
  
  public void register(RegistryNode node)
  {
    node.registerInstance(Assembly.class,this);

    T instance=_optic.get();
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

  /**
   * Return the Assembly which contains this one
   */
  public Assembly<?> getParent()
  { return _parent;
  }

  public void registerSingletons(Class[] singletonInterfaces,Assembly singleton)
    throws BuildException
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

  
  //////////////////////////////////////////////////  
  //
  // Implementation of spiralcraft.lang.Focus
  //
  //////////////////////////////////////////////////  

  /**
   * implement Focus.getParentFocus()
   */
  public Focus<?> getParentFocus()
  { return _parent;
  }

  /**
   * implement Focus.getContext()
   */
  public Optic<?> getContext()
  { 
    if (_context!=null)
    { return _context;
    }
    else
    { return _optic;
    }
  }

  /**
   * implement Focus.getSubject()
   */
  public Optic<T> getSubject()
  { return _optic;
  }
 
  /**
   * implement Focus.findFocus()
   */
  public Focus<?> findFocus(String name)
  { 
    
    if (_importedSingletons!=null)
    { 
      Assembly assembly=_importedSingletons.get(name);
      if (assembly!=null)
      { return assembly;
      }
    }
    
    if (_assemblyClass.isFocusNamed(name))
    { return this;
    }

    if (_parent!=null)
    { return _parent.findFocus(name);
    }
    
    return null;

  }

  /**
   * implement Focus.bind()
   */
  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
  public synchronized Channel bind(Expression expression)
    throws BindException
  { 
    Channel channel=null;
    if (_channels==null)
    { _channels=new HashMap<Expression,Channel>();
    }
    else
    { channel=_channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      _channels.put(expression,channel);
    }
    return channel;
  }


}


