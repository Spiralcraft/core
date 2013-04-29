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
 * Represents one or more resources of a given type that have been enlisted as part of
 *   a Transaction. 
 */
public interface Branch
{

  Transaction.State getState();
  
  /**
   * @return Whether the branch supports 2-phase commit (2PC). Only one resource
   *   that does not support 2PC is permitted to take part in a Transaction involving
   *   multiple branches.
   */
  boolean is2PC();
  
  /**
   * Attempt to roll back this branch of the Transaction
   */
  void rollback()
    throws TransactionException;

  /**
   * Attempt to prepare this branch of the Transaction
   */
  void prepare()
    throws TransactionException;
  
  /**
   * Attempt to commit this branch of the Transaction
   */
  void commit()
    throws TransactionException;
  
  /**
   * Deallocate any resources consumed by the branch
   */
  void complete()
    throws TransactionException;
}
