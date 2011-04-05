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

import java.io.InputStream;
import java.io.PrintStream;

import java.net.URI;
import java.util.HashMap;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>Provides contextual information for specific invocations of user driven
 *   entry points to decouple coarse-grained application logic from using
 *   java.lang.System as a "global" context.
 * </p>
 */
public class ExecutionContext
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
  public static final void pushInstance(ExecutionContext context)
  { _INSTANCE.push(context);
  }
  
  /**
   * <p>Reset the current thread-local singleton instance
   * </p>
   */
  public static final void popInstance()
  { _INSTANCE.pop();
  }
  
  private final ExecutionContext parent;
  private final PrintStream out;
  private final PrintStream err;
  private final InputStream in;
  private final URI focusURI;
  
  
  public ExecutionContext
    (ExecutionContext parent,HashMap<String,Object> contextMap)
  { 
    this.parent=parent;
    this.out=(PrintStream) contextMap.get("out");
    this.err=(PrintStream) contextMap.get("err");
    this.in=(InputStream) contextMap.get("in");
    this.focusURI=(URI) contextMap.get("focusURI");
  }
  
  public ExecutionContext(ExecutionContext parent)
  { 
    this.parent=parent;
    this.out=null;
    this.err=null;
    this.in=null;
    this.focusURI=null;
  }
  
  
  public PrintStream out()
  { return out!=null?out:parent.out();
  }
  
  public InputStream in()
  { return in!=null?in:parent.in();
  }

  public PrintStream err()
  { return err!=null?err:parent.err();
  }
  
  
  /**
   * Return the user focus URI- the URI equivalent of the "current directory"
   *   in a file system
   */
  public URI focusURI()
  { return focusURI!=null?focusURI:parent.focusURI();
  }
  
  /**
   * Convert a relative or context-mapped URI to an absolute URI. Other
   *   URIs are passed through unchanged.
   */
  public URI canonicalize(URI relativeURI)
  {
    if (!relativeURI.isAbsolute())
    { return focusURI().resolve(relativeURI).normalize();
    }
    else
    { return relativeURI;
    }
  }
}

