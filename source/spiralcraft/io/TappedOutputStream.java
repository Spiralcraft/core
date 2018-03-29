//
// Copyright (c) 2012 Michael Toth
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
import java.io.OutputStream;

/**
 * <p>An OutputStream that sends a copy of its output to a secondary "tap"
 *   stream
 * </p>
 */
public class TappedOutputStream
  extends OutputStream
{

  private final OutputStream out;
  private final OutputStream tap;
  private final boolean closeTap;
  
  public TappedOutputStream(OutputStream out,OutputStream tap)
  { 
    this.out=out;
    this.tap=tap;
    closeTap=true;
  }
  
  @Override
  public void write(int val)
    throws IOException
  {
    out.write(val);
    tap.write(val);
  }
  
  @Override
  public void write(byte[] bytes)
    throws IOException
  { 
    out.write(bytes);
    tap.write(bytes);
    
  }
  
  @Override
  public void write(byte[] bytes,int start,int len)
    throws IOException
  { 
    out.write(bytes,start,len);
    tap.write(bytes,start,len);
    
  }  
  
  @Override
  public void close()
    throws IOException
  {
    out.close();
    if (closeTap)
    { tap.close();
    }
  }
}
