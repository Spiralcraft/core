package spiralcraft.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VfsUtil
{

  
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

