package spiralcraft.builder;

import spiralcraft.lang.Environment;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;

import spiralcraft.lang.optics.SimpleOptic;

import java.util.HashMap;

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
  private HashMap _singletons;


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

  public void registerSingletons(Class[] singletonInterfaces,Assembly singleton)
    throws BuildException
  { 
    if (_singletons==null)
    { _singletons=new HashMap();
    }
    for (int i=0;i<singletonInterfaces.length;i++)
    { _singletons.put(singletonInterfaces[i],singleton);
    }
    
  }

  public Class[] getSingletons()
  { return _assemblyClass.getSingletons();
  }

  public Focus findFocus(Class focusInterface)
  { 
    if (_assemblyClass.getJavaClass()==focusInterface)
    { return this;
    }
    
    if (_singletons!=null)
    { 
      Assembly focus=(Assembly) _singletons.get(focusInterface);
      if (focus!=null)
      { return focus;
      }
    }

    if (_parent!=null)
    { return _parent.findFocus(focusInterface);
    }
    
    return null;
  }
}
