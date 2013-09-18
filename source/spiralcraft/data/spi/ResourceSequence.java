package spiralcraft.data.spi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.Sequence;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.UnresolvableURIException;

/**
 * A sequence which stores data in a Resource.
 * 
 * @author mike
 *
 */
public class ResourceSequence
  implements Sequence,Lifecycle
{

  private static final Charset ASCII=Charset.forName("ASCII");
  
  private int increment;
  private volatile long next;
  private volatile long stop;
  private volatile boolean allocated;
  private Resource resource;
  
  private URI resourceURI;

  public ResourceSequence (URI resourceURI)
  { 
    this.resourceURI=resourceURI;
  }

  @Override
  public void start()
  throws LifecycleException
  { 
    try
    { resource=Resolver.getInstance().resolve(resourceURI);
    }
    catch (UnresolvableURIException x)
    { throw new LifecycleException("Could not resolve ResourceSequence URI",x);
    }
    if (resource==null)
    { throw new LifecycleException("Could not resolve "+resourceURI);
    }
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
      if (out instanceof FileOutputStream)
      { ((FileOutputStream) out).getFD().sync();
      }
    }
    finally
    { out.close();
    }
  }
  
  private synchronized void deallocate()
    throws DataException
  {
    
    try
    { 
      if (allocated)
      { write(Long.toString(next));
      }
    }
    catch (IOException x)
    { throw new DataException("Error deallocating sequence",x);
    }
  }

  private void allocate()
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
      allocated=true;
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

