package spiralcraft.builder;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Context;
import spiralcraft.lang.Attribute;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleBinding;

import spiralcraft.tuple.lang.TupleDelegate;

import java.util.HashMap;

import spiralcraft.registry.RegistryNode;
import spiralcraft.registry.Registrant;

import java.lang.reflect.Proxy;

import spiralcraft.util.StringConverter;

/**
 * Assemblies are 'instances' of AssemblyClasses.
 */
public class Assembly
  implements Focus,Registrant
{
  private final AssemblyClass _assemblyClass;
  private Assembly _parent;
  private final Optic _optic;
  private PropertyBinding[] _propertyBindings;
  private HashMap<String,Assembly> _importedSingletons;
  private HashMap<Expression,Channel> _channels;
  private Context _context;
  private boolean bound=false;
  private boolean resolved=false;
  private boolean applied=false;
  
  /**
   * Construct an instance of the specified AssemblyClass,
   *   without binding properties
   */
  Assembly(AssemblyClass assemblyClass)
    throws BuildException
  {
    _assemblyClass=assemblyClass;
    try
    {
      Class javaClass=_assemblyClass.getJavaClass();
      if (javaClass==null)
      { throw new BuildException("No java class defined for assembly");
      }
      
      if (javaClass.isInterface())
      { _optic=new TupleDelegate(javaClass);
      }
      else 
      { 
        String constructor=_assemblyClass.getConstructor();
        Object instance;
        if (constructor!=null)
        { instance=construct(javaClass,constructor);
        }
        else
        { instance=javaClass.newInstance();
        }
        if (Context.class.isAssignableFrom(instance.getClass()))
        { _context=(Context) instance;
        }
        _optic=new SimpleBinding(instance,true);
      }

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
  }

  Object construct(Class clazz,String constructor)
    throws BuildException
  {
    StringConverter converter=StringConverter.getInstance(clazz);
    if (converter!=null)
    { return converter.fromString(constructor);
    }
    else
    { throw new BuildException("Can't construct a "+clazz+" from text"); 
    }
  }
  
  /**
   * Construct an instance of the specified AssemblyClass
   */
  Assembly(AssemblyClass assemblyClass,Assembly parent)
    throws BuildException
  { 
    this(assemblyClass);
    bind(parent);
  }

  /**
   *@return Whether this Assembly's properties have been bound
   */
  boolean isBound()
  { return bound;
  }
  
  void bind(Assembly parent)
    throws BuildException
  {
    if (bound)
    { throw new BuildException("Already bound properties");
    }
    bound=true;
    _parent=parent;
    _propertyBindings=_assemblyClass.bindProperties(this);
  }

  boolean isResolved()
  { return resolved;
  }
  
  void resolve()
    throws BuildException
  {
    if (resolved)
    { throw new BuildException("Already resolved");
    }
    resolved=true;
    
    if (_propertyBindings!=null)
    {
      for (PropertyBinding binding: _propertyBindings)
      { binding.resolve();
      }
    }
  }
  
  boolean isApplied()
  { return applied;
  }
  
  void applyProperties()
    throws BuildException
  {
    if (applied)
    { throw new BuildException("Already applied");
    }
    applied=true;
    if (_propertyBindings!=null)
    {
      for (PropertyBinding binding: _propertyBindings)
      { binding.applyProperties();
      }
    }
    
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

  /**
   * Descend the tree and write all persistent properties
   *   to their respective Tuple field.
   */
  public void storePersistentData()
  {
    for (int i=0;i<_propertyBindings.length;i++)
    { _propertyBindings[i].storePersistentData();
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
    if (_importedSingletons==null)
    { _importedSingletons=new HashMap<String,Assembly>();
    }
    for (int i=0;i<singletonInterfaces.length;i++)
    { _importedSingletons.put(singletonInterfaces[i].getName(),singleton);
    }
    
  }
  
  /**
   * Return the singleton interfaces exported by this
   *   Assembly. 
   *
   * Called by PropertyBinding when it exports singletons from a
   *   'child' assembly in a property up to its parent assembly.
   */
  public Class[] getSingletons()
  { 
    // XXX Should we export the imported singletons as well? We would have
    //   to change that interface to map Classes to objects as opposed to
    //   names in order to do this.
    return _assemblyClass.getSingletons();
  }

  public AssemblyClass getAssemblyClass()
  { return _assemblyClass;
  }

  /**
   *@return the Java object managed by this Assembly
   */
  public Object getObject()
  { return _optic.get();
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
   * implement Focus.getContext()
   */
  public Context getContext()
  { return _context;
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
    
    if (_importedSingletons!=null)
    { 
      Assembly assembly=_importedSingletons.get(name);
      if (assembly!=null)
      { return assembly;
      }
    }
    
    if (_assemblyClass.isFocusNamed(name))
    { return this;
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
    { _channels=new HashMap<Expression,Channel>();
    }
    else
    { channel=_channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      _channels.put(expression,channel);
    }
    return channel;
  }


}
