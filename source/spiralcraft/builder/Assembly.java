package spiralcraft.builder;

import spiralcraft.lang.Environment;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;

import spiralcraft.lang.optics.SimpleOptic;


/**
 * Assemblies are 'instances' of AssemblyClasses. 
 */
public class Assembly
  implements Focus,Environment
{
  private final AssemblyClass _assemblyClass;
  private Assembly _parent;
  private Optic _optic;
  private PropertyBinding[] _propertyBindings;

  /**
   * Construct an instance of the specified AssemblyClass
   */
  Assembly(AssemblyClass assemblyClass,Assembly parent)
    throws BuildException
  { 
    _assemblyClass=assemblyClass;
    _parent=parent;

    try
    {
      Class javaClass=_assemblyClass.getJavaClass();
      if (javaClass==null)
      { throw new BuildException("No java class defined for assembly");
      }
      
  
      _optic=OpticFactory.decorate
        (new SimpleOptic(javaClass.newInstance())
        );
    }
    catch (InstantiationException x)
    { throw new BuildException("Error instantiating assembly",x);
    }
    catch (IllegalAccessException x)
    { throw new BuildException("Error instantiating assembly",x);
    }
    
    _propertyBindings=_assemblyClass.bindProperties(this);
  }

  /**
   * Instantiate contained assemblies
   */
  private void instantiateMetaStructure()
    throws BuildException
  {
    
  }

  public Environment getEnvironment()
  { return this;
  }

  public Optic getSubject()
  { return _optic;
  }
 
  public Optic resolve(String name)
  { return _optic.resolve(this,name,null);
  }
}
