package spiralcraft.stream.test;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;
import spiralcraft.stream.StreamUtil;

import spiralcraft.util.Arguments;

public class ResolverTest
{

  private URI _uri;
  private File _file;
  private boolean _dump=false;
  private int _repeats=0;

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
        { _repeats=Integer.parseInt(nextArgument());
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
