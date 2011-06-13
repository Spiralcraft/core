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
package spiralcraft.vfs.batch;

import java.net.URI;

import spiralcraft.cli.Arguments;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.UnresolvableURIException;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;


public class PrependOperation
  implements Operation
{
  private URI _contentUri;
  private Operation _nextOperation;
  
  @Override
  public void setNextOperation(Operation next)
  { _nextOperation=next;
  }
  
  @Override
  public void invoke(Resource resource)
    throws OperationException
  { 
    if (_contentUri==null)
    { throw new OperationException(this,"No content to prepend");
    }
    
    Resource contentResource=null;
    try
    {
      contentResource=Resolver.getInstance().resolve(_contentUri);
      if (contentResource==null)
      { throw new OperationException(this,"Content "+_contentUri+" not found.");
      }

      InputStream contentIn=contentResource.getInputStream();
      
      byte[] contentBytes=StreamUtil.readBytes(contentIn);
      contentIn.close();
      
      InputStream resourceIn=resource.getInputStream();
      byte[] resourceBytes=StreamUtil.readBytes(resourceIn);
      resourceIn.close();
      
      OutputStream resourceOut=resource.getOutputStream();
      resourceOut.write(contentBytes);
      resourceOut.write(resourceBytes);
      resourceOut.flush();
      resourceOut.close();
      
      if (_nextOperation!=null)
      { _nextOperation.invoke(resource);
      }
    }
    catch (UnresolvableURIException x)
    { throw new OperationException(this,"Content "+_contentUri+" not resolvable.",x);
    }
    catch (IOException x)
    { throw new OperationException(this,"IOException",x);
    }
  }
  
  @Override
  public boolean processOption(Arguments args,String option)
  { 
    if (option.equals("content"))
    { 
      _contentUri=URI.create(args.nextArgument());
      if (!_contentUri.isAbsolute())
      {  
        // XXX Get user context
        _contentUri=new File(new File(".").getAbsolutePath()).toURI().resolve(_contentUri);
      }
    }
    else
    { return false;
    }
    return true;
  }
  
  @Override
  public boolean processArgument(Arguments args,String argument)
  { return false;
  }
}
