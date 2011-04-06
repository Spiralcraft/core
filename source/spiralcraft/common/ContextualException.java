//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.common;

/**
 * <p>Common base class for Exceptions to capture information about context
 *   at a higher level than Java code traces.
 * </p>
 * 
 * @author mike
 *
 */
public class ContextualException
  extends Exception
{
  private static final long serialVersionUID=1;
  private Object context;
  
  public ContextualException(String message)
  { super(message);
  }

  public ContextualException(String message,Object context)
  {
    super(message);
    this.context=context;
  }

  public ContextualException(String message,Throwable cause)
  { super(message,cause);
  }
  
  public ContextualException(String message,Object context,Throwable cause)
  { 
    super(message,cause);
    this.context=context;
  }

  public Object getContext()
  { return context;
  }
  
  public Throwable getRootCause()
  {
    Throwable cause=this;
    while (cause.getCause()!=null)
    { cause=cause.getCause();
    }
    return cause!=this?cause:null;
  }
  
  public Object getRootContext()
  {
    Throwable cause=this;
    Object rootContext=null;
    while (cause.getCause()!=null)
    { 
      cause=cause.getCause();
      if (cause instanceof ContextualException)
      { 
        Object newRootContext=((ContextualException) cause).getContext();
        if (newRootContext!=null)
        { rootContext=newRootContext;
        }
      }
    }
    return rootContext;
  }
  
  @Override
  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(super.toString());

    if (context!=null)
    { buf.append(" (context: "+context.toString());
    }
    
//    Throwable rootCause=getRootCause();
//    if (rootCause!=null)
//    { buf.append(" (root cause: "+getRootCause()+")");
//    }
//
//    Object rootContext=getRootContext();
//    if (rootContext!=null)
//    { buf.append(" (root context: "+context.toString());
//    }
    
    return buf.toString();
  }

}
