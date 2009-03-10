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

import java.io.IOException;
import java.io.RandomAccessFile;

import spiralcraft.util.ArrayUtil;

/**
 * <p>Provides a record based interface into a RandomAccessFile that is
 *   organized using a record delimiter.
 * </p>
 * 
 * <p>A records is a sequence of bytes terminated with the record delimiter or
 *   EOF.
 * </p>
 * 
 * <p>The FileRecordIterator starts out before the first record, signified
 *   by isBOF() == true and recordPointer == -1
 * </p>
 * @author mike
 *
 */
public class FileRecordIterator
{
 
  private final RandomAccessFile file;
  private final byte[] delimiter;
  private int recordPointer=-1;
  private final KmpMatcher forwardMatcher;
  private final KmpMatcher reverseMatcher;
  private byte[] fileBuffer=new byte[1024];
  
  
  public FileRecordIterator(RandomAccessFile file,byte[] delimiter)
    throws IOException
  {
    this.file=file;
    this.delimiter=delimiter;
    forwardMatcher=new KmpMatcher(delimiter);
    reverseMatcher=new KmpMatcher((byte[]) ArrayUtil.reverse(delimiter));
    file.seek(0);
  }
  
  public int getRecordPointer()
  { return recordPointer;
  }
  
  public boolean isEOF()
    throws IOException
  { return file.getFilePointer()>=file.length();
  }
  
  public boolean isBOF()
  { return recordPointer<0;
  }
  
  /**
   * <p>Read len bytes of the the current record, starting at the
   *   specifed recordOffset, into the specified
   *   buffer at the specified bufferOffset
   * </p>
   * 
   * @param recordOffset
   * @param buffer
   * @param bufferOffset
   * @param len
   * @return The number of bytes read
   * @throws IOException If there is a problem reading data
   */
  public int read(int recordOffset,byte[] buffer,int bufferOffset,int len)
    throws IOException
  { 
    if (isBOF())
    { return -1;
    }
    if (isEOF())
    { return -1;
    }
        
    
    long filePos=file.getFilePointer();
    
    try
    { 
      
      forwardMatcher.reset();
      int skipCount=0;
      int readCount=0;
      
      while (true)
      {
        int bytes=file.read(fileBuffer,0,len);
        if (bytes==-1)
        { 
          if (readCount>0)
          { return readCount;
          }
          else
          { return -1;
          }
        }
        for (int i=0;i<bytes;i++)
        {   
          if (forwardMatcher.match(fileBuffer[i]))
          { 
            forwardMatcher.reset();
            readCount=Math.max(0,readCount-(delimiter.length-1));
            if (readCount>0)
            { return readCount;
            }
            else
            { return -1;
            }
          }
          else if (skipCount<recordOffset)
          { skipCount++;
          }
          else if (readCount<len)
          { 
            buffer[bufferOffset+readCount]=fileBuffer[i];
            readCount++;
          }
          else
          {
            if (readCount>0)
            { return readCount;
            }
            else
            { return -1;
            }
          }
        }
      }
    }
    finally
    { file.seek(filePos);
    }
    
  }
  
  /**
   * Position the record pointer on the next record
   * 
   * @return true If the record pointer moved, false if EOF
   * @throws IOException
   */  
  public boolean next()
    throws IOException
  { 
    if (isEOF())
    { return false;
    }
    
    if (isBOF())
    { 
      file.seek(0);
      recordPointer=0;
      return !isEOF();
    }
    
    long filePos=file.getFilePointer();
    try
    {
      forwardMatcher.reset();
      while (true)
      {
        int bytes=file.read(fileBuffer);
        if (bytes==-1)
        { return false;
        }
        for (int i=0;i<bytes;i++)
        {   
          filePos++;
          if (forwardMatcher.match(fileBuffer[i]))
          { 
            forwardMatcher.reset();
            recordPointer++;
            file.seek(filePos);
            return !isEOF();
          }
        }
      }
    }
    finally 
    { file.seek(filePos);
    }
  }
  

  /**
   * Position the record pointer on the previous record
   * 
   * @return The record pointer
   * @throws IOException
   */
  public boolean previous()
    throws IOException
  { 
    long filePos=file.getFilePointer();
    if (recordPointer==0 || filePos==0)
    { 
      recordPointer=-1;
      file.seek(0);
      return false;
    }
    
    try
    {
      
      reverseMatcher.reset();
      boolean inPrevious=false;
      while (true)
      {
        
        long currentPos=file.getFilePointer();
        if (currentPos==0)
        {
          filePos=0;
          return false;
        }
        long startPos=Math.max(currentPos-fileBuffer.length,0);
        int offset = (int) Math.max(fileBuffer.length-currentPos,0);
        file.seek(startPos);
        int bytes=file.read(fileBuffer,offset,fileBuffer.length-offset);
        
        for (int i=0;i<bytes;i++)
        {   
          filePos--;
          if (reverseMatcher.match(fileBuffer[fileBuffer.length-i-1]))
          { 
            reverseMatcher.reset();
            if (!inPrevious)
            { inPrevious=true;
            }
            else
            {
              filePos=filePos+delimiter.length;
              recordPointer--;
              return true;
            }
          }
        }
      }
      
      
      
    }
    finally
    { file.seek(filePos);
    }
  }
  
  /**
   * Position the record pointer on the specified record
   * 
   * @param recordNum
   */
  public boolean seek(long recordNum)
    throws IOException
  { 
    if (recordNum==recordPointer)
    { return true;
    }
    else if (recordNum>recordPointer)
    {
      while (next())
      {
        if (recordNum==recordPointer)
        { return true;
        }
      }
      return false;
    }
    else
    {
      while (previous())
      {
        if (recordNum==recordPointer)
        { return true;
        }
      }
      return false;
    }
  }
  
  /**
   * Position the record pointer after the last record
   * 
   */
  public void bottom()
    throws IOException
  { 
    while (next())
    { // NOOP
    }
  }

  /**
   * Position the record pointer on the first record
   */
  public void top()
    throws IOException
  { 
    file.seek(0);
    recordPointer=0;
  }
  
}
