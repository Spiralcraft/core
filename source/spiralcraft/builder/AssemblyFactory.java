package spiralcraft.builder;

import java.net.URI;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an assembly from an XML resource
 */
public class AssemblyFactory
{

  /** 
   * Instantiate an assembly defined by the XML document obtained
   *   from the specified resource.
   */
  public static Assembly createAssembly(URI resourceUri)
    throws IOException
  {
    Resource resource=Resolver.getInstance().resolve(resourceUri);
   
    return null;
  }

}
