package spiralcraft.builder.test;


import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.builder.AssemblyFactory;
import spiralcraft.builder.AssemblyClass;


import spiralcraft.util.Arguments;

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
    new Arguments()
    {

      protected boolean processOption(String option)
      {
        if (option=="uri")
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
      AssemblyClass assemblyClass
        = AssemblyFactory.loadAssemblyDefinition(_uri);
      if (assemblyClass!=null)
      { System.err.println(assemblyClass.toString());
      }
      else
      { System.err.println("AssemblyClass is null");
      }
    }
    catch (Exception x)
    { System.err.println(x.toString());
    }
  }
}
