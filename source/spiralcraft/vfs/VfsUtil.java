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
  
  /**
   * Convert a glob expression to
   *   a regexp pattern.
   */
  public static Pattern globToPattern(String orig)
    throws PatternSyntaxException
  {
    StringBuffer out=new StringBuffer();
    for (int i=0;i<orig.length();i++)
    { 
      char chr=orig.charAt(i);
      switch (chr)
      {
      case '?':
        out.append('.');
        break;
      case '*':
        out.append(".*");
        break;
      case '.':
        out.append("\\.");
        break;
      default:
        out.append(chr);
      }
    }
    return Pattern.compile(out.toString());
  }  
}

