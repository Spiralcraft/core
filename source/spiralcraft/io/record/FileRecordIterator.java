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
package spiralcraft.io.record;

import java.io.IOException;
import java.io.RandomAccessFile;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.ByteBuffer;
import spiralcraft.util.BytePatternMatcher;

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
  implements ScrollableRecordIterator
{
 
  private final RandomAccessFile file;
  private final ByteBuffer tempBuffer=new ByteBuffer();
  private final byte[] delimiter;
  private int recordPointer=-1;
  private final BytePatternMatcher forwardMatcher;
  private final BytePatternMatcher reverseMatcher;
  private byte[] fileBuffer=new byte[1024];
  private byte[] recordBuffer;
  
  
  public FileRecordIterator(RandomAccessFile file,byte[] delimiter)
    throws IOException
  {
    this.file=file;
    this.delimiter=delimiter;
    forwardMatcher=new BytePatternMatcher(delimiter);
    reverseMatcher=new BytePatternMatcher(ArrayUtil.reverse(delimiter));
    file.seek(0);
  }
  
  @Override
  public int getRecordPointer()
  { return recordPointer;
  }
  
  @Override
  public boolean isEOF()
    throws IOException
  { return file.getFilePointer()>=file.length();
  }
  
  @Override
  public boolean isBOF()
  { return recordPointer<0;
  }

  @Override
  public byte[] read()
  { return recordBuffer;
  }
  
  @Override
  public void close()
    throws IOException
  { file.close();
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
  @Override
  public int read(int recordOffset,byte[] buffer,int bufferOffset,int len)
    throws IOException
  { 
    if (isBOF())
    { return -1;
    }
    if (isEOF())
    { return -1;
    }
    
    if (recordOffset>=recordBuffer.length)
    { return -1;
    }
    
    len=Math.min(recordBuffer.length-recordOffset,len);
    if (len==0)
    { return -1;
    }
    System.arraycopy(recordBuffer,recordOffset,buffer,bufferOffset,len);
    return len;
  }
  
  private void finalizeRecordBuffer()
  {
    if (tempBuffer.length()<=delimiter.length)
    { recordBuffer=new byte[0];
    }
    else
    {
      recordBuffer=new byte[tempBuffer.length()-delimiter.length];
      tempBuffer.toArray(recordBuffer);
    }
  }
  
  private void buffer()
    throws IOException
  {
    long filePos=file.getFilePointer();
    try
    {
      forwardMatcher.reset();
      
      tempBuffer.clear();
      while (true)
      {
        int bytes=file.read(fileBuffer,0,fileBuffer.length);
        if (bytes==-1)
        { 
          // Tolerate premature EOF
          finalizeRecordBuffer();
          return;
        }

        for (int i=0;i<bytes;i++)
        {   
          tempBuffer.append(fileBuffer[i]);
          if (forwardMatcher.match(fileBuffer[i]))
          { 
            forwardMatcher.reset();
            finalizeRecordBuffer();
            return;
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
  @Override
  public boolean next()
    throws IOException
  { 
    // Positions the file pointer at the beginning of the next record
    if (isEOF())
    { return false;
    }
    
    if (isBOF())
    { 
      
      file.seek(0);
      buffer();
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
            buffer();
            return !isEOF();
          }
        }
      }
    }
    finally 
    { file.seek(filePos);
    }
  }
  

  @Override
  public void skip(int count)
    throws IOException
  {
    for (int i=0;i<count;i++)
    {
      if (!next())
      { break;
      }
    }
  }
  
  /**
   * Position the record pointer on the previous record
   * 
   * @return The record pointer
   * @throws IOException
   */
  @Override
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
              file.seek(filePos);
              buffer();
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
  @Override
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
  @Override
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
  @Override
  public void top()
    throws IOException
  { 
    file.seek(0);
    recordPointer=0;
  }
  
}
