package spiralcraft.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class StreamUtil
{

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

}
