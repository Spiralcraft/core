package spiralcraft.builder.test;


import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;

import spiralcraft.util.Arguments;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

public class AssemblyFactoryTest
  implements Executable
{

  private URI _uri;
  private boolean _dump=false;
  private int _repeats=0;

  public void execute(final ExecutionContext context,String[] args)
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
        { _uri=context.canonicalize(URI.create(nextArgument()));
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
      
      Assembly assembly=assemblyClass.newInstance(null);
      if (assembly!=null)
      { System.err.println(assembly.toString());
      }
      else
      { System.err.println("Assembly is null");
      }
      
      Object o=assembly.getSubject().get();
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

  public void dump(PrintWriter writer,String linePrefix)
  {
  }

}
