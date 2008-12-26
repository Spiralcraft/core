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
package spiralcraft.builder.test;


import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;

import spiralcraft.util.Arguments;


import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

/**
 * Test driver for builder
 */
public class AssemblyFactoryTest
  implements Executable
{

  private URI _uri;
  // private boolean _dump=false;
  // private int _repeats=0;

  public void execute(String ... args)
  {
    final ExecutionContext context=ExecutionContext.getInstance();
    try
    { 
      _uri=
        new URI
          ("class:/spiralcraft/builder/test/test1.assy.xml"
          );
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x.toString());
    }

    new Arguments()
    {

      @Override
      protected boolean processOption(String option)
      {
        if (option=="uri")
        { _uri=context.canonicalize(URI.create(nextArgument()));
        }
        else if (option=="repeats")
        { // _repeats=Integer.parseInt(nextArgument());
        }
        else
        { return false;
        }
        return true;
      }
    }.process(args,'-');

     
    try
    {
      AssemblyClass assemblyClass
        = AssemblyLoader.getInstance().findAssemblyDefinition(_uri);
      if (assemblyClass!=null)
      { System.err.println(assemblyClass.toString());
      }
      else
      { System.err.println("AssemblyClass is null");
      }
      
      Assembly<?> assembly=assemblyClass.newInstance(null);
      if (assembly!=null)
      { System.err.println(assembly.toString());
      }
      else
      { System.err.println("Assembly is null");
      }
      
      Object o=assembly.get();
      if (o!=null)
      { System.err.println(o.toString());
      }
      else
      { System.err.println("Subject is null");
      }
    }
    catch (Exception x)
    { x.printStackTrace();
    }
  }


}
