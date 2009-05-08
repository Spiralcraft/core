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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A FileInputStream from a consumable File- eg. a temporary disk buffer.
 *   The File will be deleted when the InputStream is closed.
 * 
 * @author mike
 */
public class ConsumableFileInputStream
  extends InputStreamWrapper
{
  private File tempFile;
  
  public ConsumableFileInputStream(File tempFile,int bufferSize)
    throws IOException
  { 
    super(new BufferedInputStream(new FileInputStream(tempFile),bufferSize));
    this.tempFile=tempFile;
    this.tempFile.deleteOnExit();
  }
  
  
  @Override
  public void close()
    throws IOException
  {
    try
    { super.close();
    }
    finally
    { tempFile.delete();
    }
  }
  
  
}
