//
// Copyright (c) 1998,2009 Michael Toth
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


import spiralcraft.data.DeltaTuple;
import spiralcraft.data.transaction.TransactionException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;


/**
 * <p>Abstract trigger for Delta level updates
 * </p>
 * 
 * <p>The trigger is bound into a Focus where the DeltaTuple representing
 *   the incoming change is the subject.
 * </p>
 *   
 * 
 * @author mike
 *
 */
public abstract class DeltaTrigger
  extends Trigger
{
 
  private boolean forInsert;
  private boolean forUpdate;
  private boolean forDelete;
  

  
  
  /**
   * Specify that the Trigger should run when a Tuple is inserted
   * 
   * @param forInsert
   */
  public void setForInsert(boolean forInsert)
  { this.forInsert=forInsert;
  }
  
  /**
   * 
   * @return whether the Trigger should run when a Tuple is inserted
   */
  public boolean isForInsert()
  { return forInsert;
  }
  
  /**
   * Specify that the Trigger should run when a Tuple is updated
   * 
   * @param forInsert
   */
  public void setForUpdate(boolean forUpdate)
  { this.forUpdate=forUpdate;
  }

  /**
   * 
   * @return whether the Trigger should run when a Tuple is updated
   */
  public boolean isForUpdate()
  { return forUpdate;
  }

  
  /**
   * Specify that the Trigger should run when a Tuple is deleted
   * 
   * @param forInsert
   */
  public void setForDelete(boolean forDelete)
  { this.forDelete=forDelete;
  }

  /**
   * 
   * @return whether the Trigger should run when a Tuple is deleted
   */
  public boolean isForDelete()
  { return forDelete;
  }
 
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    return focusChain;
  }
  
  public abstract DeltaTuple trigger()
    throws TransactionException;
  



}
