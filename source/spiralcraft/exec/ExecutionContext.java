//
// Copyright (c) 1998,2008 Michael Toth
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

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>Provides contextual information for specific invocations of user driven
 *   entry points to decouple coarse-grained application logic from using
 *   java.lang.System as a "global" context.
 * </p>
 */
public abstract class ExecutionContext
{
  private static final ExecutionContext _SYSTEM_CONTEXT
    =new SystemExecutionContext();
  
  private static ThreadLocalStack<ExecutionContext> _INSTANCE 
    = new ThreadLocalStack<ExecutionContext>(true)
  {
    @Override
    public ExecutionContext defaultValue()
    { return _SYSTEM_CONTEXT;
    }
  };
  
  /**
   * <p>Obtain the thread-local singleton instance of the ExecutionContext
   * </p>
   * 
   * @return
   */
  public static final ExecutionContext getInstance()
  { return _INSTANCE.get();
  }

  /**
   * <p>Set the current thread-local singleton instance of the ExecutionContext
   * </p>
   * 
   * <p>This instance must be reset by calling popInstance() when the operation
   *   is complete
   * </p>
   * 
   * @return
   */
  static final void pushInstance(ExecutionContext context)
  { _INSTANCE.push(context);
  }
  
  /**
   * <p>Reset the current thread-local singleton instance
   * </p>
   */
  static final void popInstance()
  { _INSTANCE.pop();
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

