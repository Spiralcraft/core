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

/**
 * <p>Provides access to a sequence of Records. A Record is a subdivision of
 *   a binary data unit that is organized around some record separator.
 * </p>
 * 
 *
 * @author mike
 *
 */
public interface RecordIterator
{
 
  public int getRecordPointer();
  
  /** 
   * In the EOF (End Of File) state, the record pointer is past the last
   *   record.
   * 
   * @return Whether the Record pointer is past the last record.
   * 
   * @throws IOException
   */
  public boolean isEOF()
    throws IOException;
  
  /**
   * In the BOF (Beginning Of File) state, the record pointer is before the
   *   first record. Call next() before reading the first record.
   *   
   * @return
   */
  public boolean isBOF();
  
  /**
   * Close the underlying resource
   */
  public void close()
    throws IOException;
  
  /**
   * <p>Read len bytes of the the current record, starting at the
   *   specifed recordOffset, into the specified
   *   buffer at the specified bufferOffset
   * </p>
   * 
   * @param recordOffset The number of bytes, within the record, to start reading
   * @param buffer The destination buffer for the data
   * @param bufferOffset The position in the destination buffer to start writing
   * @param len The maximum number of bytes to read
   * @return The number of bytes read
   * @throws IOException If there is a problem reading data
   */
  public int read(int recordOffset,byte[] buffer,int bufferOffset,int len)
    throws IOException;
  
  /**
   * Return a copy of the record buffer
   * 
   * @return
   * @throws IOException
   */
  public byte[] read()
    throws IOException;
  
  /**
   * Position the record pointer on the next record
   * 
   * @return true If the record pointer moved, false if EOF
   * @throws IOException
   */  
  public boolean next()
    throws IOException;
  
  /**
   * Skip the next <i>count</i> records.
   * 
   * @param count
   */
  public void skip(int count)
    throws IOException;

}
