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

  public AssemblyClass
    (URI sourceUri
    ,URI derivationPackage
    ,String derivationName
    ,AssemblyClass outerClass
    )
  { 
    _sourceUri=sourceUri;
    _derivationPackage=derivationPackage;
    _derivationName=derivationName;
    _outerClass=outerClass;
  }

  private void resolveExternalBaseClass()
    throws IOException
  {
    URI baseUri
      =_derivationPackage.resolve(_derivationName+".assembly.xml");

    if (baseUri.equals(_sourceUri) && _outerClass==null)
    { 
      // Circular definition
      // Use Java class instead
      return;
    }

    // Use the AssemblyLoader here
    _baseClass=AssemblyFactory.loadAssemblyDefinition(baseUri);
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

  public String toString()
  {
    StringBuffer out=new StringBuffer();
    out.append(super.toString());
    out.append("[uri="+_sourceUri);
    if (_propertySpecifiers!=null)
    {
      out.append(",properties=[");
      Iterator it=_propertySpecifiers.iterator();
      while (it.hasNext())
      { 
        out.append(it.next().toString());
        if (it.hasNext())
        { out.append(",");
        }
      }
      out.append("]");
    }
    out.append("]");
    return out.toString();
  }
}
