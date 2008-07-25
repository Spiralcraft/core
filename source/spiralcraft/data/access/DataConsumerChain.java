//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.data.access;

import spiralcraft.data.Tuple;

/**
 * Processes data from a DataSource and sends it to the DataHandler
 */
public interface DataConsumerChain<T extends Tuple>
  extends DataConsumer<T>
{
  
  /**
   * Set the next DataConsumer in the chain
   */
  void setDataConsumer(DataConsumer<?> consumer);
  
  /**
   * Insert the specified DataConsumerChain into the chain
   *   immediately after this one
   */
  void insertDataConsumer(DataConsumerChain<?> consumerChain);
  
}