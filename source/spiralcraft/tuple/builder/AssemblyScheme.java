package spiralcraft.tuple.builder;

import spiralcraft.stream.Resource;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import spiralcraft.tuple.Scheme;

import spiralcraft.tuple.spi.SchemeImpl;


/**
 * A Scheme instantiated from an Assembly.
 *
 * A deep copy is made of the Scheme contained in an Assembly. For ease of
 *   writing these Assemblies, it is assumed that the Scheme found in the 
 *   Assembly is not necessarily an efficient implmentation (typically
 *   the Scheme found in the Assembly will be a proxy implementation of
 *   the Scheme interfaces using Tuples).
 */
public class AssemblyScheme
  extends SchemeImpl
{
  
  public AssemblyScheme(Resource resource)
    throws BuildException
  { super(loadScheme(resource));
  }

  private static final Scheme loadScheme(Resource resource)
    throws BuildException
  {
    AssemblyClass assemblyClass
      =AssemblyLoader.getInstance().loadAssemblyDefinition(resource);
    
    Assembly assembly
      =assemblyClass.newInstance(null);
    
    return (Scheme) assembly.getSubject().get();
  }
}
