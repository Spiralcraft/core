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
  private final URI _derivationPackage;
  private final String _derivationName;
  private final AssemblyClass _outerClass;
  private String _declarationName;
  private AssemblyClass _baseClass;
  private Class _javaClass;
  private LinkedList _propertySpecifiers;
  private LinkedList _compositeMembers;
  private AssemblyLoader _loader;
  private String[] _singletonNames;
  private Class[] _localSingletons;
  private Class[] _singletons;
  private LinkedList _members;
  private HashMap _memberMap;

  public AssemblyClass
    (URI sourceUri
    ,URI derivationPackage
    ,String derivationName
    ,AssemblyClass outerClass
    ,AssemblyLoader loader
    )
  { 
    _sourceUri=sourceUri;
    _derivationPackage=derivationPackage;
    _derivationName=derivationName;
    _outerClass=outerClass;
    _loader=loader;
  }

  void registerMember(String name,PropertySpecifier prop)
  { 
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
        _members.remove(oldProp);
        _memberMap.remove(name);
      }
    }

    _memberMap.put(name,prop);
    _members.add(prop);
    if (_compositeMembers!=null)
    { _compositeMembers.add(prop);
    }
  }

  PropertySpecifier getMember(String name)
  { 
    if (_memberMap!=null)
    { return (PropertySpecifier) _memberMap.get(name);
    }
    else
    { 
      if (_baseClass!=null)
      { return _baseClass.getMember(name);
      }
      else
      { return null;
      }
    }
    
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

  public void resolve()
    throws BuildException
  { 
    resolveExternalBaseClass();
    resolveProperties();
    resolveSingletons();
  }

  public boolean isFocusNamed(String name)
  { 
    if (_declarationName.equals(name))
    { return true;
    }
    else if (_baseClass!=null)
    { return _baseClass.isFocusNamed(name);
    }
    else if (_javaClass!=null)
    { return _javaClass.getName().equals(name);
    }
    return false;
  }

  public void setDeclarationName(String val)
  { _declarationName=val;
  }

  public void setSingletonNames(String[] interfaceNames)
  { _singletonNames=interfaceNames;
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
      Iterator it=_propertySpecifiers.iterator();
      while (it.hasNext())
      { 
        PropertySpecifier prop=(PropertySpecifier) it.next();
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
  {
    if (_baseClass!=null)
    { _baseClass.composeMembers(list,map);
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
        }
        list.add(prop);
        map.put(prop.getTargetName(),prop);
      }
    }
  }

  public void composeSingletons(LinkedList list)
  { 
    if (_baseClass!=null)
    { _baseClass.composeSingletons(list);
    }
    if (_localSingletons!=null)
    { 
      for (int i=0;i<_singletons.length;i++)
      { list.add(_localSingletons[i]);
      }
    }
  }

  public URI getSourceURI()
  { return _sourceUri;
  }

  private void resolveExternalBaseClass()
    throws BuildException
  {
    URI baseUri
      =_derivationPackage.resolve(_derivationName+".assembly.xml");

    if (baseUri.equals(_sourceUri) && _outerClass==null)
    { 
      // Circular definition
      // Use Java class instead
      resolveJavaClass();
    }
    else
    {
      _baseClass=_loader.findAssemblyDefinition(baseUri);
      if (_baseClass==null)
      { resolveJavaClass();
      }
    }
  }

  private void resolveJavaClass()
    throws BuildException
  { 
    String className
      =_derivationPackage.getPath().substring(1).replace('/','.')+_derivationName;
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
  { throw new BuildException(message+" ("+_sourceUri.toString()+")",cause);
  }

  private String qualifyRelativeJavaClassName(String name)
  { return _derivationPackage.getPath().substring(1).replace('/','.')+name;
  }

  public void addPropertySpecifier(PropertySpecifier prop)
  { 
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
    else if (_baseClass!=null)
    { return _baseClass.getJavaClass();
    }
    else
    { return null;
    }
    
  }

  public AssemblyClass getBaseClass()
  { return _baseClass;
  }

  public List getPropertySpecifiers()
  { return _propertySpecifiers;
  }

  public Class[] getSingletons()
  { return _singletons;
  }

  public Assembly newInstance(Assembly parent)
    throws BuildException
  { 
    // Instantiate the assembly
    Assembly assembly=new Assembly(this,parent);
    
    return assembly;
  }
}
