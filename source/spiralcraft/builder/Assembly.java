package spiralcraft.builder;

/**
 * Assemblies are 'instances' of AssemblyClasses. 
 */
public class Assembly
{
  private final AssemblyClass _assemblyClass;

  /**
   * Construct an instance of the specified AssemblyClass
   */
  Assembly(AssemblyClass assemblyClass)
  { _assemblyClass=assemblyClass;
  }

}
