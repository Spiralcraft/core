package spiralcraft.tuple.builder;

import spiralcraft.stream.Resource;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;

import spiralcraft.tuple.Scheme;

import spiralcraft.tuple.spi.SchemeImpl;


/**
 * A Scheme constructed by using an AssemblyClass definition to construct
 *   data Objects out of the Scheme, Field, and Type interfaces.
 *
 * AssemblyClasses can instantiate interfaces as well as classes. When an
 *   interface is instantiated, a proxy object is created which implements
 *   the interface and implements bean accessors using Tuple storage.
 *
 * A deep copy is made of the Scheme contained in an Assembly. For ease of
 *   writing these Assemblies, it is assumed that the Scheme found in the 
 *   Assembly is not necessarily an efficient implmentation (typically
 *   the Scheme found in the Assembly will be a proxy implementation of
 *   the Scheme interface and related interfaces).
 *
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
