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
import java.io.InputStream;

import spiralcraft.util.ByteBuffer;
import spiralcraft.util.BytePatternMatcher;

/**
 * <p>Provides a record based interface into an InputStream that is
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
public class InputStreamRecordIterator
  implements RecordIterator
{
 
  private final InputStream in;
  private final byte[] delimiter;
  private final ByteBuffer tempBuffer=new ByteBuffer();
  private int recordPointer=-1;
  private final BytePatternMatcher forwardMatcher;
  private byte[] recordBuffer;
  private boolean eof;
  private boolean eofPending;
  private boolean eofEndsRecord=true;
  
  
  public InputStreamRecordIterator(InputStream in,byte[] delimiter)
  {
    this.in=in;
    this.delimiter=delimiter;
    forwardMatcher=new BytePatternMatcher(delimiter);
  }
  
  /**
   * Specify whether an EOF will cause the content, if any, after the
   *   last record terminator to be processed as a record
   * 
   * @param eofEndsRecord
   */
  public void setEofEndsRecord(boolean eofEndsRecord)
  { this.eofEndsRecord=eofEndsRecord;
  }
  
  @Override
  public int getRecordPointer()
  { return recordPointer;
  }
  
  @Override
  public boolean isEOF()
    throws IOException
  { return eof;
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
  { in.close();
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
    if (isEOF())
    { return false;
    }
    
    forwardMatcher.reset();
    tempBuffer.clear();
    while (true)
    {
      int input=eofPending?-1:in.read();
      if (input==-1)
      {
        
        if (eofPending || !eofEndsRecord || tempBuffer.length()==0)
        { 
          eof=true;
          return false;
        }
        else
        { 
          eofPending=true;
          recordPointer++;
          recordBuffer=new byte[tempBuffer.length()];
          tempBuffer.toArray(recordBuffer);
          return true;
          
        }
      }
      tempBuffer.append(input);
      if (forwardMatcher.match((byte) input))
      { 
        forwardMatcher.reset();
        recordPointer++;
        recordBuffer=new byte[tempBuffer.length()-delimiter.length];
        tempBuffer.toArray(recordBuffer);
        return true;
      }
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
    
}
