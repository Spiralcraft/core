package spiralcraft.exec;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.util.ArrayUtil;

import spiralcraft.registry.Registry;
import spiralcraft.registry.RegistryNode;

/**
 * Loads and executes an Executable defined in an Assembly (spiralcraft.builder).
 *
 * The Executor is the root of the structural tree of the application, and thus
 *   registers itself as a child named 'executor' of the Registry root. 
 *
 * The Executor registers the executable as a Registry child of it's own named 'executable'.
 *   
 */
public class Executor
{
  private static RegistryNode _REGISTRY_ROOT
    =Registry.getLocalRoot().createChild("executor");

  public static final void main(String[] args)
    throws IOException
            ,URISyntaxException
            ,BuildException
  { 
    if (args.length<1)
    { throw new IllegalArgumentException("No Assembly URI specified");
    }
    new Executor().execute(args[0],(String[]) ArrayUtil.truncateBefore(args,1));
  }

  /**
   * Execute the executable found 
   */
  public void execute(String assemblyName,String[] args)
    throws IOException
            ,URISyntaxException
            ,BuildException
  {
    URI uri=new URI(assemblyName+".assembly.xml");
    AssemblyClass assemblyClass
      =AssemblyLoader.getInstance().findAssemblyDefinition(uri);

    if (assemblyClass!=null)
    { 
      Assembly assembly=assemblyClass.newInstance(null);
      assembly.register(_REGISTRY_ROOT.createChild("executable"));

      Executable executable=(Executable) assembly.getSubject().get();
      executable.execute(args);
    }
    else
    { throw new IOException("Assembly "+uri+" not found");
    }

  }
}
