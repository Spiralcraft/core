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
package spiralcraft.vfs;

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
