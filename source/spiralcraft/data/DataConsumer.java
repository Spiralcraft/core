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
package spiralcraft.data;

/**
 * A passive consumer of Tuple data. 
 */
interface DataConsumer
{
  /**
   * Called once before dataAvailable() to allow the DataConsumer to verify
   *   and bind to the applicable Tuple Scheme.
   */
  void dataInitialize(Scheme scheme)
    throws DataException;
  
  /**
   * Called one or more times when data is available to consume. The
   *   DataConsumer should copy data from this tuple.
   */
  void dataAvailable(Tuple tuple)
    throws DataException;
    
  
}
