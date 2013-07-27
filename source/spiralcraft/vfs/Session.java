//
// Copyright (c) 2013 Michael Toth
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import spiralcraft.vfs.file.FileResource;

/**
 * Maintains open resources and temporary files for the duration of an
 *   operation and cleans them up when the operation is complete.
 * 
 * @author mike
 *
 */
public class Session
{

  private static volatile int nextId=1;
  private ArrayList<File> tempFiles
    =new ArrayList<File>();
  private final int id=nextId++;
  
  public FileResource asLocalResource(Resource resource)
    throws IOException
  { 
    FileResource fileResource;
    if (resource instanceof FileResource)
    { fileResource=(FileResource) resource;
    }
    else
    {
      File tempFile
        =File.createTempFile("session."+id+".",resource.getLocalName());
      tempFile.deleteOnExit();
      tempFiles.add(tempFile);
      fileResource=new FileResource(tempFile);
      fileResource.copyFrom(resource);
    }
    
    return fileResource;
  }
  
  public void release()
  {
    for (File file: tempFiles)
    { file.delete();
    }
  }
}
