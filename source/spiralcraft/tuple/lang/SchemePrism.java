package spiralcraft.tuple.lang;

import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Binding;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Field;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class SchemePrism
  implements Prism
{
  private static final HashMap _SINGLETONS
    =new HashMap();
  
  private final Scheme _scheme;
  private final HashMap _fields=new HashMap();
  private HashMap _fieldLenses;

  public synchronized static final SchemePrism getInstance(Scheme scheme)
  { 
    SchemePrism prism=(SchemePrism) _SINGLETONS.get(scheme);
    if (prism==null)
    {
      prism=new SchemePrism(scheme);
      _SINGLETONS.put(scheme,prism);
    }
    return prism;
  }
  
  SchemePrism(Scheme scheme)
  { 
    _scheme=scheme;
    Iterator it=_scheme.getFields().iterator();
    while (it.hasNext())
    {
      Field field=(Field) it.next();
      _fields.put(field.getName(),field);
    }
  }

  public Field findField(String name)
  { return (Field) _fields.get(name);
  }
  
  public Scheme getScheme()
  { return _scheme;
  }

  public synchronized Binding resolve
    (Binding source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {
    FieldLense lense=null;
    if (_fieldLenses==null)
    { _fieldLenses=new HashMap();
    }
    else
    { lense=(FieldLense) _fieldLenses.get(name);
    }
    
    if (lense==null)
    {
      Field field
        =findField(name);

      if (field!=null)
      { 
        lense=new FieldLense(field);
        _fieldLenses.put(name,lense);
      }
      
    }
    
    if (lense!=null)
    {
      Binding binding=source.getCache().get(lense);
      if (binding==null)
      { 
        binding=new FieldBinding(source,lense);
        source.getCache().put(lense,binding);
      }
      return binding;      
    }
    
    return null;
  }

  public Decorator decorate(Binding binding,Class decoratorInterface)
  { 
    // This depends on a system for registering and mapping decorators
    //   to Tuple constructs.
    return null;
  }
  
  public Class getContentType()
  { return Tuple.class;
  }
  
}
