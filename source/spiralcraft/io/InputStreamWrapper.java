//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * General purpose InputStream wrapper
 * 
 * @author mike
 *
 */
public class InputStreamWrapper
  extends InputStream
{
  
  private final InputStream delegate;

  public InputStreamWrapper(InputStream delegate)
  { this.delegate=delegate;
  }
  
  @Override
  public int read()
    throws IOException
  { return delegate.read();
  }
  
  @Override
  public int read(byte[] b)
    throws IOException
  { return delegate.read(b);
  }
  
  @Override
  public int read(byte[] b,int start, int len)
    throws IOException
  { return delegate.read(b,start,len);
  }

  @Override
  public void close()
    throws IOException
  { delegate.close();
  }
}
