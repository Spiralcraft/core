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

