package spiralcraft.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that delegates to an implementation that is flexibly
 *   determined by the subclass.
 * 
 * @author mike
 *
 */
public abstract class OutputStreamReference
  extends OutputStream
{

  protected abstract OutputStream get();
  
  @Override
  public void write(int val)
    throws IOException
  { get().write(val);
  }

  @Override
  public void write(byte[] b)
    throws IOException
  { get().write(b);
  }

  @Override
  public void write(byte[] b,int start,int len)
    throws IOException
  { get().write(b,start,len);
  }

  @Override
  public void close()
    throws IOException
  { get().close();
  }

  @Override
  public void flush()
    throws IOException
  {  get().flush();
  }
}
