package spiralcraft.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that delegates to an implementation that is flexibly
 *   determined by the subclass.
 * 
 * @author mike
 *
 */
public abstract class InputStreamReference
  extends InputStream
{

  protected abstract InputStream get();
  
  @Override
  public int read()
    throws IOException
  { return get().read();
  }

  @Override
  public int read(byte[] b)
    throws IOException
  { return get().read(b);
  }

  @Override
  public int read(byte[] b,int start,int len)
    throws IOException
  { return get().read(b,start, len);
  }
  
  @Override
  public int available()
    throws IOException
  { return get().available();
  }

  @Override
  public boolean markSupported()
  { return get().markSupported();
  }

  @Override
  public void close()
    throws IOException
  { get().close();
  }

  @Override
  public void mark(int readLimit)
  { get().mark(readLimit);
  }

  @Override
  public void reset()
    throws IOException
  { get().reset();
  }

  @Override
  public long skip(long len)
    throws IOException
  { return get().skip(len);
  }
}
