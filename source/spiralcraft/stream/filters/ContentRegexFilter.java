package spiralcraft.stream.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import spiralcraft.stream.ResourceFilter;
import spiralcraft.stream.Resource;
import spiralcraft.stream.StreamUtil;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.channels.FileChannel;

import java.nio.charset.Charset;

import java.lang.CharSequence;

/**
 * A ResourceFilter which accepts resources with content 
 *   which contains the specified pattern.
 */
public class ContentRegexFilter
  implements ResourceFilter
{
  private Pattern _pattern;

  public ContentRegexFilter(String expression)
    throws PatternSyntaxException
  { _pattern=Pattern.compile(expression);
  }

  public boolean accept(Resource resource)
  {
    InputStream in=null;
    try
    {
      in=resource.getInputStream();
      CharSequence charSequence=null;
  
      if (in instanceof FileInputStream)
      { 
        FileChannel channel=((FileInputStream) in).getChannel();
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
        charSequence=Charset.forName("8859_1").newDecoder().decode(bbuf);
      }
      else
      {
        byte[] bytes=StreamUtil.readBytes(in);
        ByteBuffer bbuf = ByteBuffer.allocate(bytes.length).put(bytes);
        charSequence=Charset.forName("8859_1").newDecoder().decode(bbuf);
      }
  
      return _pattern.matcher(charSequence).find();
    }
    catch (IOException x)
    { System.err.println(x.toString());
    }
    finally
    {
      if (in!=null)
      { 
        try
        { in.close();
        }
        catch (IOException x)
        { }
      }
    }
    return false;
  }

}

