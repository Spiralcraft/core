package spiralcraft.stream;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.URI;

public class ByteArrayResource
  extends AbstractResource
{
  private static int NEXT_ID=0;
  private byte[] _bytes;
  
  
  public ByteArrayResource(byte[] bytes)
  { 
    super(URI.create("bytes:"+NEXT_ID++));
    _bytes=bytes;
  }
  
  public ByteArrayResource()
  { 
    super(URI.create("bytes:"+NEXT_ID++));
    _bytes=new byte[0];
  }
  
  public InputStream getInputStream()
  { return new ByteArrayInputStream(_bytes);
  }
  
  public boolean supportsRead()
  { return true;
  }
  
  public boolean exists()
  { return _bytes!=null;
  }
}
