package spiralcraft.builder;

import java.net.URI;

import java.io.IOException;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import spiralcraft.util.ArrayUtil;

/**
 * An AssemblyClass defines the behavior and content of an Assembly. One
 *   instance of an AssemblyClass is created within a given ClassLoader
 *   for each assembly definition, which is uniquely named.
 * 
 * An AssemblyClass is associated with one or more instances of type
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
  private LinkedList _propertySpecifiers;
  private LinkedList _compositeMembers;
  private String[] _singletonNames;
  private Class[] _localSingletons;
  private Class[] _singletons;
  private LinkedList _members;
  private HashMap _memberMap;
  private boolean _resolved;

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
      _members=new LinkedList();
      _memberMap=new HashMap();
    }
    else
    {
      PropertySpecifier oldProp=(PropertySpecifier) _memberMap.get(name);
      if (oldProp!=null)
      { 
        
        // Silently replace the old member. The registering PropertySpecifier
        //   should check for an old member before replacing it.
        _members.remove(oldProp);
        _memberMap.remove(name);
      }
    }

    _memberMap.put(name,prop);
    _members.add(prop);
    if (_compositeMembers!=null)
    { 
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
    { member=(PropertySpecifier) _memberMap.get(name);
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
  
      
      Iterator it=_compositeMembers.iterator();
      int i=0;
      while (it.hasNext())
      {
        PropertySpecifier prop=(PropertySpecifier) it.next();
        bindings[i++]=new PropertyBinding(prop,container);
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
    resolveSingletons();
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
    if (_declarationName.equals(name))
    { return true;
    }
    else if (_baseAssemblyClass!=null)
    { return _baseAssemblyClass.isFocusNamed(name);
    }
    else if (_javaClass!=null)
    { return _javaClass.getName().equals(name);
    }
    return false;
  }

  public void setDeclarationName(String val)
  { 
    assertUnresolved();
    _declarationName=val;
  }

  public void setSingletonNames(String[] interfaceNames)
  { 
    assertUnresolved();
    _singletonNames=interfaceNames;
  }

  private void resolveSingletons()
    throws BuildException
  { 
    if (_singletonNames!=null)
    {
      _localSingletons=new Class[_singletonNames.length];
      for (int i=0;i<_singletonNames.length;i++)
      { 
        try
        {
          _localSingletons[i]
            =Class.forName
              (_singletonNames[i]
              ,false
              ,Thread.currentThread().getContextClassLoader()
              );
        }
        catch (ClassNotFoundException x)
        { throw new BuildException("Class not found: "+_singletonNames[i],x);
        }
      }
    }

    LinkedList singletons=new LinkedList();
    composeSingletons(singletons);
    _singletons=new Class[singletons.size()];
    singletons.toArray(_singletons);
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
    
    _compositeMembers=new LinkedList();
    composeMembers(_compositeMembers,new HashMap());
    
  }

  /**
   * Compose the list of members, overriding members with the same target name
   */
  private void composeMembers(LinkedList list,HashMap map)
    throws BuildException
  {
    if (_baseAssemblyClass!=null)
    { _baseAssemblyClass.composeMembers(list,map);
    }
    if (_members!=null)
    { 
      Iterator it=_members.iterator();
      while (it.hasNext())
      { 
        
        PropertySpecifier prop=(PropertySpecifier) it.next();
        PropertySpecifier oldProp
          =(PropertySpecifier) map.get(prop.getTargetName());
        if (oldProp!=null)
        { 
          map.remove(prop.getTargetName());
          list.remove(oldProp);
          prop.setBaseMember(oldProp);
        }
        list.add(prop);
        map.put(prop.getTargetName(),prop);
      }
    }
  }

  public void composeSingletons(LinkedList list)
  { 
    if (_baseAssemblyClass!=null)
    { _baseAssemblyClass.composeSingletons(list);
    }
    if (_localSingletons!=null)
    { 
      for (int i=0;i<_singletons.length;i++)
      { list.add(_localSingletons[i]);
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
    { throwBuildException("Class not found: '"+className+"'",x);
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
  { return _singletons;
  }

  /**
   * Ensure that the given test class is defined within the scope of this
   *   assembly class. If it isn't, define an inner subclass under the 
   *   specified property name.
   *
   * This is part of the algorithm for determining the AssemblyClass associated
   *   with the path in a property specifier.
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
  
  public Assembly newInstance(Assembly parent)
    throws BuildException
  { 
    // Instantiate the assembly
    Assembly assembly=new Assembly(this,parent);
    
    return assembly;
  }
}
