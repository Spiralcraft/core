//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/** 
 * Allows multiple exceptions to be reported at once.
 * 
 * @author mike
 *
 */
public class MultiException
  extends Exception
{

  private static final long serialVersionUID = -3493588820930574955L;
  private Exception[] exceptions;
  
  public MultiException(String message,Exception[] exceptions)
  { 
    super(message);
    this.exceptions=exceptions;
  }
  
  public Exception[] getExceptions()
  { return exceptions;
  }

  @Override
  public void printStackTrace(PrintStream stream)
  {
    super.printStackTrace(stream);
    for (Exception exception:exceptions)
    { exception.printStackTrace(stream);
    }
  }

  @Override
  public void printStackTrace(PrintWriter writer)
  {
    super.printStackTrace(writer);
    for (Exception exception:exceptions)
    { exception.printStackTrace(writer);
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+ArrayUtil.format(exceptions,",","[]");
  }
  
}
