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

import spiralcraft.log.ClassLog;
import spiralcraft.util.string.ByteArrayToHex;
import spiralcraft.util.string.ByteArrayTo7BitAscii;

/**
 * General purpose InputStream wrapper
 * 
 * @author mike
 *
 */
public class DebugInputStream
  extends InputStreamWrapper
{
  
  private static final ClassLog log
    =ClassLog.getInstance(DebugInputStream.class);
  private static volatile int nextId=0;
  private final int id=nextId++;
  
  private ByteArrayTo7BitAscii asciiConverter
    =new ByteArrayTo7BitAscii();
  private ByteArrayToHex hexConverter
    =new ByteArrayToHex();

  private long ms(long ns)
  { return ns / 1000000;
  }
  
  private String toString(byte[] bytes)
  { return hexConverter.toString(bytes)+" | "+asciiConverter.toString(bytes);
  }
  
  private byte[] subArray(byte[] b,int start,int len)
  {
    byte[] bn=new byte[len];
    System.arraycopy(b,start,bn,start,len);
    return bn;
  }
  
  public DebugInputStream(InputStream delegate)
  { 
    super(delegate);
    log.fine("in-stream#"+id+": Created");
    
  }
  
  @Override
  public int read()
    throws IOException
  { 
    long time=System.nanoTime();
    int val=super.read();
    log.fine("in-stream#"+id+": Read "+val+" in "
              +ms(System.nanoTime()-time)+" ms "
              +"["+toString(new byte[] {((Integer) val).byteValue()})+"]"
            );
    return val;
  }
  
  @Override
  public int read(byte[] b)
    throws IOException
  { 
    
    long time=System.nanoTime();
    int val=super.read(b);
    log.fine("in-stream#"+id+": Read "+val+" (of "+b.length+" requested) in "
            +ms(System.nanoTime()-time)+" ms "
            +"["+toString(b)+"]"
            );
    return val;
  }
  
  @Override
  public int read(byte[] b,int start, int len)
    throws IOException
  { 
    long time=System.nanoTime();
    int val=super.read(b,start,len);
    log.fine("in-stream#"+id+": Read "+val+" (of "+len+" requested) in "
            +ms(System.nanoTime()-time)+" ms "
            +"["+toString(subArray(b,start,val))+"]"
            );
    return val;
  }

  @Override
  public void close()
    throws IOException
  { 
    super.close();
    log.fine("in-stream#"+id+": Closed");
  }
}
