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

import spiralcraft.log.ClassLog;
import spiralcraft.util.string.ByteArrayToHex;
import spiralcraft.util.string.ByteArrayTo7BitAscii;

/**
 * <p>An OutputStream that logs all data
 * </p>
 */
public class DebugOutputStream
  extends OutputStream
{

  private static final ClassLog log
    =ClassLog.getInstance(DebugInputStream.class);
  private static volatile int nextId=0;
  private final int id=nextId++;
  private ByteArrayTo7BitAscii asciiConverter
    =new ByteArrayTo7BitAscii();
  private ByteArrayToHex hexConverter
    =new ByteArrayToHex();
  private final OutputStream out;

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
  
  public DebugOutputStream(OutputStream out)
  { 
    this.out=out;
    log.fine("out-stream#"+id+": Created");
  }
  
  @Override
  public void write(int val)
    throws IOException
  {
    long time=System.nanoTime();
    out.write(val);
    log.fine("out-stream#"+id+": Wrote "+val+" in "
             +ms(System.nanoTime()-time)+" ms "
             +"["+toString(new byte[] {((Integer) val).byteValue()})+"]"
             );
  }
  
  @Override
  public void write(byte[] bytes)
    throws IOException
  { 
    long time=System.nanoTime();
    out.write(bytes);
    log.fine("out-stream#"+id+": Wrote "+bytes.length+" bytes in "
             +ms(System.nanoTime()-time)+" ms "
             +"["+toString(bytes)+"]"
             );
    
  }
  
  @Override
  public void write(byte[] bytes,int start,int len)
    throws IOException
  { 
    long time=System.nanoTime();
    out.write(bytes,start,len);
    log.fine("out-stream#"+id+": Wrote "+len+" bytes in "
             +ms(System.nanoTime()-time)+" ms "
             +"["+toString(subArray(bytes,start,len))+"]"
             );
    
  } 
  
  @Override
  public void close()
    throws IOException
  { 
    long time=System.nanoTime();
    out.close();
    log.fine("out-stream#"+id+": Closed in "+ms(System.nanoTime()-time)+" ms");
  }
  
}
