package spiralcraft.builder;

import spiralcraft.lang.Environment;
import spiralcraft.lang.Attribute;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleOptic;

import java.util.HashMap;

/**
 * Assemblies are 'instances' of AssemblyClasses. 
 */
public class Assembly
  implements Focus,Environment
{
  private final AssemblyClass _assemblyClass;
  private final Assembly _parent;
  private final Optic _optic;
  private final PropertyBinding[] _propertyBindings;
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
      
      Object instance=javaClass.newInstance();
      
      _optic=OpticFactory.decorate
        (new SimpleOptic(instance)
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

  public Assembly getParent()
  { return _parent;
  }

  public Focus getParentFocus()
  { return _parent;
  }

  /**
   * Focus.getEnvironment()
   */
  public Environment getEnvironment()
  { return this;
  }

  /**
   * Focus.getSubject()
   */
  public Optic getSubject()
  { return _optic;
  }
 
  /**
   * Focus.findFocus()
   */
  public Focus findFocus(String name)
  { 
    if (_assemblyClass.getJavaClass().getName().equals(name))
    { return this;
    }

    if (_singletons!=null)
    { 
      Assembly assembly=(Assembly) _singletons.get(name);
      if (assembly!=null)
      { return assembly;
      }
    }

    if (_parent!=null)
    { return _parent.findFocus(name);
    }
    
    return null;

  }

  public void registerSingletons(Class[] singletonInterfaces,Assembly singleton)
    throws BuildException
  { 
    if (_singletons==null)
    { _singletons=new HashMap();
    }
    for (int i=0;i<singletonInterfaces.length;i++)
    { _singletons.put(singletonInterfaces[i].getName(),singleton);
    }
    
  }

  public Class[] getSingletons()
  { return _assemblyClass.getSingletons();
  }

  public AssemblyClass getAssemblyClass()
  { return _assemblyClass;
  }

  /**
   * Environment.resolve()
   */
  public Optic resolve(String name)
  { 
    try
    { return _optic.resolve(this,name,null);
    }
    catch (BindException x)
    { x.printStackTrace();
    }
    return null;
  }

  /**
   * Environment.resolve()
   */
  public Attribute[] getAttributes()
  { 
    // XXX Translate Optic names into attributes
    return null;
  }

}
