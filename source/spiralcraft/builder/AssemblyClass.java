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

import java.net.URI;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import spiralcraft.util.ArrayUtil;

/**
 * An AssemblyClass defines the behavior and content of an Assembly. One
 *   instance of an AssemblyClass is created within a given ClassLoader for
 *   each unique URI used to reference an assembly definition.
 * 
 * An AssemblyClass instantiates one or more instances of an 
 *   Assembly.
 */
public class AssemblyClass
{
  private final URI _sourceUri;

  // _basePackage and _baseName are used
  //   to resolve external base class or java class
  //   if _baseAssemblyClass hasn't been supplied at construction
  private final URI _basePackage;
  private final String _baseName;
  
  private AssemblyClass _baseAssemblyClass;

  private final AssemblyClass _outerClass;
  private final AssemblyLoader _loader;
  private String _declarationName;
  private Class _javaClass;
  private LinkedList<PropertySpecifier> _propertySpecifiers;
  private LinkedList<PropertySpecifier> _compositeMembers;
  
  private boolean _singleton;
  private Assembly _singletonInstance;
  
  private Class[] _singletons;
  private LinkedList<PropertySpecifier> _members;
  private HashMap<String,PropertySpecifier> _memberMap;
  private boolean _resolved;
  private String _id;
  private String _constructorText;

  /**
   * Construct a new AssemblyClass from a definition
   *
   *@param sourceUri The URI of the resource which defines this AssemblyClass,
   *   or null of the AssemblyClass is being defined programmatically.
   *
   *@param basePackage The package which contains the base AssemblyClass or 
   *   Java class from which this AssemblyClass is derived
   *
   *@param baseName The name of the Java class or AssemblyClass resource within
   *   the basePackage
   *
   *@param outerClass The AssemblyClass in which this AssemblyClass is being
   *   defined, if any.
   *
   *@param loader The AssemblyLoader which loaded this AssemblyClass, if any
   */
  public AssemblyClass
    (URI sourceUri
    ,URI basePackage
    ,String baseName
    ,AssemblyClass outerClass
    ,AssemblyLoader loader
    )
  { 
    _sourceUri=sourceUri;
    _basePackage=basePackage;
    _baseName=baseName;
    _outerClass=outerClass;
    if (loader!=null)
    { _loader=loader;
    }
    else
    { _loader=AssemblyLoader.getInstance();
    }
  }

  public String toString()
  { return super.toString()+":"+_sourceUri+":"+_basePackage+":"+_baseName;
  }

  public void setConstructor(String constructor)
  { _constructorText=constructor;
  }
  
  public String getConstructor()
  {
    if (_constructorText!=null)
    { return _constructorText;
    }
    else if (_baseAssemblyClass!=null)
    { return _baseAssemblyClass.getConstructor();
    }
    else
    { return null;
    }
  }
  
  /**
   * Construct a new AssemblyClass from a referenced base class
   *
   *@param sourceUri The URI of the resource which defines this AssemblyClass,
   *   or null of the AssemblyClass is being defined programmatically.
   *
   *@param baseClass The base class 
   *
   *@param outerClass The AssemblyClass in which this AssemblyClass is being
   *   defined, if any.
   *
   *@param loader The AssemblyLoader which loaded this AssemblyClass, if any
   */
  public AssemblyClass
    (URI sourceUri
    ,AssemblyClass baseClass
    ,AssemblyClass outerClass
    ,AssemblyLoader loader
    )
  { 
    _sourceUri=sourceUri;
    _basePackage=null;
    _baseName=null;
    _baseAssemblyClass=baseClass;
    _outerClass=outerClass;
    if (loader!=null)
    { _loader=loader;
    }
    else
    { _loader=AssemblyLoader.getInstance();
    }
  }

  /**
   * Create a new inner AssemblyClass in this AssemblyClass which subclasses 
   *   the specified baseClass.
   */
  AssemblyClass innerSubclass(AssemblyClass baseClass)
  { return new AssemblyClass(_sourceUri,baseClass,this,_loader);
  }



  /**
   * Register a PropertySpecifier as member of this assembly class.
   * 
   * If a PropertySpecifier has already been registered with the specified
   *   name, replace it with the new one.
   */
  void registerMember(String name,PropertySpecifier prop)
    throws BuildException
  { 
    assertUnresolved();
    
    if (_members==null)
    { 
      _members=new LinkedList<PropertySpecifier>();
      _memberMap=new HashMap<String,PropertySpecifier>();
    }
    
    PropertySpecifier oldProp=_memberMap.get(name);
    if (oldProp!=null)
    { 
      // Property has been set multiple times in the same definition
      _memberMap.put(name,prop);
      _members.set(_members.indexOf(oldProp),prop);
    }
    else
    {
      _memberMap.put(name,prop);
      _members.add(prop);
    }

    if (_compositeMembers!=null)
    { 
      System.err.println("Recomposing "+prop);
      // XXX Should override more intelligently,
      //   if property exists instead of double
      //   setting.
      _compositeMembers.add(prop);
    }
  }

  /**
   * Return the specified member of this AssemblyClass or its inheritance
   *   chain.
   */
  PropertySpecifier getMember(String name)
  { 
    PropertySpecifier member=null;
    if (_memberMap!=null)
    { member=_memberMap.get(name);
    }
    
    if (member==null && _baseAssemblyClass!=null)
    { member=_baseAssemblyClass.getMember(name);
    }
    
    return member;
  }

  PropertyBinding[] bindProperties(Assembly container)
    throws BuildException
  { 
    if (_compositeMembers!=null)
    {
      PropertyBinding[] bindings
        =new PropertyBinding[_compositeMembers.size()];
  
      
      int i=0;
      for (PropertySpecifier prop: _compositeMembers)
      { bindings[i++]=new PropertyBinding(prop,container);
      }
      return bindings;
    }
    return null;
  }
  
  /**
   * Return the AssemblyClass, if any, in which this class was defined
   */
  public AssemblyClass getDefiningClass()
  { return _outerClass;
  }
  
  /**
   * Return the path from the root of the declaration unit.
   */
  public String[] getInnerPath()
  { 
    if (_outerClass!=null)
    { return (String[]) ArrayUtil.append(_outerClass.getInnerPath(),_declarationName);
    }
    else
    { return new String[] {_declarationName};
    }
  }

  public synchronized void resolve()
    throws BuildException
  { 
    assertUnresolved();
    if (_baseAssemblyClass==null)
    { resolveExternalBaseClass();
    }
    resolveProperties();
    _resolved=true;
  }
  
  private void assertUnresolved()
  {
    if (_resolved)
    { 
      throw new IllegalStateException
        ("AssemblyClass has already been resolved and cannot be modified");
    }
  }

  public boolean isFocusNamed(String name)
  { 
    if (_id!=null && _id.equals(name))
    { return true;
    }
    else if (_baseAssemblyClass!=null)
    { return _baseAssemblyClass.isFocusNamed(name);
    }
    else if (_javaClass!=null)
    { 
      name=name.intern();
      Class[] singletons=getSingletons();
      for (Class clazz:singletons)
      { return clazz.getName()==name;
      }
    }
    return false;
  }

  public void setDeclarationName(String val)
  { 
    assertUnresolved();
    _declarationName=val;
  }

  public void setId(String val)
  { 
    assertUnresolved();
    _id=val;
  }

  /**
   * Whether this AssemblyClass should generate a global shared instance
   */
  public void setSingleton(boolean val)
  { _singleton=val;
  }
  
  /**
   *@return Whether this AssemblyClass will generate a global shared instance
   */
  public boolean isSingleton()
  { return _singleton;
  }
  
  /**
   *@return Whether this AssemblyClass references a superclass but does not
   *  modify the instance in any way.
   */
  public boolean isUnmodifiedSubclass()
  { 
    return _baseAssemblyClass!=null 
      && _propertySpecifiers==null;
  }
  
  private void resolveProperties()
    throws BuildException
  {
    if (_propertySpecifiers!=null)
    {
      while (_propertySpecifiers.size()>0)
      { 
        PropertySpecifier prop
          =(PropertySpecifier) _propertySpecifiers.removeFirst();
        prop.resolve();
      }
    }
    
    _compositeMembers=new LinkedList<PropertySpecifier>();
    composeMembers(_compositeMembers,new HashMap<String,PropertySpecifier>());
    
  }

  /**
   * Compose the list of members, overriding members with the same target name
   */
  private void composeMembers
    (LinkedList<PropertySpecifier> list
    ,HashMap<String,PropertySpecifier> map
    )
    throws BuildException
  {
    if (_baseAssemblyClass!=null)
    { _baseAssemblyClass.composeMembers(list,map);
    }
    if (_members!=null)
    { 
      for (PropertySpecifier prop:_members)
      {
        PropertySpecifier oldProp
          =map.get(prop.getTargetName());
        if (oldProp!=null)
        { 
          // Replace without reordering
          prop.setBaseMember(oldProp);
          list.set(list.indexOf(oldProp),prop);
          map.put(prop.getTargetName(),prop);
        }
        else
        {
          list.add(prop);
          map.put(prop.getTargetName(),prop);
        }
      }
    }
  }

  /**
   * The URI of the defining resource for this assembly class
   */
  public URI getSourceURI()
  { return _sourceUri;
  }

  /**
   * Resolve the external base class for this AssemblyClass. Called during
   *   resolution when the assembly hasn't been constructed with an existing
   *   base class.
   */
  private void resolveExternalBaseClass()
    throws BuildException
  {
    URI baseResource
      =_basePackage.resolve(_baseName+".assembly.xml");

    URI baseUri
      =_basePackage.resolve(_baseName);
      
    if (baseResource.equals(_sourceUri) && _outerClass==null)
    { 
      // Circular definition
      // Use Java class instead
      resolveJavaClass();
    }
    else
    {
      _baseAssemblyClass=_loader.findAssemblyDefinition(baseResource);
      if (_baseAssemblyClass==null)
      { resolveJavaClass();
      }
    }
  }

  /**
   * Resolve the Java class- called when the top of the AssemblyClass tree is
   *   reached during resolveExternalBaseClass
   *
   * XXX Use the whole URI to load classes from the network, and shortcut
   *     the java: scheme to work from the classpath.
   */
  private void resolveJavaClass()
    throws BuildException
  { 
    String className
      =_basePackage.getPath().substring(1).replace('/','.')+_baseName;
    try
    {
      _javaClass
        =Class.forName
          (className
          ,false
          ,Thread.currentThread().getContextClassLoader()
          );
    }
    catch (ClassNotFoundException x)
    { 
      String langClassName="java.lang."+_baseName;
      try
      {
        _javaClass
          =Class.forName
            (langClassName
            ,false
            ,Thread.currentThread().getContextClassLoader()
            );
      }
      catch (ClassNotFoundException y)
      { throwBuildException("Class not found: '"+className+"'",x);
      }
    }
  }

  /**
   * Throw a build exception and add location information
   */
  private void throwBuildException(String message,Exception cause)
    throws BuildException
  { 
    if (_sourceUri!=null)
    { throw new BuildException(message+" ("+_sourceUri.toString()+")",cause);
    }
    else
    { throw new BuildException(message,cause);
    }
  }

  /**
   * Add a PropertySpecifier which defines the value of a
   *   property of one of the objects in the assembly
   */
  public void addPropertySpecifier(PropertySpecifier prop)
  { 
    assertUnresolved();
    if (_propertySpecifiers==null)
    { _propertySpecifiers=new LinkedList();
    }
    _propertySpecifiers.add(prop);
  }

  public Class getJavaClass()
  {
    if (_javaClass!=null)
    { return _javaClass;
    }
    else if (_baseAssemblyClass!=null)
    { return _baseAssemblyClass.getJavaClass();
    }
    else
    { return null;
    }
    
  }

  public AssemblyClass getBaseClass()
  { return _baseAssemblyClass;
  }

  public List getPropertySpecifiers()
  { return _propertySpecifiers;
  }

  public Class[] getSingletons()
  { 
    if (_baseAssemblyClass!=null)
    { return _baseAssemblyClass.getSingletons();
    }
    else
    {
      if (_singletons==null)
      { resolveSingletons();
      }
      return _singletons;
    }
  }

  /**
   * Create the list of interfaces and classes exposed by the
   *   Java class that defines this Assembly.
   */
  private void resolveSingletons()
  {

    LinkedList singletons=new LinkedList();
    Class javaClass=_javaClass;
    Class[] interfaces=javaClass.getInterfaces();
    
    while (javaClass!=null && javaClass!=Object.class)
    { 
      singletons.add(javaClass);
      javaClass=javaClass.getSuperclass();
    }
    
    for (Class clazz: interfaces)
    { 
      while (clazz!=null)
      { 
        singletons.add(clazz);
        clazz=clazz.getSuperclass();
      }
      
    }
    _singletons=new Class[singletons.size()];
    singletons.toArray(_singletons);
  }
  
  /**
   * Ensure that the given test class is defined within the scope of -this-
   *   assembly class, as opposed to its base class, for the purpose of
   *   overriding a property of some nested class of the base class.
   *
   * If it isn't, define an inner subclass under the specified property name
   *   that extends the nested class within the base class.
   *
   * This is part of the algorithm for determining the AssemblyClass associated
   *   with the dot-separated path in a property specifier.
   */
  AssemblyClass ensureLocalClass(String propertyName,AssemblyClass testClass)
    throws BuildException
  {
    if (testClass.getDefiningClass()!=this)
    {
      AssemblyClass localClass=innerSubclass(testClass);
      PropertySpecifier overrideSpecifier
        =new PropertySpecifier(this,propertyName,localClass);
      registerMember(propertyName,overrideSpecifier);
      addPropertySpecifier(overrideSpecifier);
      return localClass;
    }
    else
    { return testClass;
    }
  }

  /**
   * Create an unbound assembly (if not a singleton), for later binding
   */
  Assembly newInstance()
    throws BuildException
  {
    if (_singleton)
    { 
      synchronized (this)
      {
        if (_singletonInstance!=null)
        { return _singletonInstance;
        }
        else
        { 
          _singletonInstance=new Assembly(this);
          _singletonInstance.bind((Assembly) null);
          return _singletonInstance;
        }
      }
    }
    else if (isUnmodifiedSubclass() && _baseAssemblyClass.isSingleton())
    { 
      // Reference to a singleton
      return _baseAssemblyClass.newInstance();
    }
    else
    { return new Assembly(this);
    }
  }
  
  public Assembly newInstance(Assembly parent)
    throws BuildException
  { 
    Assembly assembly=newInstance();
    if (!assembly.isBound())
    { assembly.bind(parent);
    }
    if (!assembly.isResolved())
    { assembly.resolve();
    }
    if (!assembly.isApplied())
    { assembly.applyProperties();
    }
    return assembly;    
  }
}
