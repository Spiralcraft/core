package spiralcraft.stream.batch;

import java.net.URI;

import spiralcraft.util.Arguments;

import spiralcraft.stream.Resource;
import spiralcraft.stream.Resolver;
import spiralcraft.stream.UnresolvableURIException;
import spiralcraft.stream.StreamUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


public class PrependOperation
  implements Operation
{
  private URI _contentUri;
  private Operation _nextOperation;
  
  public void setNextOperation(Operation next)
  { _nextOperation=next;
  }
  
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
    }
    catch (UnresolvableURIException x)
    { throw new OperationException(this,"Content "+_contentUri+" not resolvable.",x);
    }
    catch (IOException x)
    { throw new OperationException(this,"IOException",x);
    }
  }
  
  public boolean processOption(Arguments args,String option)
  { 
    if (option=="content")
    { _contentUri=URI.create(args.nextArgument());
    }
    else
    { return false;
    }
    return true;
  }
  
  public boolean processArgument(Arguments args,String argument)
  { return false;
  }
}
