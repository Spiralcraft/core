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
package spiralcraft.data.transaction;

/** 
 * Manages a specific resource that participates in Transactions. 
 */
public abstract class ResourceManager<T extends Branch>
{
  /**
   * Create a new branch for the given Transaction. 
   */
  public abstract T createBranch(Transaction transaction)
    throws TransactionException;
  
  /**
   * Provide the existing branch or create a new one
   */
  @SuppressWarnings("unchecked") // Transaction.branch() is heterogeneous
  public final T branch(Transaction transaction)
    throws TransactionException
  { return (T) transaction.branch(this);
  }
}
