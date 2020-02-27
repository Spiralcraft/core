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
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

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
   *   using a buffer of the specified size and blocking until the specified
   *   number of bytes is read.
   */
  public static long copyRaw
    (InputStream in
    ,OutputStream out
    ,int bufferSize
    ,long len
    )
    throws IOException
  { 
    if (bufferSize<=0)
    { bufferSize=DEFAULT_BUFFER_SIZE;
    }
    byte[] buffer=new byte[bufferSize];
    long count=0;
    
    if (len<=0)
    {
      for (;;)
      { 
        int read=in.read(buffer,0,buffer.length);
        if (read<0)
        { break;
        }
        out.write(buffer,0,read);
        count+=read;
      }
    }
    else
    {
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

  public static byte[] readBytes(InputStream in,long len)
    throws IOException
  {
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    copyRaw(in,out,DEFAULT_BUFFER_SIZE,len);
    return out.toByteArray();
  }
  
  /**
   * Read numBytes from an InputStream and encode them into a String.
   * 
   * @param in
   * @param numBytes
   * @param encoding
   * @return
   * @throws IOException
   */
  public static String readString(InputStream in,int numBytes,Charset encoding)
    throws IOException
  { 
    byte[] bytes=readBytes(in,numBytes);
    return new String(bytes,encoding);
  }
 
  public static String readAsciiString(InputStream in,int len)
    throws IOException
  { 
    byte[] bytes=readBytes(in,len);
    char[] chars=new char[bytes.length];
    int i=0;
    
    for (byte b:bytes)
    { chars[i++]=(char) b;
    }
    return new String(chars);
  }
  
  public static void writeAsciiString(OutputStream out,String content)
    throws IOException
  { 
    byte[] bytes=new byte[content.length()];
    for (int i=0;i<content.length();i++)
    { bytes[i]=(byte) content.charAt(i);
    }
    out.write(bytes);
  }
  
  
  /**
   * Read buffers of text, stopping cleanly as line endings are
   *   encountered or the buffer fills up.
   */
  public final static String readUntilEOL
    (final InputStream in
    ,byte[] buffer
    ,int maxLength
    ,String encoding
    )
    throws IOException
  {
    if (buffer==null)
    { buffer=new byte[8192];
    }
    if (encoding==null)
    { encoding="UTF-8";
    }
    int totalCount=0;

    String ret=null;
    while (totalCount<maxLength)
    {
      int count
        =readUntil
          (in
          ,buffer
          ,(byte) '\n'
          ,0
          ,Math.min(maxLength-totalCount,buffer.length)
          );
     
      if (count==-1)
      { break;
      }
      totalCount+=count;
      

      if (count==0)
      { 
        ret=new String(buffer,0,0,encoding);
        break;
      }
      else
      {
        if (buffer[count-1]=='\n')
        { 
          // Deal with possibility of just a \n as a line term 
          final int trim=(count==1||buffer[count-2]!='\r')?1:2;
          ret=new String(buffer,0,count-trim,encoding);
          break;
        }
        else
        { ret=new String(buffer,0,count,encoding);
        }
      }
    }
    return ret;
  }

  /**
   * Read a line of ascii text ending with an \r\n.
   */
  public final static String readAsciiLine
    (final InputStream in
    ,byte[] buffer
    ,int maxLength
    )
    throws IOException
  {
    if (buffer==null)
    { buffer=new byte[8192];
    }
    
    StringBuilder ret=new StringBuilder();
    int totalCount=0;
    while (totalCount<maxLength)
    {
      int count
        =readUntil
          (in
          ,buffer
          ,(byte) '\n'
          ,0
          ,Math.min(maxLength-totalCount,buffer.length)
          );
     
      if (count==-1)
      { break;
      }
      totalCount+=count;
      char[] _charBuffer=new char[count];
      for (int i=0;i<count;i++)
      { _charBuffer[i]=(char) buffer[i];
      }

      if (_charBuffer[count-1]=='\n')
      { 
        // Deal with possibility of just a \n as a line term 
        final int trim=(count==1||_charBuffer[count-2]!='\r')?1:2;
        ret.append(_charBuffer,0,count-trim);
        break;
      }
      else
      { ret.append(_charBuffer,0,count);
      }
    }
    return ret.toString();
  }

  /**
   * Read an input stream until a marker is reached.
   *
   *@return The number of characters read
   */
  public final static int readUntil
    (final InputStream in
    ,final byte[] bytes
    ,final byte marker
    , final int start
    ,final int len
    )
    throws IOException
  {
    for (int i=0;i<len;i++)
    { 
      final int b=in.read();
      if (b==-1)
      { return i;
      }
      bytes[start+i]=(byte) b;
      
      if (b== marker)
      { return i+1;
      }
    }
    return len;
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

  /**
   * Discard the remainder of the input stream
   */
  public static long drain(InputStream in)
    throws IOException
  { 
    
    long count=0;
    while (true)
    {
      long ret=in.skip(Long.MAX_VALUE);
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
