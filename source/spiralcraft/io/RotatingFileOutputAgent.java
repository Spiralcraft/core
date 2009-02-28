//
// Copyright (c) 1998,2009 Michael Toth
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

import spiralcraft.common.LifecycleException;
import spiralcraft.time.Clock;

import java.io.IOException;

import java.io.RandomAccessFile;

import java.util.Calendar;
import java.util.Date;

import java.io.File;

/**
 * <p>An OutputAgent that manages a file that rotates daily or when it exceeds
 *   a given size
 * </p>
 */
public class RotatingFileOutputAgent
  extends OutputAgent
{

  
  private FileSequence fileSequence;
  
  private RandomAccessFile _file;
  
  private long _maxLengthKB=16384;
  private Calendar calendar=Calendar.getInstance();
  private int calendarField=Calendar.DAY_OF_YEAR;
  private int periodId;

  public void setMaxLengthKB(long maxLengthKB)
  { _maxLengthKB=maxLengthKB;
  }

  public void setFileSequence(FileSequence fileSequence)
  { this.fileSequence=fileSequence;
  }

  
  /**
   * <p>The Calender field (eg. Calendar.DAY_OF_YEAR) that will trigger a
   *   rotation when changed.
   * </p>
   * 
   * @param calendarField
   */
  public void setCalendarField(int calendarField)
  { this.calendarField=calendarField;
  }
  
  protected byte[] header()
  { return null;
  }
  
  @Override
  protected void destroy()
  {
    if (_file!=null)
    { 
      try
      { _file.close();
      }
      catch (IOException x)
      { }
      
    }
    _file=null;
    return;    
  }
  
  @Override
  protected void prepare()
    throws IOException
  {
    if (_file==null)
    { 
      File targetFile
        =fileSequence.getActiveFile();
      

      if (targetFile.exists())
      { 
        calendar.setTime(new Date(targetFile.lastModified()));
        periodId=calendar.get(Calendar.DAY_OF_YEAR);
      }
        
      _file=new RandomAccessFile
        (targetFile
        ,"rw"
        );
    }
    if (_file.length()==0)
    {
      byte[] header=header();
      if (header!=null)
      { _file.write(header);
      }
    }
    else
    { _file.skipBytes((int) _file.length());
    }
  }
  
  @Override
  protected void output(byte[] bytes)
    throws IOException
  { 
    _file.write(bytes);
    _file.getFD().sync();
    if (periodChanged() || _file.length()>=_maxLengthKB*1024)
    {
      log.info(getLogPrefix()+": Rotating output file");
      _file.close();
      fileSequence.rotate();
      _file=null;
    }    
  }
  
  
  private boolean periodChanged()
  { 
    calendar.setTime(new Date(Clock.instance().approxTimeMillis()));
    int newPeriodId=calendar.get(calendarField);
    if (newPeriodId!=periodId)
    { 
      periodId=newPeriodId;
      return true;
    }
    else
    { return false;
    }
  }
  
  @Override
  protected String getLogPrefix()
  { 
    return fileSequence.getDirectory().toURI().getPath()
      +fileSequence.getPrefix();
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    periodId=calendar.get(calendarField);
    if (fileSequence.getPrefix()==null || fileSequence.getPrefix().isEmpty())
    { 
      throw new LifecycleException
        ("RotatingFileOutputAgent.fileSequence.prefix must be specified");
    }
    if (fileSequence.getSuffix()==null || fileSequence.getSuffix().isEmpty())
    { 
      throw new LifecycleException
        ("RotatingFileOutputAgent.fileSequence.suffix must be specified");
    }
    if (maxBufferSize==0)
    { maxBufferSize=(int) _maxLengthKB*1024;
    }
    super.start();
  }
  
 
}
