package spiralcraft.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StreamUtil
{
  public static final int DEFAULT_BUFFER_SIZE=65536;

  /**
   * Copy an InputStream to an OutputStream using a buffer of the specified size.
   */
  public static long copyRaw(InputStream in,OutputStream out,int bufferSize)
    throws IOException
  {
    byte[] buffer=new byte[bufferSize];
    long count=0;
    for (;;)
    { 
      int read=in.read(buffer,0,buffer.length);
      if (read<0)
      { break;
      }
      out.write(buffer,0,read);
      count+=read;
    }
    return count;
    
  }

  public static byte[] readBytes(InputStream in)
    throws IOException
  {
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    copyRaw(in,out,DEFAULT_BUFFER_SIZE);
    return out.toByteArray();
  }

  /**
   * Discard [bytes] bytes of the input stream
   */
  public static long discard(InputStream in,long bytes)
    throws IOException
  { 
    long count=0;
    while (count<bytes)
    {
      long ret=in.skip(bytes);
      if (ret==-1)
      { break;
      }
      else
      { count+=ret;
      }
    }
    return count;
  }


}
