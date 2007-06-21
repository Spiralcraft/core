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
package spiralcraft.vfs.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.net.URISyntaxException;


import spiralcraft.util.Arguments;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;

public class ResolverTest
{

  private URI _uri;
  private File _file;
  // private boolean _dump=false;
  // private int _repeats=0;

  public static void main(String[] args)
  { new ResolverTest().run(args);
  }
  
  public void run(String[] args)
  {
    new Arguments()
    {

      protected boolean processOption(String option)
      {
        if (option=="file")
        { _file=new File(nextArgument());
        }
        else if (option=="uri")
        { 
          try
          { _uri=new URI(nextArgument());
          }
          catch (URISyntaxException x)
          { throw new IllegalArgumentException(x.toString());
          }
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

    if (_uri==null)
    { 
      System.err.println("uri cannot be null");
      return;
    }
      
    try
    {
      Resource resource = Resolver.getInstance().resolve(_uri);
      if (resource!=null)
      { 
        System.err.println(resource.toString());
        InputStream in = resource.getInputStream();
        if (in!=null)
        {
          OutputStream out;
          if (_file!=null)
          { out = new FileOutputStream(_file,true);
          }
          else
          { out=System.out;
          }
          StreamUtil.copyRaw(in,out,8192);
          out.flush();
          in.close();
          out.close();
        }
        else
        { System.err.println("InputStream was null");
        }
      }
      else
      { 
        System.err.println("Resource not found");
      }
    }
    catch (Exception x)
    { System.err.println(x.toString());
    }
  }
}
