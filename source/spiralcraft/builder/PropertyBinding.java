package spiralcraft.builder;

import spiralcraft.util.StringConverter;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;

import spiralcraft.lang.BindException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.lang.reflect.Array;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

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
  implements PropertyChangeListener
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
    else if (_specifier.isPersistent())
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
      // Sub-assemblies (contents) are specified in definition.
      // Register all sub-assemblies 
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
    else if (_specifier.isPersistent())
    { 
      // Contents are not specified in definition, and property has been tagged
      //   as being a preference. Read property valye from preferences.
      Preferences preferences=(Preferences) node.findInstance(Preferences.class);
      if (preferences!=null)
      { applyPreferences(preferences);
      }
    }
  }

  /**
   * Read the value for this property from the preferences object and
   *   apply the value.
   */
  public void applyPreferences(Preferences preferences)
  {
    if (_target.getTargetClass().isArray())
    { applyArrayPreferences(preferences);
    }
    else
    { applySinglePreferences(preferences);
    }
  }

  /**
   * Array preferences are represented in the store in to form &lt;name&gt;.&lt;arrayIndex&gt;
   */
  private void applyArrayPreferences(Preferences preferences)
  {
    Class componentType=_target.getTargetClass().getComponentType();
    if (_converter==null)
    { _converter=StringConverter.getInstance(componentType);
    }

    List prefs=new ArrayList();
    String value;
    int count=0;

    while ( (value=preferences.get
              (_specifier.getTargetName()+"."+Integer.toString(count++)
              ,null
              )
            )
            !=null
          )
    { 
      if (componentType.isAssignableFrom(String.class))
      { prefs.add(value);
      }
      else if (_converter!=null)
      { prefs.add(_converter.fromString(value));
      }
      else
      {
        System.err.println
          ("Can't convert preference value '"
          +value
          +"' to "
          +componentType
          );
      }

    }
    Object targetValue=Array.newInstance(componentType,prefs.size());
    for (int i=0;i<prefs.size();i++)
    { Array.set(targetValue,i,prefs.get(i));
    }
    applySafe(targetValue);
  }
  
  public void applySinglePreferences(Preferences preferences)
  {
    if (_converter==null)
    { _converter=StringConverter.getInstance(_target.getTargetClass());
    }
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

  /**
   * Indicate whether the target is an aggregate type
   */
  private boolean isAggregate()
  { 
    return _target.getTargetClass().isArray()
      || Collection.class.isAssignableFrom(_target.getTargetClass());
  }
  
  private void resolveSource()
    throws BuildException
  {
    if (_contents!=null && _contents.length>0)
    {
      if (!isAggregate())
      {
        // Source is a single object
        if (_contents.length==1)
        { 
          _source=_contents[0].getSubject().get();
          registerSingletons(_contents[0]);
        }
        else
        {
          throw new BuildException
            (_specifier.getTargetName()
            +" in "+_container.getAssemblyClass().getJavaClass()
            +" cannot have multiple values"
            );
        }
      }
      else if (Collection.class.isAssignableFrom(_target.getTargetClass()))
      { 
        Collection source;
        if (_specifier.getCollectionClass()!=null)
        {
          try
          { 
            source
              =(Collection) _specifier.getCollectionClass().newInstance();
          }
          catch (Exception x)
          { 
            throw new BuildException
              ("Error instantiating "+_specifier.getCollectionClass(),x);
          }
        }
        else if (_target.getTargetClass()==List.class)
        { source=new ArrayList(_contents.length);
        }
        else if (!_target.getTargetClass().isInterface())
        { 
          try
          { 
            source
              =(Collection) _target.getTargetClass().newInstance();
          }
          catch (Exception x)
          { 
            throw new BuildException
              ("Error instantiating "+_target.getTargetClass(),x);
          }
        }
        else
        {
          throw new BuildException
            ("Not enough information to instantiate a Collection suitable"
            +" to implement "+_target.getTargetClass()
            );
        }
           
        
        for (int i=0;i<_contents.length;i++)
        { 
          source.add(_contents[i].getSubject().get());
          registerSingletons(_contents[i]);
        }
        _source=source;
        
      }
      else
      {
        // Source is an Array
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
    else if (_specifier.getFocusExpression()!=null)
    { 
      // Focus on the specific result of evaluating the focus expression
      try
      { 
        Object focusObject=_focus.bind(_specifier.getFocusExpression()).get();
        if (focusObject!=null)
        { _focus=OpticFactory.getInstance().focus(focusObject);
        }
        else
        { 
          throw new BuildException
            ("Subject of focus "+_specifier.getFocusExpression().getText()
            +" is null." 
            );
        }
      }
      catch (BindException x)
      { 
        throw new BuildException
          ("Error binding focus "+_specifier.getFocusExpression().getText(),x);
      }
    }
    
    if (_specifier.getSourceExpression()!=null)
    {
      try
      {
        Channel sourceChannel=_focus.bind(_specifier.getSourceExpression());
        if (_target.getTargetClass()==Focus.class
            && !Focus.class.isAssignableFrom
              (sourceChannel.getTargetClass())
           )
        {
          // Property is looking for a Focus for further expression
          //   bindings
          _source=OpticFactory.getInstance().focus(sourceChannel.get());
        }
        else if (_target.getTargetClass()==Channel.class
            && !Channel.class.isAssignableFrom
              (sourceChannel.getTargetClass())
           )
        {
          // Property is looking for a Channel 
          _source=sourceChannel;
        }
        else
        { 
          // Property is looking for a standard Object or value
          _source=sourceChannel.get();
          if (_specifier.isDynamic())
          {
            // Propogate property changes
            sourceChannel
              .propertyChangeSupport()
                .addPropertyChangeListener(this);
          }
        }
      }
      catch (BindException x)
      { 
        x.printStackTrace();
        throw new BuildException
          ("Error binding "+_specifier.getSourceExpression().getText(),x);
      }
      
    }
  }
  
  public void propertyChange(PropertyChangeEvent event)
  { applySafe(event.getNewValue());
  } 
  
  private void apply()
    throws BuildException
  { 
    try
    {
      if (_source!=null && !_target.set(_source))
      { throwBuildException("Could not write property "+_specifier.getTargetName(),null);
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
      System.err.println(x.toString());
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
