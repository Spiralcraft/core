package spiralcraft.builder;

/**
 * Specifies a property to be defined in the context of an AssemblyClass
 *
 * The 'specifier' of the property is a name expression (spiralcraft.lang)
 *   evaluated in the context of the containing Assembly which identifies
 *   a property in the containing Assembly or SubAssemblies that is to
 *   be assigned a value or modified in some way.
 *   
 */
public class PropertySpecifier
{
  private final AssemblyClass _assemblyClass;
  private final String _specifier;
  
  public PropertySpecifier(AssemblyClass assemblyClass,String specifier)
  {
    _assemblyClass=assemblyClass;
    _specifier=specifier;
  }

  public String toString()
  { return super.toString()+"[specifier="+_specifier+"]";
  }

}
