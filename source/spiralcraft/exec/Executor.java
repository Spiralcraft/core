package spiralcraft.exec;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.util.ArrayUtil;

/**
 * Loads and executes an Executable defined in an Assembly (spiralcraft.builder)
 */
public class Executor
{
  public static final void main(String[] args)
    throws IOException
            ,URISyntaxException
            ,BuildException
  { new Executor().exec(args);
  }

  public void exec(String[] args)
    throws IOException
            ,URISyntaxException
            ,BuildException
  {
    URI uri=new URI(args[0]+".assembly.xml");
    AssemblyClass assemblyClass
      =AssemblyLoader.getInstance().findAssemblyDefinition(uri);

    if (assemblyClass!=null)
    { 
      Assembly assembly=assemblyClass.newInstance(null);
      Executable executable=(Executable) assembly.getSubject().get();
      executable.exec((String[]) ArrayUtil.truncateBefore(args,1));
    }
    else
    { throw new IOException("Assembly "+uri+" not found");
    }

  }
}
