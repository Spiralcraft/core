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
package spiralcraft.lang;

import spiralcraft.common.ContextualException;
import spiralcraft.common.declare.DeclarationInfo;

public class BindException
  extends ContextualException
{
  private static final long serialVersionUID=1;
  
  public BindException(String message)
  { super(message);
  }

  public BindException(String message,Throwable cause)
  { super(message,cause);
  }
  
  public BindException(String message,Object context,Throwable cause)
  { super(message,context,cause);
  }

  public BindException(String message,DeclarationInfo context)
  { super(message,context);
  }
}
