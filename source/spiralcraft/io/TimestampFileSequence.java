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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
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

  @Override
  public Comparator<String> getSequenceIdComparator()
  {
    return new Comparator<String>()
    {

      @Override
      public int compare(String o1,String o2)
      {
        if (o1.length()==0)
        { return 1;
        }
        else if (o2.length()==0)
        { return -1;
        }
        else
        { 
          try
          {
            Date d1=dateFormat.parse(o1);
            Date d2=dateFormat.parse(o2);
            
            if (d1.getTime()<d2.getTime())
            { return -1;
            }
            else if (d1.getTime()>d2.getTime())
            { return 1;
            }
            else
            { return 0;
            }
          }
          catch (ParseException x)
          { throw new IllegalArgumentException(x);
          }
        }
      }
    };
  }

  @Override
  public boolean isSequenceId(
    String sequenceId)
  { 
    try
    { return sequenceId.length()==0 || dateFormat.parse(sequenceId)!=null;
    }
    catch (ParseException x)
    { return false;
    }
  }
  
  
}
