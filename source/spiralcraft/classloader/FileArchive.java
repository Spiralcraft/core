//
// Copyright (c) 1998,2008 Michael Toth
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

package spiralcraft.classloader;

import java.io.File;

import spiralcraft.vfs.file.FileResource;

/**
 * An archive contained in a directory tree
 * 
 * @author mike
 *
 */
public class FileArchive
  extends ResourceArchive
{
  
  
  public FileArchive(File file)
  { super(new FileResource(file));
  }
   

  
}
