package spiralcraft.builder;

import spiralcraft.util.StringConverter;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;

import spiralcraft.lang.BindException;

import java.util.Iterator;
import java.util.List;

import java.lang.reflect.Array;


import spiralcraft.registry.RegistryNode;

import java.util.prefs.Preferences;

/**
 * Associates a PropertySpecifier with a some value in the context of
 *   an instantiated Assembly
 *
 * If the property is indicated as being a 'preference' to be loaded from 
 *   and saved to a Preferences object, one of the following cases apply:
 *
 * If the target class is a String, the value is transferred literally
 * If the target class can be associated with a StringConverter, the 
 *    value is run through the converter before loading and saving.
 * If the Preferences object holds a persistent reference, the reference will
 *    be instantiated and applied to the property, and will be flushed
 *    when the preferences are saved
 * If the Preferences object holds an XmlEncoded object, the object will
 *    be decoded and encoded on load/save.
 */
public class PropertyBinding
{
  private Assembly _container;
  private PropertySpecifier _specifier;

  private Optic _target;
  private Optic _sourceOptic;
  private Focus _focus;
  private Object _source;

  private Assembly[] _contents;
  private StringConverter _converter;
  private RegistryNode _registryNode;

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

  public void savePreferences()
  {
    if (_contents!=null && _contents.length>0)
    { 
      for (int i=0;i<_contents.length;i++)
      { _contents[i].savePreferences();
      }
    }
    else if (_specifier.isPreference())
    { 
      Preferences preferences=(Preferences) _registryNode.findInstance(Preferences.class);
      if (preferences!=null)
      {
        Object value=_target.get();
        if (value==null)
        { preferences.remove(_specifier.getTargetName());
        }
        else if (_target.getTargetClass().isAssignableFrom(String.class))
        { preferences.put(_specifier.getTargetName(),(String) value);
        }
        else if (_converter!=null)
        { preferences.put(_specifier.getTargetName(),_converter.toString(value));
        }
        else
        { 
          String xml=StringConverter.encodeToXml(value);
          if (xml!=null && xml.length()>0)
          { preferences.put(_specifier.getTargetName(),xml);
          }
          else
          {
            System.err.println
              ("Can't convert preference value '"
              +_specifier.getTargetName()
              +"' ("+_target.getTargetClass()+") to a String");
          }
        }
      }
    }
  }

  public void register(RegistryNode node)
  {
    _registryNode=node;
    if (_contents!=null && _contents.length>0)
    { 
      RegistryNode propertyNode=node.createChild(_specifier.getTargetName());
      if (!_target.getTargetClass().isArray())
      { _contents[0].register(propertyNode);
      }
      else
      {
        for (int i=0;i<_contents.length;i++)
        { 
          _contents[i].register
            (propertyNode.createChild(Integer.toString(i))
            );
        }
      }
    }
    else if (_specifier.isPreference())
    { 
      Preferences preferences=(Preferences) node.findInstance(Preferences.class);
      if (preferences!=null)
      { applyPreferences(preferences);
      }
    }
  }

  public void applyPreferences(Preferences preferences)
  {
    String value=preferences.get(_specifier.getTargetName(),null);
    
    if (value!=null)
    {
      if (_target.getTargetClass().isAssignableFrom(String.class))
      { applySafe(value);
      }
      else if (_converter!=null)
      { applySafe(_converter.fromString(value));
      }
      else
      { 
        Object ovalue=null;
        try
        { ovalue=StringConverter.decodeFromXml(value);
        }
        catch (Exception x)
        { x.printStackTrace();
        }
        if (ovalue!=null)
        { applySafe(ovalue);
        }
        else
        {
          System.err.println
            ("Can't convert preference value '"
            +value
            +"' to "
            +_target.getTargetClass()
            );
        }
      }
    }
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
    _target=_container.resolve(_specifier.getTargetName());
    if (_target==null)
    {
      throw new BuildException
        ("Property '"+_specifier.getTargetName()+"' not found"
        +" ("+_specifier.getSourceCodeLocation()+")"
        );
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
            (_specifier.getTargetName()
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
        Channel sourceChannel=_focus.bind(_specifier.getSourceExpression());
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
    try
    {
      if (_source!=null && !_target.set(_source))
      { throwBuildException("Could not write "+_specifier.getTargetName(),null);
      }
    }
    catch (RuntimeException x)
    { throwBuildException(x.toString()+" writing "+_specifier.getTargetName(),x);
    }
  }

  private void throwBuildException(String message,Exception source)
    throws BuildException
  { throw new BuildException(message+" ("+_specifier.getSourceCodeLocation()+")",source);
  }

  private void applySafe(Object value)
  {
    Object osource=_source;
    _source=value;
    try
    { apply();
    }
    catch (Exception x)
    { 
      x.printStackTrace();
      _source=osource;
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
