package spiralcraft.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>An InputStream that will terminate after reading a maximum number of
 *   bytes or reaching the end of the stream.
 * </p>
 * 
 * @author mike
 *
 */
public class CappedInputStream
  extends InputStream
{

  private final long cap;
  private final InputStream source;

  private volatile long read;
  
  /**
   * 
   * @param source The source InputStream
   * @param cap The maximum number of bytes to read.
   */
  public CappedInputStream(InputStream source,long cap)
  { 
    this.cap=cap;
    this.source=source;
  }
  
  @Override
  public int read()
    throws IOException
  {
    if (read==cap)
    { return -1;
    }
    
    int ret=source.read();
    read++;
    return ret;
  }

  @Override
  public int read(byte[] bytes)
    throws IOException
  { 
    return read(bytes,0,bytes.length);
  }
  
  @Override
  public int read(byte[] bytes,int start,int len)
    throws IOException
  { 
    if (read==cap)
    { return -1;
    }
    
    len=Long.valueOf(Math.min(cap-read,len)).intValue();
    int actual=source.read(bytes,start,len);
    if (actual>-1)
    { read+=actual;
    }
    return actual;
  }
  
}
