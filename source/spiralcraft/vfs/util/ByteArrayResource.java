//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.vfs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import java.net.URI;

import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.spi.AbstractResource;

public class ByteArrayResource
  extends AbstractResource
{
  private static int NEXT_ID=0;

  public static ByteArrayResource copyOf(InputStream inputStream) 
    throws IOException
  {
    try
    { return new ByteArrayResource(StreamUtil.readBytes(inputStream));
    }
    finally
    { inputStream.close();
    }
  }
  
  private byte[] _bytes;
  private long lastModified;
  
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
  
  @Override
  public InputStream getInputStream()
  { return new ByteArrayInputStream(_bytes);
  }
  
  @Override
  public OutputStream getOutputStream()
  {
    return new ByteArrayOutputStream()
    {
      @Override
      public void flush()
        throws IOException
      { 
        super.flush();
        ByteArrayResource.this._bytes=toByteArray();
      }
    };
  }
  
  @Override
  public boolean supportsRead()
  { return true;
  }
  
  @Override
  public boolean supportsWrite()
  { return true;
  }
  
  @Override
  public boolean exists()
  { return _bytes!=null;
  }
  
  @Override
  public long getSize()
  { return _bytes.length;
  }
  
  @Override
  public void renameTo(URI name)
  { throw new UnsupportedOperationException("A ByteArray cannot be renamed");
  }

  @Override
  public void delete()
  { _bytes=null;
  }

  public byte[] getBackingStore()
  { return _bytes;
  
  }

  public long getLastModified()
  { return lastModified;
  }
  
  public boolean setLastModified(long lastModified)
  { 
    this.lastModified=lastModified;
    return true;
  }
}
