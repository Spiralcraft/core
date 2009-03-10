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
 * <p>A random-access, bidirectional RecordIterator.
 * </p>

 *
 * @author mike
 *
 */
public interface ScrollableRecordIterator
  extends RecordIterator
{
 
  /**
   * Position the record pointer on the previous record
   * 
   * @return The record pointer
   * @throws IOException
   */
  public boolean previous()
    throws IOException;
  
  /**
   * Position the record pointer on the specified record
   * 
   * @param recordNum
   */
  public boolean seek(long recordNum)
    throws IOException;
  
  /**
   * Position the record pointer after the last record
   * 
   */
  public void bottom()
    throws IOException;

  /**
   * Position the record pointer on the first record
   */
  public void top()
    throws IOException;
  
}
