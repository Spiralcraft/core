//
// Copyright (c) 1998,2007 Michael Toth
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

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.WriteException;

import spiralcraft.lang.spi.SimpleBinding;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.lang.reflect.Array;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import spiralcraft.registry.RegistryNode;


/**
 * Associates a PropertySpecifier with some value or value source
 *   in the context of an instantiated Assembly
 */
@SuppressWarnings("unchecked") // Heterogenous design- does not use generics
public class PropertyBinding
  implements PropertyChangeListener
{
  private Assembly _container;
  private PropertySpecifier _specifier;

  private Channel _target;
  // private Channel _sourceOptic;
  private Focus _focus;
  private Object _source;

  private Assembly[] _contents;
  private StringConverter _converter;
  // private RegistryNode _registryNode;
  private RegistryNode propertyNode;

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

  /**
   * Instantiate the sub-assemblies, resolve the property value source, retrieve the value
   *   and apply it to the target,.
   */
  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
  public void resolve()
    throws BuildException
  {
    if (!isAggregate() 
       && _contents!=null
       && _contents.length==1
       && !_contents[0].getAssemblyClass().isSingleton()
       )
    { 
      // Give the containing object an opportunity to construct a default instance
      //   to use as the source object, instead of having the sub-assembly construct
      //   one.
      Object defaultVal=_target.get();
      if (defaultVal!=null)
      { _contents[0].setDefaultInstance(defaultVal);
      }
    }
    
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

  public Assembly[] getContents()
  { return _contents;
  }
  
  public void replaceContents(Assembly[] contents)
    throws BuildException
  {
    _contents=contents;
    resolveSource();
    apply();
    if (propertyNode!=null)
    { registerSubAssemblies();
    }
  }

  private void registerSubAssemblies()
  {
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
  
  public void register(RegistryNode node)
  {
    // _registryNode=node;
    if (_contents!=null && _contents.length>0)
    { 
      // Sub-assemblies (contents) are specified in definition.
      // Register all sub-assemblies 
      propertyNode=node.createChild(_specifier.getTargetName());
      registerSubAssemblies();
    }
  }

  
  /**
   * Instantiate sub-Assemblies (but not the instances they contain yet)
   */
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

  
  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
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
  public boolean isAggregate()
  { 
    return _target.getContentType().isArray()
      || Collection.class.isAssignableFrom(_target.getContentType());
  }
  
  /**
   * Resolve the source data for this property. At this point, all sub-assemblies must 
   *   have had their contents instantiated and configured.
   */
  @SuppressWarnings("unchecked")
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
              =_specifier.getCollectionClass().newInstance();
          }
          catch (Exception x)
          { 
            throw new BuildException
              ("Error instantiating "+_specifier.getCollectionClass(),x);
          } 
        }
        else if (_target.getContentType()==List.class)
        { source=new ArrayList<Object>(_contents.length);
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
          _focus=new SimpleFocus
            (new SimpleBinding(focusObject,true)
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
          _source=new SimpleFocus
            (new SimpleBinding(sourceChannel.get(),false)
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
  
  public Assembly getContainer()
  { return _container;
  }

  @SuppressWarnings("unchecked") // We haven't genericized the builder package builder yet
  private void apply()
    throws BuildException
  { 
    try
    {
      if (_source!=null && !_target.set(_source))
      { 
        // Log something here- value didn't didn't need to change
        
        //        System.err.println(_source.toSt
        //        throwBuildException
        //          ("Could not write ["+_source.toString()+"] value to property \""
        //          +_specifier.getTargetName()+"\"- not a writable property"
        //          ,null
        //          );
      }
    }
    catch (WriteException x)
    {
      throwBuildException
        ("Caught "+x.getCause().toString()+" writing ["+_source.toString()+"] value to property \""
        +_specifier.getTargetName()+"\""
        ,x
        );
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
