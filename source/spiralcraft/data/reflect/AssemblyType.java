package spiralcraft.data.reflect;

import java.net.URI;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.data.DataException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.builder.BuilderType;
import spiralcraft.data.core.SchemeImpl;
import spiralcraft.data.core.TypeImpl;
import spiralcraft.data.util.InstanceResolver;

public class AssemblyType<T>
  extends TypeImpl<T>
{

  private boolean linked;
  private boolean delegate;
  
  @SuppressWarnings("unchecked")
  public static <X> Type<X> canonicalType(AssemblyClass assemblyClass)
    throws DataException
  { 
    URI uri=null;
    Type<Assembly> builderType=BuilderType.canonicalType(assemblyClass);
    if (builderType!=null)
    { uri=TypeResolver.desuffix(builderType.getURI(),".assy");
    }
    if (uri==null)
    { 
      throw new DataException
        ("Can't resolve canonical type for "+assemblyClass);
    }
    return Type.resolve(assemblyClass.getContainerURI());
  }
  
  
  protected final AssemblyClass assemblyClass;

  
  @SuppressWarnings("unchecked")
  public AssemblyType
    (TypeResolver resolver
    ,URI uri
    ,AssemblyClass assemblyClass
    )
    throws DataException
  {
    super(resolver, uri);
    this.assemblyClass=assemblyClass;
    
      
    this.nativeClass=(Class<T>) assemblyClass.getJavaClass();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public T fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  {
    if (!(archetype instanceof AssemblyType))
    { return (T) archetype.fromData(data,new AssemblyResolver(resolver));
    }
    else
    { return new AssemblyResolver(resolver).resolve(this.nativeClass);
    }
  }
  
  @Override
  public boolean isAssignableFrom(Type<?> type)
  {
    if (delegate)
    { return archetype.isAssignableFrom(type);
    }
    else
    { 
      if (super.isAssignableFrom(type))
      { return true;
      }
      else
      { return archetype.isAssignableFrom(type);
      }
    }
  }
    
  @SuppressWarnings("unchecked")
  @Override
  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;
    pushLink(getURI());
    try
    {
      if (assemblyClass.getBaseClass()!=null)
      { this.archetype=AssemblyType.canonicalType(assemblyClass.getBaseClass());
      }
      else if (assemblyClass.getJavaClass()!=null)
      { 
        this.archetype
          =new ReflectionType<T>
            (resolver
            ,getURI()
            ,(Class<T>) assemblyClass.getJavaClass()
            ,(Class<T>) assemblyClass.getJavaClass()
            );
        delegate=true;
      }
      
      if (debug)
      { log.fine("archetype of "+getURI()+" is  "+archetype);
      }
      
      setScheme(new SchemeImpl());
      archetype.link();
      super.link();
    }
    finally
    { popLink();
    }
  }  
  
  class AssemblyResolver
    implements InstanceResolver
  {
    private final InstanceResolver delegate;
    
    public AssemblyResolver(InstanceResolver resolver)
    { this.delegate=resolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T resolve(
      Class<?> clazz)
      throws DataException
    {
      T oldVal=null;
      if (delegate!=null)
      {
        oldVal=(T) delegate.resolve(clazz);
      }
      
      Assembly<T> assembly=null;
      try
      {
        if (oldVal!=null)
        { 
          assembly=assemblyClass.wrap(null,oldVal);
          if (debug)
          { log.fine("Wrapping "+oldVal+" in "+assembly);
          }
        }
        else
        { 
          assembly=(Assembly<T>) assemblyClass.newInstance(null);
          if (debug)
          { log.fine("Instantiated "+assembly);
          }
        }
      }
      catch (BuildException x)
      { throw new DataException("Error constructing Assembly for "+getURI(),x);
      }
      return assembly.get();
    }
  }

}
