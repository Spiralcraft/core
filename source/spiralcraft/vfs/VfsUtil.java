package spiralcraft.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class VfsUtil
{

  /**
   * <p>Return the contents of the URI as a String using an ASCII character
   *    encoding.
   * </p>
   * 
   * @param uri
   * @return
   * @throws IOException
   */
  public static String fetchAsciiText(String uri)
    throws IOException
  {
    Resource resource=Resolver.getInstance().resolve(URI.create(uri));
    InputStream in=resource.getInputStream();
    try
    { return StreamUtil.readAsciiString(in, -1);
    }
    finally
    { 
      if (in!=null)
      { in.close();
      }
    }
  }
  

}

