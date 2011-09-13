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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import spiralcraft.log.ClassLog;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.file.FileResource;

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
  protected ClassLog log=ClassLog.getInstance(getClass());
  
  private String prefix="";
  private String suffix="";
  private File directory;
  protected boolean debug;
  
  
  private FileFilter filter
    =new FileFilter()
    {

      @Override
      public boolean accept(File pathname)
      {
        String identifier="";
        String path=pathname.getPath();
        
        if (path.startsWith(directory.getPath()))
        { 
          // Remove directory + trailing slash
          path=path.substring(directory.getPath().length()+1);
        }
        
        if (path.startsWith(prefix)
            && path.endsWith(suffix)
            )
        {
          identifier
            =path.substring(prefix.length(),path.length()-suffix.length());
          if (isSequenceId(identifier))
          { 
            if (debug)
            { log.fine("Included "+pathname+" ("+identifier+")");
            }
            return true;
          }
        }
        if (debug)
        { log.fine("Excluded "+pathname+" ("+identifier+")");
        }
        return false;
      }
    };
    
  private final Comparator<String> comparator
    =new Comparator<String>()
  {
    final Comparator<String> idComparator
      =getSequenceIdComparator();
    
    @Override
    public int compare(String p1,String p2)
    {
      
      String id1=p1.substring(prefix.length(),p1.length()-suffix.length());
      String id2=p2.substring(prefix.length(),p2.length()-suffix.length());
      return idComparator.compare(id1, id2);
    }
  };
  
  /**
   * Output information on path calculations, etc.
   * 
   * @param val
   */
  public void setDebug(boolean val)
  { debug=val;
  }
  
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
  
  public void setContainer(Resource container)
  { 
    FileResource resource
      =container.unwrap(FileResource.class);
    if (resource==null)
    { 
      throw new IllegalArgumentException
        (container.getURI()+" does not refer to the local filesystem");
    }
    directory=resource.getFile().getAbsoluteFile();
  }

  public abstract String getNextSequenceId();
  
  public abstract boolean isSequenceId(String sequenceId);
  
  public abstract Comparator<String> getSequenceIdComparator();
  
  /**
   * 
   * @return The File (whether it exists or not) that represents the 
   *   component of the FileSequence that data will be appended to.
   */
  public File getActiveFile()
  { return new File(directory,prefix+suffix);
  }
  
  public File getNextFile()
  { return new File(directory,prefix+getNextSequenceId()+suffix);
  }
  
  public void rotate()
    throws IOException
  { 
    File activeFile=getActiveFile();
    File nextFile=getNextFile();
    if (!activeFile.renameTo(nextFile))
    { 
      throw new IOException
        ("Rotation failed. Could not rename "+activeFile+" to "+nextFile);
      
    }
  }
  
  /**
   * List the paths of all the files in the set, relative to the 
   *   directory of this FileSequence
   *   
   * @return
   */
  public String[] listFilePaths()
    throws IOException
  {
    if (debug)
    { log.fine("Listing "+directory.getPath());
    }
    File[] allFiles
      =directory.listFiles(filter);
    
    if (allFiles==null)
    { 
      if (!directory.exists())
      { throw new FileNotFoundException(directory.getPath());
      }
      return null;
    }
    
    String[] paths=new String[allFiles.length];
    for (int i=0;i<allFiles.length;i++)
    {
      File file=allFiles[i];
      if (file.getPath().startsWith(directory.getPath()))
      { 
        paths[i]
          =file.getPath().substring(directory.getPath().length()+1);
      }
      else
      { paths[i]=file.getPath();
      }
              
    }
    
    Arrays.sort(paths,comparator);
    return paths;
  }
  
}
