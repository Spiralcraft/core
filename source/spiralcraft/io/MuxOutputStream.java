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
import java.util.ArrayList;

/**
 * <p>An OutputStream that sends a copy of its output to none or more secondary 
 *   streams
 * </p>
 */
public class MuxOutputStream
  extends OutputStream
{

  private final OutputStream out;
  private final ArrayList<OutputStream> mux=new ArrayList<>();
  private final boolean closeMux;
  
  public MuxOutputStream(OutputStream out,boolean closeMux)
  { 
    this.out=out;
    this.closeMux=closeMux;
  }
  
  public void addMux(OutputStream tap)
  { this.mux.add(tap);
  }
  
  public void removeMux(OutputStream tap)
  { this.mux.remove(tap);
  }
  
  @Override
  public void write(int val)
    throws IOException
  {
    out.write(val);
    for (OutputStream tap :mux)
    { 
      try
      { tap.write(val);
      }
      catch (IOException x)
      { }
    }
  }
  
  @Override
  public void write(byte[] bytes)
    throws IOException
  { 
    out.write(bytes);
    for (OutputStream tap :mux)
    { 
      try
      { tap.write(bytes);
      }
      catch (IOException x)
      { }
    }
    
  }
  
  @Override
  public void write(byte[] bytes,int start,int len)
    throws IOException
  { 
    out.write(bytes,start,len);
    for (OutputStream tap :mux)
    { 
      try
      { tap.write(bytes,start,len);
      }
      catch (IOException x)
      { }
    }
    
  }  
  @Override
  public void flush()
    throws IOException
  {
    out.flush();
    for (OutputStream tap :mux)
    { 
      try
      { tap.flush();
      }
      catch (IOException x)
      { }
    }
  }
  
  @Override
  public void close()
    throws IOException
  {
    out.close();
    if (closeMux)
    { 
      for (OutputStream tap :mux)
      { 
        try
        { tap.close();
        }
        catch (IOException x)
        { }
      }
      
    }
  }
}
