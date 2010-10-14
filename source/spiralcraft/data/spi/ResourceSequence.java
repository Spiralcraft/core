package spiralcraft.data.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.Sequence;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;

/**
 * A sequence which stores data in a Resource.
 * 
 * @author mike
 *
 */
public class ResourceSequence
  implements Sequence
{

  private static final Charset ASCII=Charset.forName("ASCII");
  
  private int increment;
  private volatile long next;
  private volatile long stop;
  private Resource resource;

  public ResourceSequence (Resource resource)
  { 
    this.resource=resource;
  }

  @Override
  public void start()
  throws LifecycleException
  {

  }

  @Override
  public void stop()
  throws LifecycleException
  {
    try
    {
      deallocate();
    }
    catch (DataException x)
    { 
      throw new LifecycleException
        ("Error deallocating sequence "+resource.getURI(),x);
    }
  }

  private String read()
    throws IOException
  {    
    if (!resource.exists())
    { return null;
    }
    InputStream in=resource.getInputStream();
    try
    { return StreamUtil.readAsciiString(in,-1);
    }
    finally
    { in.close();
    }
  }
  
  private void write(String val)
    throws IOException
  {
    OutputStream out=resource.getOutputStream();
    try
    { 
      out.write(val.getBytes(ASCII));
      out.flush();
    }
    finally
    { out.close();
    }
  }
  
  private synchronized void deallocate()
    throws DataException
  {
    
    try
    { write(Long.toString(next));
    }
    catch (IOException x)
    { throw new DataException("Error deallocating sequence",x);
    }
  }

  public void allocate()
  throws DataException
  {
    try
    {
      String val=read();
      if (val==null || val.isEmpty())
      { 
        write("200");
        next=100;
        stop=200;
        increment=100;
      }
      else
      { 
        next=Long.parseLong(val);
        increment=100;
        stop=next+increment;
        write(Long.toString(stop));
      }
    }
    catch (IOException x)
    { throw new DataException("Error allocating sequence",x);
    }
  }



  @Override
  public synchronized Long next()
    throws DataException
  {
    if (next==stop)
    { allocate();
    }
    return next++;
  }
}

