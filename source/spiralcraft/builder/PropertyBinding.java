package spiralcraft.builder;

import spiralcraft.util.StringConverter;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Channel;

import spiralcraft.lang.BindException;

import java.util.Iterator;
import java.util.List;

import java.lang.reflect.Array;

/**
 * Associates a PropertySpecifier with a some value in the context of
 *   an instantiated Assembly
 */
public class PropertyBinding
{
  private Assembly _container;
  private PropertySpecifier _specifier;

  private Channel _target;
  private Optic _sourceOptic;
  private Object _source;

  private Assembly[] _contents;
  private StringConverter _converter;

  public PropertyBinding(PropertySpecifier specifier,Assembly container)
    throws BuildException
  { 
    _specifier=specifier;
    _container=container;

    instantiateContents();
    resolveTarget();
    resolveSource();
    apply();
  }

  private void instantiateContents()
    throws BuildException
  {
    List contents=_specifier.getContents();
    if (contents!=null)
    { 
      _contents=new Assembly[contents.size()];
      Iterator it=contents.iterator();
      int i=0;
      while (it.hasNext())
      { 
        AssemblyClass assemblyClass=(AssemblyClass) it.next();
        _contents[i++]=assemblyClass.newInstance(_container);
      }
    }
  }

  private void resolveTarget()
    throws BuildException
  {
    try    
    { _target=_specifier.getTargetExpression().createChannel(_container);
    }
    catch (BindException x)
    { 
      throw new BuildException
        ("Error binding "+_specifier.getTargetExpression().getText(),x);
    }
  }
  
  private void resolveSource()
    throws BuildException
  {
    if (_contents!=null && _contents.length>0)
    {
      if (_contents.length==1)
      { _source=_contents[0].getSubject().get();
      }
      else
      { 
        _source=Array.newInstance
          (_target.getTargetClass().getComponentType()
          ,_contents.length
          );
        for (int i=0;i<_contents.length;i++)
        { Array.set(_source,i,_contents[i].getSubject().get());
        }
      }
    }
    else
    {
      String text=_specifier.getTextData();
      _converter=StringConverter.getInstance(_target.getTargetClass());
      if (_converter==null)
      { 
        throw new BuildException
          ("No StringConverter registered for "
          +_target.getTargetClass().getName()
          );
      }
      _source=_converter.fromString(text);
    }
    
    /**
    Focus sourceFocus=container;

    Optic sourceOptic=null;        

    try
    {
      if (_sourceExpression!=null)
      { sourceOptic=_sourceExpression.createChannel(sourceFocus);
      }
    }
    catch (BindException x)
    { throw new BuildException("Error binding "+_sourceExpression.getText(),x);
    }

    return new PropertyBinding(targetOptic,sourceOptic);
    */
  }
  
  private void apply()
    throws BuildException
  { 
    if (!_target.set(_source))
    { throw new BuildException("Could not write "+_target.getExpression().getText());
    }
  }
}
