//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.query;

import spiralcraft.data.DataException;
import spiralcraft.data.transport.SerialCursor;

/**
 * <P>A BoundQuery is a data path that resolves sets of Tuples based on sets of
 *    parameters and criteria. 
 *    
 * <P>It is created when a Query is bound to a Focus
 *    and a Space. The Focus provides external parameter values, and the Space
 *    provides access to Data.
 */
public interface BoundQuery
{

  /**
   * @return A SerialCursor which provides access to the results of the Query
   * 
   * @throws DataException If anything goes wrong when executing the Query
   */
  public SerialCursor execute()
    throws DataException;
}
