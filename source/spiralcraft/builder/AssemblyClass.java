package spiralcraft.builder;

import java.net.URI;

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
  private final AssemblyClass _baseClass;
  private final Class _javaClass;
  private final URI _uri;

  public AssemblyClass(URI uri,AssemblyClass baseClass)
  { 
    _uri=uri;
    _baseClass=baseClass;
    _javaClass=null;
  }

  public AssemblyClass(URI uri,Class javaClass)
  { 
    _uri=uri;
    _baseClass=null;
    _javaClass=javaClass;
  }

  public String toString()
  {
    return super.toString()+"[uri="+_uri+",javaClass="+_javaClass+",baseClass="+_baseClass+"]";
  }
}
