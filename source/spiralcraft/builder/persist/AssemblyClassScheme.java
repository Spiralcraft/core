package spiralcraft.builder.persist;

import java.net.URI;

import spiralcraft.tuple.spi.ReflectionScheme;
import spiralcraft.tuple.spi.SchemeImpl;
import spiralcraft.tuple.spi.FieldListImpl;

import spiralcraft.builder.AssemblyClass;

/**
 * A Scheme derived from an AssemblyClass definition.
 *
 * This Scheme implementation wraps a ReflectionScheme, and decorates the
 *   associated Fields with information specified in the AssemblyClass
 */
public class AssemblyClassScheme
  extends SchemeImpl
{
  private final ReflectionScheme reflectionScheme;
  
  public AssemblyClassScheme(URI uri,AssemblyClass assemblyClass)
  {
    setURI(uri);
    if (assemblyClass==null)
    { throw new IllegalArgumentException("assemblyClass cannot be null");
    }
    Class javaClass=assemblyClass.getJavaClass();
    reflectionScheme=ReflectionScheme.getInstance(javaClass);
    setFields(new FieldListImpl(reflectionScheme.getFields()));
  }
  
  public String toString()
  { return super.toString()+":"+reflectionScheme.toString();
  }
}