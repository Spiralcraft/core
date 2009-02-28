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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Identifies a sequence of files closely related to each other where the file
 *   sequence id is embedded in the filename 
 *
 * @author mike
 *
 */
public class TimestampFileSequence
  extends FileSequence
{

  private DateFormat dateFormat
    =new SimpleDateFormat("-yyyy-MM-dd--HH-mm-ss");
  
  /**
   * 
   * @return The date format used to generate the sequence identifier
   */
  public DateFormat getDateFormat()
  { return dateFormat;
  }
  
  /**
   * 
   * @param dateFormat
   */
  public void setDateFormat(
    DateFormat dateFormat)
  { this.dateFormat = dateFormat;
  }

  @Override
  public String getNextSequenceId()
  {
    File current=getActiveFile();
    if (current.exists())
    { return dateFormat.format(new Date(current.lastModified()));
    }
    else
    { return dateFormat.format(new Date());
    }
  }
  
  
}
