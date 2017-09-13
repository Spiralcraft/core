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
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.data.lang.TupleDelegate;
import spiralcraft.data.DataException;

import java.util.HashMap;

import spiralcraft.util.string.StringConverter;

import spiralcraft.lang.AccessException;

import spiralcraft.log.ClassLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;

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
@SuppressWarnings({"unchecked","rawtypes"}) // Heterogeneous design- does not use generics
public class Assembly<T>
  implements 
    Lifecycle
{
  private static ClassLog log=ClassLog.getInstance(Assembly.class);
  
  private final AssemblyClass _assemblyClass;
  // private Assembly<?> _parent;
  private PropertyBinding[] _propertyBindings;
  private HashMap<String,Assembly> _importedSingletons;
  private boolean bound=false;
  private boolean resolved=false;
  private final AssemblyFocus<T> focus;
  private boolean factoryMode=false;
  private Channel<T> instanceSourceChannel;
  
  
  /**
   * Construct an instance of the specified AssemblyClass,
   *   without binding properties
   */
  Assembly(AssemblyClass assemblyClass,Focus parentFocus,boolean factoryMode)
    throws BuildException
  {
    // log.fine("Plain Constructor");

    _assemblyClass=assemblyClass;
    this.factoryMode=factoryMode;

    Class javaClass=_assemblyClass.getJavaClass();
    if (javaClass==null)
    { throw _assemblyClass.newBuildException
        ("No java class defined for assembly",null);
    }

    focus=new AssemblyFocus(parentFocus);
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
    Class<T> javaClass=(Class<T>) _assemblyClass.getJavaClass();

    if (instanceSourceChannel!=null)
    { 
      T instance=instanceSourceChannel.get();
      if (instance!=null)
      {
        focus.getSubject().set(instance);
        updateDeclarable(instance);
        return;
      }
      else
      { 
        log.warning
          ("Instance source channel returned null, attempting instantiation of "
          +javaClass
          );
      }
    }
    
    String constructor=_assemblyClass.getConstructor();
    if (javaClass==null)
    { 
      throw _assemblyClass.newBuildException
        ("No java class defined for assembly",null);
    }
    else if (constructor==null && Modifier.isAbstract(javaClass.getModifiers()))
    { 
      throw _assemblyClass.newBuildException
        ("Cannot instantiate an abstract class "+javaClass.getName(),null);
    }
    else if (constructor!=null || !javaClass.isInterface())
    { 
      try
      {
        T instance;
        if (constructor!=null)
        { instance=constructFromString(javaClass,constructor);
        }
        else
        { instance=javaClass.getDeclaredConstructor().newInstance();
        }
        focus.getSubject().set(instance);
        updateDeclarable(instance);
      }
      catch (AccessException x)
      { throw _assemblyClass.newBuildException("Error publishing instance in Focus chain",x);
      }
      catch (InstantiationException x)
      { throw _assemblyClass.newBuildException("Error instantiating assembly",x);
      }
      catch (InvocationTargetException x)
      { throw _assemblyClass.newBuildException("Error instantiating assembly",x);
      }
      catch (NoSuchMethodException x)
      { throw _assemblyClass.newBuildException("Error instantiating assembly",x);
      }
      catch (IllegalAccessException x)
      { 
        AssemblyClass dumpClass=_assemblyClass;
        while (dumpClass!=null)
        { 
          log.debug(dumpClass.toString());
          dumpClass=dumpClass.getBaseClass();
        }
        throw _assemblyClass.newBuildException("Error instantiating assembly",x);
      }
    }
    else
    {
      try
      { 
        // Use a proxy to auto-implement the interface
        T instance=(T) new TupleDelegate(javaClass).get();
        focus.getSubject().set(instance);
        updateDeclarable(instance);
      }
      catch (AccessException x)
      { throw _assemblyClass.newBuildException("Error publishing instance in Focus chain",x);
      }
      catch (DataException x)
      { throw _assemblyClass.newBuildException("Error binding instance",x);
      }
      catch (BindException x)
      { throw _assemblyClass.newBuildException("Error binding instance",x);
      }
    }
    
  }
  
  void updateDeclarable(T instance)
  {
    if (_assemblyClass.isDeclarable() 
        && _assemblyClass.getDeclarationInfo()!=null
        )
    { 
      ((Declarable) instance).setDeclarationInfo
        (_assemblyClass.getDeclarationInfo());
    }
  }
  
  void setInstanceSourceChannel(Channel<T> sourceChannel)
  { this.instanceSourceChannel=sourceChannel;
  }
  
  boolean isFactoryMode()
  { return factoryMode;
  }
  
  T constructFromString(Class clazz,String constructor)
    throws BuildException
  {
    StringConverter<T> converter=StringConverter.<T>getInstance(clazz);
    if (converter!=null)
    { 
      PrefixResolver resolver=_assemblyClass.getPrefixResolver();
      
      if (resolver!=null)
      { NamespaceContext.push(resolver);
      }
      
      try
      { return converter.fromString(constructor);
      }
      finally
      { 
        if (resolver!=null)
        { NamespaceContext.pop();
        }
      }
    }
    else
    { throw _assemblyClass.newBuildException
        ("Can't construct a "+clazz+" from text",null); 
    }
  }


  /**
   *@return Whether this Assembly's properties have been bound
   */
  boolean isBound()
  { return bound;
  }
  
  void bind()
    throws BuildException
  {
    if (bound)
    { throw new BuildException("Already bound properties");
    }
    bound=true;
    String instanceX= _assemblyClass.getInstanceX();
    if (instanceX!=null)
    {
      try
      { 
        NamespaceContext.push(_assemblyClass.getPrefixResolver());
        try
        {
          Expression instanceSource=Expression.parse(instanceX);
          instanceSourceChannel=focus.getParentFocus().bind(instanceSource);
         
        }
        finally
        { NamespaceContext.pop();
        }
      }
      catch (ParseException x)
      { 
        throw _assemblyClass.newBuildException
          ("Error parsing constructor expression 'x'",x);
      }
      catch (BindException x)
      { 
        throw _assemblyClass.newBuildException
          ("Error binding to instance source "+instanceX,x);
      }
    }
    _propertyBindings=_assemblyClass.bindProperties(this);
  }
  
  PropertyBinding getPropertyBinding(int index)
  { return _propertyBindings[index];
  }
  
  boolean isResolved()
  { return resolved;
  }
  
  /**
   * Specify a default instance for this Assembly to apply properties to,
   *   as opposed to constructing a new instance. Must be called before
   *   resolve().
   */
  private void setDefaultInstance(T val)
  {
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
      // If the specified class is not assignable from the default instance
      //   type then we will discard the default instance and create a new
      //   value for the property.
      log.info
        (  (_assemblyClass.getContainingProperty()!=null
            ?""+_assemblyClass.getContainingProperty().getSourceInfo()
              :""
           )
        +": "+(_assemblyClass.getBaseURI()!=null?_assemblyClass.getBaseURI().toString():"")
        +" ("+_assemblyClass.getJavaClass().getName()+")"
        +" will replace default instance of type "
        +val.getClass()
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
  public void resolve(T defaultInstance)
    throws BuildException
  {
    if (resolved)
    { throw new IllegalStateException("Already resolved");
    }
    
    _assemblyClass.pushContext();
    try
    {
      if (!factoryMode)
      { resolved=true;
      }
      else
      { push();
      }
    
      if (defaultInstance!=null)
      { setDefaultInstance(defaultInstance);
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
    catch (BuildException x)
    { 
      if (factoryMode)
      { pop();
      }
      throw x;
    }
    catch (RuntimeException x)
    { 
      if (factoryMode)
      { pop();
      }
      throw x;
    }
    finally
    {  _assemblyClass.popContext();
    }
    
  }

  
  private void push()
  {
    // log.fine("PUSH "+_assemblyClass.getContainerURI()+":"+ArrayUtil.format(_assemblyClass.getInnerPath(),",",null));
    ((ThreadLocalChannel) focus.getSubject()).push();
  }
  
  private void pop()
  { 
    // log.fine("POP "+_assemblyClass.getContainerURI()+":"+ArrayUtil.format(_assemblyClass.getInnerPath(),",",null));
    ((ThreadLocalChannel) focus.getSubject()).pop();
  }
  
  /**
   * When the property that created this Assembly is something created by
   *   default, don't construct a default instance
   */
  public void resolveDefault()
    throws BuildException
  {
    // 2009-04-30 miketoth
    //
    //   Don't construct anything automatically when referenced by a 
    //     PropertyDescriptor automatically constructed by discovery.
    //
    
    if (resolved)
    { throw new IllegalStateException("Already resolved");
    }
    
    _assemblyClass.pushContext();
    try
    {
      if (!factoryMode)
      { resolved=true;
      }
      else
      { push();
      }
    
      if (_propertyBindings!=null)
      {
        for (PropertyBinding binding: _propertyBindings)
        { binding.resolveDefault();
        }
      }
    }
    catch (BuildException x)
    { 
      if (factoryMode)
      { pop();
      }
      throw x;
    }
    catch (RuntimeException x)
    { 
      if (factoryMode)
      { pop();
      }
      throw x;
    }      
    finally
    {  _assemblyClass.popContext();
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
    if (factoryMode)
    { pop();
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
    public AssemblyFocus(Focus<?> parentFocus)
    { 
      super(parentFocus);
      AssemblyClass assemblyClass
        =_assemblyClass;
      while (assemblyClass.getBaseClass()!=null)
      { 
        if (assemblyClass.getDeclarationInfo()!=null)
        { 
          URI declaredType=assemblyClass.getDeclarationInfo().getDeclaredType();
          if (declaredType!=null)
          { addAlias(declaredType);
          }
        }
        assemblyClass=assemblyClass.getBaseClass();
      }
    }
    
    @Override
    public boolean isFocus(URI uri)
    { 
      if (_assemblyClass.isFocus(uri))
      { return true;
      }
      else
      {  return super.isFocus(uri);
      }
    }
    
  }
  
}


