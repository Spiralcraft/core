package spiralcraft.builder;

import spiralcraft.util.StringConverter;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
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
  private Focus _focus;
  private Object _source;

  private Assembly[] _contents;
  private StringConverter _converter;

  public PropertyBinding(PropertySpecifier specifier,Assembly container)
    throws BuildException
  { 
    _specifier=specifier;
    _container=container;
    _focus=container;

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
      if (_contents.length==1 && !_target.getTargetClass().isArray())
      { 
        _source=_contents[0].getSubject().get();
        registerSingletons(_contents[0]);
      }
      else
      { 
        if (!_target.getTargetClass().isArray())
        { 
          throw new BuildException
            (_target.getExpression().getText()
            +" in "+_container.getAssemblyClass().getJavaClass()
            +" cannot have multiple values"
            );
        }
        
        _source=Array.newInstance
          (_target.getTargetClass().getComponentType()
          ,_contents.length
          );
        for (int i=0;i<_contents.length;i++)
        { 
          Array.set(_source,i,_contents[i].getSubject().get());
          registerSingletons(_contents[i]);
        }
      }
    }
    else if (_specifier.getTextData()!=null)
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
    else if (_specifier.getFocus()!=null)
    { 
      
      Class focusInterface;
      if (_specifier.getFocus().equals("auto"))
      { focusInterface=_target.getTargetClass();
      }
      else
      { 
        try
        {
          focusInterface
            =Class.forName
              (_specifier.getFocus()
              ,false
              ,Thread.currentThread().getContextClassLoader()
              );
        }
        catch (ClassNotFoundException x)
        { throw new BuildException("Unknown Focus interface",x);
        }
      }

      _focus=_container.findFocus(focusInterface.getName());
      if (_focus==_container && _container.getParent()!=null)
      { _focus=_container.getParent().findFocus(focusInterface.getName());
      }

      if (_focus==null)
      { throw new BuildException("Singleton "+focusInterface.getName()+" not found in this Assembly or its ancestors");
      }
    }
    
    if (_specifier.getSourceExpression()!=null)
    {
      try
      {
        Channel sourceChannel=_specifier.getSourceExpression().createChannel(_focus);
        _source=sourceChannel.get();
      }
      catch (BindException x)
      { 
        throw new BuildException
          ("Error binding "+_specifier.getSourceExpression().getText(),x);
      }
      
    }
  }
  
  private void apply()
    throws BuildException
  { 
    if (!_target.set(_source))
    { throw new BuildException("Could not write "+_target.getExpression().getText());
    }
  }

  private void registerSingletons(Assembly source)
    throws BuildException
  {
    Class[] interfaces=source.getSingletons();
    if (interfaces!=null)
    { _container.registerSingletons(interfaces,source);
    }
    
  }
}
