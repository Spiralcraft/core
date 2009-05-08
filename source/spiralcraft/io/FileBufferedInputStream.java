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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import spiralcraft.vfs.StreamUtil;

/**
 * Reads an entire InputStream to local temporary File, and then provides access
 *    to the data by delegating to a FileInputStream. Note that the input
 *    stream must be readable in its entirety or the constructor will block
 *    indefinitely.
 * 
 * @author mike
 *
 */
public class FileBufferedInputStream
  extends InputStreamWrapper
{
  
  private static ConsumableFileInputStream 
    buffer(InputStream source,int bufferSize)
    throws IOException
  {
    
    File tempFile
      =File.createTempFile("spiralcraft.io.DiskBufferedInputStream",".tmp");
    tempFile.deleteOnExit();
    
    OutputStream out=new FileOutputStream(tempFile);
    
    try
    { 
      StreamUtil.copyRaw(source,out,bufferSize,-1);
      source.close();
      source=null;
      out.flush();
      out.close();
      out=null;
      return new ConsumableFileInputStream(tempFile,bufferSize);
      
    }
    catch (IOException x)
    { 
      tempFile.delete();
      throw x;
    }
    finally
    {
      try
      {
        if (source!=null)
        { source.close();
        }
      }
      catch (IOException x)
      {
      }

      try
      {
        if (out!=null)
        { out.close();
        }
      }
      catch (IOException x)
      {
      }
    }
    
  }  
  
  public FileBufferedInputStream(InputStream source,int bufferSize)
    throws IOException
  { super(buffer(source,bufferSize));
  }
  

  
}
