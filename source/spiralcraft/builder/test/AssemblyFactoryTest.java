package spiralcraft.builder.test;


import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.AssemblyClass;


import spiralcraft.util.Arguments;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class AssemblyFactoryTest
{

  private URI _uri;
  private boolean _dump=false;
  private int _repeats=0;

  public static void main(String[] args)
  { new AssemblyFactoryTest().run(args);
  }
  
  public void run(String[] args)
  {
    try
    { 
      _uri=
        new URI
          ("java:/spiralcraft/builder/test/test1.assembly.xml"
          );
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x.toString());
    }

    new Arguments()
    {

      protected boolean processOption(String option)
      {
        if (option=="uri")
        { _uri=_uri.resolve(nextArgument());
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
    }
    catch (Exception x)
    { x.printStackTrace();
    }
  }

  public void dump(PrintWriter writer,String linePrefix)
  {
  }

}
