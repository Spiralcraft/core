package spiralcraft.builder;

/**
 * Assemblies are 'instances' of AssemblyClasses. 
 */
public class Assembly
{
  private final AssemblyClass _assemblyClass;
  private Object _object;

  /**
   * Construct an instance of the specified AssemblyClass
   */
  Assembly(AssemblyClass assemblyClass)
    throws InstantiationException,ClassNotFoundException,IllegalAccessException
  { 
    _assemblyClass=assemblyClass;
    Class javaClass=_assemblyClass.getJavaClass();
    if (javaClass==null)
    { throw new ClassNotFoundException("No java class defined for assembly");
    }
    _object=javaClass.newInstance();
  }

  public Object getObject()
  { return _object;
  }
}
