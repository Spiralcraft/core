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

/**
 * <p>Identifies a sequence of files closely related to each other where the
 *   file sequence id is embedded in the filename.
 * </p>
 * 
 * <p>Provides a mechanism for sharing the definitions of file sequences
 *   between producers and consumers. Used by producers to construct
 *   and maintain file sequences, and by consumers to read them.
 * </p>
 *
 * @author mike
 *
 */
public abstract class FileSequence
{
  private String prefix="";
  private String suffix="";
  private File directory;
  
  /**
   * 
   * @return The part of the filename before the sequence identifier
   */
  public String getPrefix()
  { return prefix;
  }
  
  /**
   * Specify the part of the filename before the sequence identifier
   * 
   * @param prefix
   */
  public void setPrefix(
    String prefix)
  { this.prefix = prefix;
  }
  
  /**
   * @return The part of the filename after the sequence identifier
   */
  public String getSuffix()
  { return suffix;
  }
  
  /**
   * Specify the part of the filename after the sequence identifier
   * 
   */
  public void setSuffix(String suffix)
  { this.suffix = suffix;
  }
  
  /**
   * Specify the directory that contains the FileSequence
   * 
   * @param directory
   */
  public void setDirectory(File directory)
  { this.directory=directory;
  }
  
  /**
   * 
   * @return The directory where this FileSequence is stored
   */
  public File getDirectory()
  { return directory;
  }
  

  public abstract String getNextSequenceId();
  
  public File getActiveFile()
  { return new File(directory,prefix+suffix);
  }
  
  public File getNextFile()
  { return new File(directory,prefix+getNextSequenceId()+suffix);
  }
  
  public void rotate()
  { getActiveFile().renameTo(getNextFile());
  }
  
  
  
  
}
