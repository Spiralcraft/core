package spiralcraft.builder;

import java.net.URI;

import java.io.IOException;

import java.util.LinkedList;
import java.util.Iterator;

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

  public void resolve()
    throws IOException,ClassNotFoundException
  { 
    resolveExternalBaseClass();

  }

  private void resolveExternalBaseClass()
    throws IOException,ClassNotFoundException
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
    throws ClassNotFoundException
  { 
    String className
      =_derivationPackage.getPath().substring(1).replace('/','.')+_derivationName;
    _javaClass
      =Class.forName
        (className
        ,false
        ,Thread.currentThread().getContextClassLoader()
        );
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


  public Assembly newInstance()
    throws InstantiationException,ClassNotFoundException,IllegalAccessException
  { 
    // Instantiate the assembly
    Assembly assembly=new Assembly(this);
    return assembly;
  }
}
