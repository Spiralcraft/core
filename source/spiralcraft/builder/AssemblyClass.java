package spiralcraft.builder;

import java.net.URI;

import java.io.IOException;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

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
  private AssemblyClass _baseClass;
  private Class _javaClass;
  private LinkedList _propertySpecifiers;
  private LinkedList _compositePropertySpecifiers;
  private AssemblyLoader _loader;

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


  PropertyBinding[] bindProperties(Assembly container)
    throws BuildException
  { 
    PropertyBinding[] bindings
      =new PropertyBinding[_compositePropertySpecifiers.size()];

    
    Iterator it=_compositePropertySpecifiers.iterator();
    int i=0;
    while (it.hasNext())
    {
      PropertySpecifier prop=(PropertySpecifier) it.next();
      bindings[i++]=prop.bind(container);
    }
    return bindings;
  }

  public void resolve()
    throws BuildException
  { 
    resolveExternalBaseClass();
    resolveProperties();
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
    
    _compositePropertySpecifiers=new LinkedList();
    composePropertySpecifiers(_compositePropertySpecifiers);

  }

  public void composePropertySpecifiers(LinkedList list)
  { 
    if (_baseClass!=null)
    { _baseClass.composePropertySpecifiers(list);
    }
    if (_propertySpecifiers!=null)
    { list.addAll(_propertySpecifiers);
    }
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
    { throw new BuildException("Class not found: "+className,x);
    }
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

  public Assembly newInstance(Assembly parent)
    throws BuildException
  { 
    // Instantiate the assembly
    Assembly assembly=new Assembly(this,parent);
    
    return assembly;
  }
}
