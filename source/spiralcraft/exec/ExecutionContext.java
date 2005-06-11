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
package spiralcraft.exec;

import java.io.PrintStream;
import java.io.InputStream;

import java.net.URI;

/**
 * Provides a minimal representation of a user environment for user driven 
 *   entry points.
 */
public abstract class ExecutionContext
{
  private static ThreadLocal _INSTANCE = new ThreadLocal() 
  {
     protected synchronized Object initialValue() 
     { return null;
     }
  };
    
  public static final ExecutionContext getInstance()
  { return (ExecutionContext) _INSTANCE.get();
  }

  static final void setInstance(ExecutionContext context)
  { 
    _INSTANCE.set(context);
    return;
  }
 
  public abstract PrintStream out();
  
  public abstract InputStream in();

  public abstract PrintStream err();

  /**
   * Return the user focus URI- the URI equivalent of the "current directory"
   *   in a file system
   */
  public abstract URI focusURI();
  
  /**
   * Convert a relative or context-mapped URI to an absolute URI. Other
   *   URIs are passed through unchanged.
   */
  public URI canonicalize(URI relativeURI)
  {
    if (!relativeURI.isAbsolute())
    { return focusURI().resolve(relativeURI);
    }
    else
    { return relativeURI;
    }
  }
}

