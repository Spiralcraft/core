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
package spiralcraft.stream.batch;

import spiralcraft.util.Arguments;

import spiralcraft.stream.Resource;

import spiralcraft.exec.ExecutionContext;

public class PrintOperation
  implements Operation
{

  private Operation _nextOperation;
  
  public void setNextOperation(Operation next)
  { _nextOperation=next;
  }
  
  public void invoke(Resource resource)
    throws OperationException
  { 
    ExecutionContext.getInstance().out().println(resource.getURI());
    if (_nextOperation!=null)
    { _nextOperation.invoke(resource);
    }
  }
  
  public boolean processOption(Arguments args,String option)
  { return false;
  }
  
  public boolean processArgument(Arguments args,String argument)
  { return false;
  }
}
