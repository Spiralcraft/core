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
package spiralcraft.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A collection of static methods useful for Stream manipulation
 */
public class StreamUtil
{
  public static final int DEFAULT_BUFFER_SIZE=65536;

  /**
   * Copy an InputStream to an OutputStream using a buffer of the specified size.
   */
  public static long copyRaw(InputStream in,OutputStream out,int bufferSize)
    throws IOException
  {
    byte[] buffer=new byte[bufferSize];
    long count=0;
    for (;;)
    { 
      int read=in.read(buffer,0,buffer.length);
      if (read<0)
      { break;
      }
      out.write(buffer,0,read);
      count+=read;
    }
    return count;
    
  }

  /**
   * Copy (len) bytes from an InputStream to an OutputStream 
   *   using a buffer of the specified size.
   */
  public static long copyRaw
    (InputStream in
    ,OutputStream out
    ,int bufferSize
    ,long len
    )
    throws IOException
  { 
    byte[] buffer=new byte[bufferSize];
    long count=0;
    for (;;)
    { 
      int read=in.read(buffer,0,(int) Math.min(buffer.length,len-count));
      if (read<0)
      { break;
      }
      out.write(buffer,0,read);
      count+=read;
      if (count==len)
      { break;
      }
    }
    return count;
  }
  
  public static byte[] readBytes(InputStream in)
    throws IOException
  {
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    copyRaw(in,out,DEFAULT_BUFFER_SIZE);
    return out.toByteArray();
  }

 
  /**
   * Discard [bytes] bytes of the input stream
   */
  public static long discard(InputStream in,long bytes)
    throws IOException
  { 
    long count=0;
    while (count<bytes)
    {
      long ret=in.skip(bytes);
      if (ret==-1)
      { break;
      }
      else
      { count+=ret;
      }
    }
    return count;
  }


}
