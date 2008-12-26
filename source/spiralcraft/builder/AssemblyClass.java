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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;


import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;

import spiralcraft.vfs.classpath.ClasspathResourceFactory;

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
  
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);
  
  private static final ClassLog log
    =ClassLog.getInstance(AssemblyClass.class);

  
  /**
   * <p>
   * Whether the specified class will be managed by a sub-assembly, or whether
   *   it represents a primitive value
   * </p>
   * @param clazz
   * @return
   */
  public static final boolean isManaged(Class<?> clazz)
  {
    return !clazz.isPrimitive()
     && !(clazz.isArray() && !isManaged(clazz.getComponentType()))
     && !(clazz==String.class)
     && ClassUtil.primitiveEquivalent(clazz)==null
     ;
  }
  
  
  
  
  private final URI sourceURI;
  

  // _basePackage and _baseName are used
  //   to resolve external base class or java class
  //   if _baseAssemblyClass hasn't been supplied at construction
  private final URI _basePackage;
  private final String _baseName;
  
  // The URI used to reference this AssemblyClass in the referencing container
  private final URI _containerURI;
  private String _id;  
  
  private AssemblyClass _baseAssemblyClass;

  private final AssemblyClass _outerClass;
  private final AssemblyLoader _loader;
  private String _declarationName;
  private Class<?> _javaClass;
  private LinkedList<PropertySpecifier> _propertySpecifiers;
  
  // The rolled-up list of PropertySpecifiers which apply to the specific
  //   Bean that is the root of this AssemblyClass and its base classes
  private LinkedList<PropertySpecifier> _compositeMembers;
  
  private boolean _singleton;
  private Assembly<?> _singletonInstance;
  
  private Class<?>[] _singletons;
  private LinkedList<PropertySpecifier> _localMembers;
  private HashMap<String,PropertySpecifier> _memberMap;
  private boolean _resolved;
  private String _constructorText;
  private PropertySpecifier _containingProperty;
  private boolean _resolving;
  private MappedBeanInfo beanInfo;

  
  private boolean debug;
  
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
   *
   */
  public AssemblyClass
    (URI sourceUri
    ,URI basePackage
    ,String baseName
    ,AssemblyClass outerClass
    ,AssemblyLoader loader
    )
  { 
    this.sourceURI=sourceUri;
    _basePackage=basePackage;
    _baseName=baseName;
    _containerURI=_basePackage.resolve(baseName);
    _outerClass=outerClass;
    if (loader!=null)
    { _loader=loader;
    }
    else
    { _loader=AssemblyLoader.getInstance();
    }
  }

  @Override
  public String toString()
  { return super.toString()+":"+sourceURI+":"+_containerURI;
  }

  public URI getContainerURI()
  { return _containerURI;
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
    this.sourceURI=sourceUri;
    _basePackage=null;
    _baseName=null;
    _containerURI=baseClass.getContainerURI();
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
  { return new AssemblyClass(sourceURI,baseClass,this,_loader);
  }

  public void setId(String id)
  { this._id=id;
  }
  
  public String getId()
  { return _id;
  }
  

  /**
   * <p>Register a PropertySpecifier as member of this assembly class, when
   *   sorting out the set of PropertySpecifiers collected in the definition
   *   of the root element of a given AssemblyClass. 
   * </p>
   * 
   * If a PropertySpecifier has already been registered with the specified
   *   name, replace it with the new one.
   */
  void registerMember(String name,PropertySpecifier prop)
    throws BuildException
  { 
    // assertUnresolved();
    
    if (_localMembers==null)
    { 
      _localMembers=new LinkedList<PropertySpecifier>();
      _memberMap=new HashMap<String,PropertySpecifier>();
    }
    
    PropertySpecifier oldProp=_memberMap.get(name);
    if (oldProp!=null)
    { 
      // Property has been set multiple times in the same definition
      _memberMap.put(name,prop);
      _localMembers.set(_localMembers.indexOf(oldProp),prop);
    }
    else
    {
      _memberMap.put(name,prop);
      _localMembers.add(prop);
    }

    if (_compositeMembers!=null)
    { 
      if (debug)
      { log.fine("Recomposing "+prop);
      }

      boolean found=false;
      for (int i=0;i<_compositeMembers.size();i++)
      {
        PropertySpecifier baseMember=_compositeMembers.get(i);
        if (baseMember.getTargetName().equals(name))
        {
          _compositeMembers.set(i,prop);
          prop.setBaseMember(baseMember);
          found=true;
          break;
        }
      }
      if (!found)
      { 
        prop.setTargetSequence(_compositeMembers.size());
        _compositeMembers.add(prop);
      }
    }
  }

  /**
   * <p>Return an Iterable of the members of this AssemblyClass. The members
   *   include declared PropertySpecifiers for the root Bean of this
   *   AssemblyClass and its base classes. 
   * </p>
   */
  public Iterable<PropertySpecifier> memberIterable()
  { return _compositeMembers;
  }
  
  /**
   * <p>Return an Iterable of the local members of this AssemblyClass.
   *  The local members include declared PropertySpecifiers for the root Bean 
   *   of this AssemblyClass only, excluding members of base classes. 
   *   Bean properties not explicitly declared in the AssemblyClass definition
   *   are not included.
   * </p>
   */
  public Iterable<PropertySpecifier> localMemberIterable()
  { return _localMembers;
  }
  
  /**
   * Return the specified member of this AssemblyClass or its inheritance
   *   chain.
   */
  PropertySpecifier getMember(String name,boolean discover)
    throws BuildException
  { 
    PropertySpecifier member=null;
    if (_memberMap!=null)
    { member=_memberMap.get(name);
    }
    
    if (member==null && _baseAssemblyClass!=null)
    { member=_baseAssemblyClass.getMember(name,discover);
    }
       
    if (member==null && _javaClass!=null)
    {
      // Set up a default association
    
      PropertyDescriptor descriptor=beanInfo.findProperty(name);
      if (descriptor!=null)
      { member=addMemberForProperty(descriptor,discover);
      }

    }
    
    if (member==null)
    { log.fine("Member "+name+" not found- javaClass="+_javaClass);
    }
    return member;
  }
  
  public PropertySpecifier getMember(String name)
    throws BuildException
  { return getMember(name,false);
  }
  
  PropertySpecifier discoverMember(String name)
    throws BuildException
  { return getMember(name,true);
  }
  

  PropertyDescriptor getPropertyDescriptor(String name)
  { 
    if (beanInfo!=null)
    { return beanInfo.findProperty(name);
    }
    else if (_baseAssemblyClass!=null)
    { return _baseAssemblyClass.getPropertyDescriptor(name);
    }
    return null;
  }
  
  /**
   * <p>Add a member to handle the specified Bean PropertyDescriptor.
   * </p>
   * 
   * <p>This method assumes that a member with the same
   *   name has been checked and found not to exist
   * </p>
   * 
   * @param descriptor
   * @param addSubAssembly
   * @return
   * @throws BuildException
   */
  private PropertySpecifier addMemberForProperty
    (PropertyDescriptor descriptor,boolean addSubAssembly)
    throws BuildException
  { 
    String name=descriptor.getName();
    PropertySpecifier member=new PropertySpecifier(this,name);
    member.setPropertyDescriptor(descriptor);
    
    Class<?> propertyType=descriptor.getPropertyType();
    if (addSubAssembly)
    {
      // XXX Factor addSubAssembly into appropriate intelligence
      
      // Create a new sub-assembly to provide a context to accept configuration
      //   data in the non-collection case.
      if (!propertyType.isArray()
          && 
          !Collection.class.isAssignableFrom(propertyType)
          &&
          isManaged(propertyType)
         )
      { 
        AssemblyClass sourceBaseClass
          =_loader.findAssemblyClass(propertyType);

        member.addAssemblyClass
          (innerSubclass(sourceBaseClass)
          );
      }
    }
    else
    { // log.fine("Adding empty member "+member+" to "+toString());
    }
    
    if (_resolving || _resolved)
    { member.resolve();
    }
    //_localMembers.add(member);
    // _memberMap.put(name,member);
    if (_compositeMembers!=null)
    { 
      member.setTargetSequence(_compositeMembers.size());
      _compositeMembers.add(member);
    }
    return member;
  }
  
  public PropertySpecifier getContainingProperty()
  { return _containingProperty;
  }
  
  boolean isResolved()
  { return _resolved;
  }
  
  void setContainingProperty(PropertySpecifier specifier)
  { 
    assertUnresolved();
    _containingProperty=specifier;
  }
  
  PropertyBinding[] bindProperties(Assembly<?> container)
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

  public String getDeclarationName()
  { return _declarationName;
  }
  
  public synchronized void resolve()
    throws BuildException
  { 
    _resolving=true;
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

  public boolean isFocus(URI uri)
  { 
    if (_outerClass==null)
    { 
      // If the source file URI matches an outer class, that works
      if (sourceURI.relativize(uri)!=uri)
      { return true;
      }
    }
    else
    {
      // For an inner class, we need to match the URI of the reference
      URI containerURI=getContainerURI();
      if (containerURI!=null 
          && containerURI.relativize(uri)!=uri
         )
      { return true;
      }
    }
    
    // Recurse
    if (_baseAssemblyClass!=null) 
    { return _baseAssemblyClass.isFocus(uri);
    }
    return false;
  }

  
  public void setDeclarationName(String val)
  { 
    assertUnresolved();
    _declarationName=val;
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

//
// Removed for now, because when bean properties are created on demand
//   the reference graph is discovered. To discover recursively now
//   would suck in a lot of unused data.
//    
//    if (_baseAssemblyClass==null)
//    { 
//      for (PropertySpecifier prop:getBeanProperties())
//      { prop.resolve();
//      }
//    }

    if (_propertySpecifiers!=null)
    {
      while (_propertySpecifiers.size()>0)
      { 
        PropertySpecifier prop
          =_propertySpecifiers.removeFirst();
        prop.resolve();
      }
    }
    
  
      
    _compositeMembers
      =new LinkedList<PropertySpecifier>();
    composeMembers(_compositeMembers,new HashMap<String,PropertySpecifier>());
  }
  
  /**
   * 
   * @return The list of bean properties 
   * @throws BuildException
   */
  public List<String> getMemberNames()
    throws BuildException
  {
    ArrayList<String> names
      =new ArrayList<String>();
    for (PropertyDescriptor prop:beanInfo.getPropertyDescriptors())
    { names.add(prop.getName());
    }
    return names;
  }
  
//
//  Make autodiscovery a switch
//
//  private LinkedList<PropertySpecifier> getBeanProperties()
//    throws BuildException
//  {
//    if (_baseAssemblyClass==null)
//    {
//      // Add java members
//      LinkedList<PropertySpecifier> props=new LinkedList<PropertySpecifier>();
//      for (PropertyDescriptor prop:beanInfo.getPropertyDescriptors())
//      { 
//        
//        PropertySpecifier specifier
//          =new PropertySpecifier(this,prop);
//        props.add(specifier);;
//      }
//      return props;
//    }
//
//    return null;
//  }
  
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
    
    if (_localMembers!=null)
    { 
      for (PropertySpecifier prop:_localMembers)
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
          prop.setTargetSequence(list.size());
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
  { return sourceURI;
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
      =_basePackage.resolve(_baseName+".assy.xml");

    // URI baseUri
    //  =_basePackage.resolve(_baseName);
      
    if (baseResource.equals(sourceURI) && _outerClass==null)
    { 
      // Circular definition
      // Use Java class instead
      _javaClass=resolveJavaClass();
      if (_javaClass==null)
      { 
        throw new BuildException
          ("Assembly "+sourceURI+" is not contained in a Java package and "
          +" thus cannot be based on a Java class of the same name"
          );
      }
    }
    else
    {
      _baseAssemblyClass=_loader.findAssemblyDefinition(baseResource);
      if (_baseAssemblyClass==null)
      { 
        _javaClass=resolveJavaClass();
        if (_javaClass==null)
        {
          throw new BuildException
            ("Assembly "+baseResource+" is not contained in a Java package and "
            +" thus cannot be automatically derived from a Java class of the same" 
            +" name"
            );
        }
      }
    }
    if (_javaClass!=null)
    { 
      try
      { beanInfo=_BEAN_INFO_CACHE.getBeanInfo(_javaClass);
      }
      catch (IntrospectionException x)
      { throw new BuildException("Error introspecting "+_javaClass,x);
      }
    }
    
  }

  /**
   * Resolve the Java class- called when the top of the AssemblyClass tree is
   *   reached during resolveExternalBaseClass
   *
   */
  private Class<?> resolveJavaClass()
    throws BuildException
  { 
    
    if (ClasspathResourceFactory.isClasspathScheme(_basePackage.getScheme()))
    {
        
      String className
        =_basePackage.getPath().substring(1).replace('/','.')
          +_baseName.replace('-','$');
      try
      { 
        return
          Class.forName
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
          return
            Class.forName
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
    
    String langClassName="java.lang."+_baseName;
    try
    {
      return
        Class.forName
          (langClassName
          ,false
          ,Thread.currentThread().getContextClassLoader()
          );
      
    }
    catch (ClassNotFoundException y)
    { throwBuildException("Class not found: '"+langClassName+"'",y);
    }
      
    return null;
  }

  /**
   * Throw a build exception and add location information
   */
  private void throwBuildException(String message,Exception cause)
    throws BuildException
  { 
    if (sourceURI!=null)
    { throw new BuildException(message+" ("+sourceURI.toString()+")",cause);
    }
    else
    { throw new BuildException(message+" ("+toString()+")",cause);
    }
  }

  /**
   * Add a PropertySpecifier which defines the value of a
   *   property of one of the objects in the assembly. The PropertySpecifier
   *   can refer to a bean property in the root object, or in any nested
   *   object defined in this or any base AssemblyClass definition.
   */
  public void addPropertySpecifier(PropertySpecifier prop)
  { 
    assertUnresolved();
    if (_propertySpecifiers==null)
    { _propertySpecifiers=new LinkedList<PropertySpecifier>();
    }
    _propertySpecifiers.add(prop);
  }

  /**
   *@return the list of Property specifiers declared in this definition
   */
  public List<PropertySpecifier> getPropertySpecifiers()
  { return _propertySpecifiers;
  }

  public Class<?> getJavaClass()
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


  public Class<?>[] getSingletons()
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

    LinkedList<Class<?>> singletons=new LinkedList<Class<?>>();
    Class<?> javaClass=_javaClass;
    Class<?>[] interfaces=javaClass.getInterfaces();
    
    while (javaClass!=null && javaClass!=Object.class)
    { 
      singletons.add(javaClass);
      javaClass=javaClass.getSuperclass();
    }
    
    for (Class<?> clazz: interfaces)
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
  AssemblyClass ensureLocalClass
    (String propertyName
    ,AssemblyClass testClass
    )
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
  @SuppressWarnings("unchecked") // Reflected type at Runtime
  Assembly<?> newInstance(boolean factoryMode)
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
          _singletonInstance=new Assembly(this,factoryMode);
          _singletonInstance.bind(null);
          // Note: resolve() is re-entrant, so it will be late-called
          
          return _singletonInstance;
        }
      }
    }
    else if (isUnmodifiedSubclass() && _baseAssemblyClass.isSingleton())
    { 
      // Reference to a singleton
      return _baseAssemblyClass.newInstance(factoryMode);
    }
    else
    { return new Assembly(this,factoryMode);
    }
  }
  
  /**
   * Create a new instance of this AssemblyClass in the context of the
   *   optional specified parent Focus.
   */
  public Assembly<?> newInstance(Focus<?> parentFocus)
    throws BuildException
  { 
    Assembly<?> assembly=newInstance(false);
    if (!assembly.isBound())
    { assembly.bind(parentFocus);
    }
    if (!assembly.isResolved())
    { assembly.resolve();
    }
    return assembly;    
  }
  
  /**
   * <p>Create a new instance of this AssemblyClass in the context of the
   *   optional specified parent Focus. The given Assembly will operate
   *   as a Factory.
   * </p>
   * 
   * <p>When operating as a Factory, the Assembly creates an instance of the
   *   target object accessible by only the current thread (ThreadLocal). The
   *   "resolve" method will create a new Object, and the release() method
   *   will release it.
   *   
   * </p>
   */
  public Assembly<?> newFactoryInstance(Focus<?> parentFocus)
    throws BuildException
  {
    Assembly<?> assembly=newInstance(true);
    
    if (!assembly.isBound())
    { assembly.bind(parentFocus);
    }
    return assembly;    
  }
  
}
