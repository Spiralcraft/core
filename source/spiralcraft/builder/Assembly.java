package spiralcraft.builder;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Environment;
import spiralcraft.lang.Attribute;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleOptic;
import spiralcraft.lang.optics.SimpleBinding;

import java.util.HashMap;

import spiralcraft.registry.RegistryNode;
import spiralcraft.registry.Registrant;

/**
 * Assemblies are 'instances' of AssemblyClasses.
 */
public class Assembly
  implements Focus,Environment,Registrant
{
  private final AssemblyClass _assemblyClass;
  private final Assembly _parent;
  private final Optic _optic;
  private final PropertyBinding[] _propertyBindings;
  private HashMap _singletons;
  private HashMap _channels;


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
      
      _optic=new SimpleOptic(new SimpleBinding(instance,true));
    }
    catch (InstantiationException x)
    { throw new BuildException("Error instantiating assembly",x);
    }
    catch (IllegalAccessException x)
    { throw new BuildException("Error instantiating assembly",x);
    }
    catch (BindException x)
    { throw new BuildException("Error binding instance",x);
    }
    
    _propertyBindings=_assemblyClass.bindProperties(this);
    
  }

  /**
   * Descend the tree and write all preference properties to
   *   their respective Preferences node.
   */
  public void savePreferences()
  {
    for (int i=0;i<_propertyBindings.length;i++)
    { _propertyBindings[i].savePreferences();
    }
  }

  public void register(RegistryNode node)
  {
    node.registerInstance(Assembly.class,this);

    Object instance=_optic.get();
    if (instance instanceof Registrant)
    { ((Registrant) instance).register(node);
    }
    node.registerInstance(Object.class,instance);

    for (int i=0;i<_propertyBindings.length;i++)
    { _propertyBindings[i].register(node);
    }
  }

  /**
   * Return the Assembly which contains this one
   */
  public Assembly getParent()
  { return _parent;
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

  //////////////////////////////////////////////////  
  //
  // Implementation of spiralcraft.lang.Focus
  //
  //////////////////////////////////////////////////  

  /**
   * implement Focus.getParentFocus()
   */
  public Focus getParentFocus()
  { return _parent;
  }

  /**
   * implement Focus.getEnvironment()
   */
  public Environment getEnvironment()
  { return this;
  }

  /**
   * implement Focus.getSubject()
   */
  public Optic getSubject()
  { return _optic;
  }
 
  /**
   * implement Focus.findFocus()
   */
  public Focus findFocus(String name)
  { 
    if (_assemblyClass.isFocusNamed(name))
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

  /**
   * implement Focus.bind()
   */
  public synchronized Channel bind(Expression expression)
    throws BindException
  { 
    Channel channel=null;
    if (_channels==null)
    { _channels=new HashMap();
    }
    else
    { channel=(Channel) _channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      _channels.put(expression,channel);
    }
    return channel;
  }

  //////////////////////////////////////////////////
  //
  // Implementation of spiralcraft.lang.Environment
  //
  //////////////////////////////////////////////////  

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
  public String[] getNames()
  { 
    // XXX Get the names in the optic
    return null;
  }

}
