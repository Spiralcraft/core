//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.builder;

import spiralcraft.util.StringConverter;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.DefaultFocus;


import spiralcraft.builder.persist.PersistentReference;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.lang.reflect.Array;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import spiralcraft.registry.RegistryNode;

import java.util.prefs.Preferences;

import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Field;

/**
 * Associates a PropertySpecifier with some value in the context of
 *   an instantiated Assembly
 *
 * Note: The following "Preferences" mechanism has been deprecated in favor
 *   of externally driven persistence.
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

    // Resolve the target first to determine how we are to interpret
    //   the contents/source information
    resolveTarget();
    instantiateContents();
  }

  public void resolve()
    throws BuildException
  {
    if (_contents!=null)
    {
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isResolved())
        { assembly.resolve();
        }
      }
    }
    resolveSource();
    apply();
  }

  public void applyProperties()
    throws BuildException
  { 
    if (_contents!=null)
    {
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isApplied())
        { assembly.applyProperties();
        }
      }
    }
  }
  
  /**
   * Write persistent data to the associated Tuple field
   */
  public void storePersistentData()
  { 
    if (_contents!=null && _contents.length>0)
    { 
      for (int i=0;i<_contents.length;i++)
      { _contents[i].savePreferences();
      }
    }
    else if (_specifier.isPersistent())
    { 
      PersistentReference ref
        =(PersistentReference) _registryNode.findInstance
          (PersistentReference.class);
        
      if (ref!=null)
      {
        Tuple tuple = ref.getTuple();
        
        Object value=_target.get();

        // XXX To implement- write value tuple and flush
        /*
        if (value==null)
        { preferences.remove(_specifier.getTargetName());
        }
        else if (_target.getContentType().isAssignableFrom(String.class))
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
              +"' ("+_target.getContentType()+") to a String");
          }
        }
        */
      }
    }
  }
  
  // XXX Deprecated- use tuples
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
        else if (_target.getContentType().isAssignableFrom(String.class))
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
              +"' ("+_target.getContentType()+") to a String");
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
      if (!_target.getContentType().isArray())
      { 
        if (!_contents[0].getAssemblyClass().isSingleton())
        { _contents[0].register(propertyNode);
        }
      }
      else
      {
        for (int i=0;i<_contents.length;i++)
        { 
          if (!_contents[i].getAssemblyClass().isSingleton())
          {
            _contents[i].register
              (propertyNode.createChild(Integer.toString(i))
              );
          }
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

  public void applyPersistentData()
  {
    if (_contents!=null && _contents.length>0)
    { 
      // How to recurse int contents?
      
      // for (int i=0;i<_contents.length;i++)
      // { _contents[i].applyPersistentData();
      // }
    }
    else if (_specifier.isPersistent())
    { 
      PersistentReference ref
        =(PersistentReference) _registryNode.findInstance
          (PersistentReference.class);
        
      if (ref!=null)
      {
        Tuple tuple = ref.getTuple();
        // XXX Verify that the tuple Scheme is compatible.
        
        Field field = 
          tuple.getScheme().getFields()
            .findFirstByName(_specifier.getTargetName());
        
        Object value=tuple.get(field.getIndex());
        
      }
    }
  }
  
  /**
   * Read the value for this property from the preferences object and
   *   apply the value.
   */
  // XXX Deprecated- use tuples
  public void applyPreferences(Preferences preferences)
  {
    if (_target.getContentType().isArray())
    { applyArrayPreferences(preferences);
    }
    else
    { applySinglePreferences(preferences);
    }
  }

  /**
   * Array preferences are represented in the store in to form &lt;name&gt;.&lt;arrayIndex&gt;
   */
  // deprecated- use tuples
  private void applyArrayPreferences(Preferences preferences)
  {
    Class componentType=_target.getContentType().getComponentType();
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
    
    // Don't specify empty arrays
    // XXX Need a better test for null or zero length array
    if (prefs.size()>0)
    {
      Object targetValue=Array.newInstance(componentType,prefs.size());
      for (int i=0;i<prefs.size();i++)
      { Array.set(targetValue,i,prefs.get(i));
      }
      applySafe(targetValue);
    }
  }
  
  // deprecated- use tuples
  public void applySinglePreferences(Preferences preferences)
  {
    if (_converter==null)
    { _converter=StringConverter.getInstance(_target.getContentType());
    }
    String value=preferences.get(_specifier.getTargetName(),null);
    if (value!=null)
    {
      if (_target.getContentType().isAssignableFrom(String.class))
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
            +_target.getContentType()
            );
        }
      }
    }

  }
  
  
  private void instantiateContents()
    throws BuildException
  {
    List<AssemblyClass> contents;
    if (isAggregate())
    { contents=_specifier.getCombinedContents();
    }
    else
    { 
      contents=_specifier.getContents();
      if (contents!=null && contents.size()>1)
      { 
        throw new BuildException
          (_specifier.getTargetName()
          +" in "+_container.getAssemblyClass().getJavaClass()
          +" cannot have multiple values"
          );
      } 
    }
    
    if (contents!=null)
    { 
      _contents=new Assembly[contents.size()];
      
      int i=0;
      for (AssemblyClass assemblyClass : contents)
      { 
        Assembly assembly=assemblyClass.newInstance();
        if (_specifier.getExport())
        { exportSingletons(assembly);
        }
        _contents[i++]=assembly;
      }
      
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isBound())
        { assembly.bind(_container);
        }
      }

    }
  }

  private void resolveTarget()
    throws BuildException
  {
    try
    {
      _target=_container.getSubject()
        .resolve(_container,_specifier.getTargetName(),null);
    }
    catch (BindException x)
    {
      throw new BuildException
        ("Property '"+_specifier.getTargetName()+"' not found"
        +" ("+_specifier.getSourceCodeLocation()+")"
        ,x
        );
    }
  }

  /**
   * Indicate whether the target is an aggregate type
   */
  private boolean isAggregate()
  { 
    return _target.getContentType().isArray()
      || Collection.class.isAssignableFrom(_target.getContentType());
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
        { _source=_contents[0].getSubject().get();
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
      else if (Collection.class.isAssignableFrom(_target.getContentType()))
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
        else if (_target.getContentType()==List.class)
        { source=new ArrayList(_contents.length);
        }
        else if (!_target.getContentType().isInterface())
        { 
          try
          { 
            source
              =(Collection) _target.getContentType().newInstance();
          }
          catch (Exception x)
          { 
            throw new BuildException
              ("Error instantiating "+_target.getContentType(),x);
          }
        }
        else
        {
          throw new BuildException
            ("Not enough information to instantiate a Collection suitable"
            +" to implement "+_target.getContentType()
            );
        }
           
        
        for (int i=0;i<_contents.length;i++)
        { 
          source.add(_contents[i].getSubject().get());
          if (_specifier.getExport())
          { exportSingletons(_contents[i]);
          }
        }
        _source=source;
        
      }
      else
      {
        // Source is an Array
        _source=Array.newInstance
          (_target.getContentType().getComponentType()
          ,_contents.length
          );
        for (int i=0;i<_contents.length;i++)
        { 
          Array.set(_source,i,_contents[i].getSubject().get());
          if (_specifier.getExport())
          { exportSingletons(_contents[i]);
          }
        }
      }
    }
    else if (_specifier.getTextData()!=null)
    {
      String text=_specifier.getTextData();
      _converter=StringConverter.getInstance(_target.getContentType());
      if (_converter==null)
      { 
        throw new BuildException
          ("No StringConverter registered for "
          +_target.getContentType().getName()
          );
      }
      _source=_converter.fromString(text);
    }
    else if (_specifier.getFocusExpression()!=null)
    { 
      // Focus on the specific result of evaluating the focus expression
      
      // Focus expressions in Assembly definitions are intended to permit the 
      //   developer to specify a alternate component in the assembly hierarchy
      //   against which to resolve an expression. By default, expressions
      //   resolve against the containing Assembly. 
      //
      // The focus expression is evaluated only once, as opposed to the source
      //   expression, which is evaluated every time a property update is 
      //   triggered. This -may- help performance.
      // 
      // This particular feature remains under evaluation, as it can be
      //   confusing.
      try
      { 
        Object focusObject=_focus.bind(_specifier.getFocusExpression()).get();
        if (focusObject!=null)
        { 
          // Consider supplying a parent focus here, since we may want to
          //   resolve something from this container.
          _focus=new DefaultFocus
            (OpticFactory.getInstance().createOptic(focusObject)
            );
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
    else
    { 
    }
    
    if (_specifier.getSourceExpression()!=null)
    {
      try
      {
        Channel sourceChannel=_focus.bind(_specifier.getSourceExpression());
        if (_target.getContentType()==Focus.class
            && !Focus.class.isAssignableFrom
              (sourceChannel.getContentType())
           )
        {
          // Property is looking for a Focus for further expression
          //   bindings. Convert the source value into a Focus and pass the
          //   Focus to the property.
          //
          // This feature effectively allows components to evaluate expressions
          //   at runtime against the containing assembly hierarchy or any
          //   object accessible from it.
          
          // !!! It is important to call get() to NARROW the type to that of
          //   the actual object. Otherwise, the formal property type will
          //   be used and some names will not resolve as expected.
          _source=new DefaultFocus
            (OpticFactory.getInstance().createOptic
              (sourceChannel.get()
              )
            );
        }
        else if (_target.getContentType()==Channel.class
            && !Channel.class.isAssignableFrom
              (sourceChannel.getContentType())
           )
        {
          // Property is looking for a Channel 
          //
          // This is another special case- components looking for a Channel
          //   are looking for a view on a property, not a property itself,
          //   primarily so the components can subscribe to property change
          //   events and manage updates.
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
      { 
        throwBuildException
          ("Could not write ["+_source.toString()+"] value to property \""
          +_specifier.getTargetName()+"\""
          ,null
          );
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

  private void exportSingletons(Assembly source)
    throws BuildException
  {
    Class[] interfaces=source.getSingletons();
    if (interfaces!=null)
    { _container.registerSingletons(interfaces,source);
    }
    
  }
}
