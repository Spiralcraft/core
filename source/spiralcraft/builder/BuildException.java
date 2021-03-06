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
package spiralcraft.builder;

import java.net.URI;

import spiralcraft.common.ContextualException;

/**
 * General exception thrown when builder encounters a problem at some point.
 */
public class BuildException
  extends ContextualException
{

  private static final long serialVersionUID = 1;

  public BuildException(String message)
  { super(message);
  }

  public BuildException(String message,Throwable nested)
  { super(message,nested);
  }

  public BuildException(
    String message,
    URI declarationLocation,
    Exception x)
  { super(message,declarationLocation,x);
  }

  public BuildException(
    String message,
    URI declarationLocation
    )
  { super(message,declarationLocation);
  }

}
