package spiralcraft.text.io;

import java.io.InputStream;
import java.io.IOException;

import java.net.URI;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

/**
 * Represents an File as a CharSequence
 */

//
// XXX Support constructors for non-default Character conversion
// 

public class ResourceCharSequence
  extends InputStreamCharSequence
{
  public ResourceCharSequence(URI resourceURI)
    throws IOException
  { 
    Resource resource=Resolver.getInstance().resolve(resourceURI);
    InputStream in=resource.getInputStream();
    load(in);
    in.close();
  }
  
  
}
