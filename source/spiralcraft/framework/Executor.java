package spiralcraft.framework;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;

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
            ,InstantiationException
            ,ClassNotFoundException
            ,IllegalAccessException
  { new Executor().exec(args);
  }

  public void exec(String[] args)
    throws IOException
            ,URISyntaxException
            ,InstantiationException
            ,ClassNotFoundException
            ,IllegalAccessException
  {
    URI uri=new URI(args[0]);    
    AssemblyClass assemblyClass
      =AssemblyLoader.getInstance().findAssemblyDefinition(uri);

    if (assemblyClass!=null)
    { 
      Assembly assembly=assemblyClass.newInstance();
      Executable executable=(Executable) assembly.getObject();
      executable.exec((String[]) ArrayUtil.truncateBefore(args,1));
    }
    else
    { throw new IOException("Assembly "+uri+" not found");
    }

  }
}
