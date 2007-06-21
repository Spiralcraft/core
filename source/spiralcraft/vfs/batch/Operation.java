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
package spiralcraft.vfs.batch;

import spiralcraft.util.Arguments;
import spiralcraft.vfs.Resource;


public interface Operation
{
  /**
   * The operation that will be invoked after this one finishes
   */
  void setNextOperation(Operation next);
  
  /**
   * Invoke this operation on the specified resource
   */
  void invoke(Resource resource)
    throws OperationException;
  
  /**
   * Accept a configuration option from the specified argument set
   *
   *@return true if the option is recognized
   */
  boolean processOption(Arguments args,String option);
  
  /**
   * Accept an argument from the specified argument set
   *
   *@return true if the argument is recognized
   */
  boolean processArgument(Arguments args,String option);
}
