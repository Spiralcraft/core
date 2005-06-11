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

import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;

import java.net.URI;

public class SystemExecutionContext
  extends ExecutionContext
{
  public SystemExecutionContext()
  { ExecutionContext.setInstance(this);
  }
  
  public PrintStream out()
  { return System.out;
  }

  public InputStream in()
  { return System.in;
  }
  
  public PrintStream err()
  { return System.err;
  }
  
  public URI focusURI()
  { 
    return 
      new File(new File(System.getProperty("user.dir")).getAbsolutePath())
        .toURI();
  }
  
  
}
