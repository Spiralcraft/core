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

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
import spiralcraft.text.markup.MarkupHandler;
import spiralcraft.text.markup.MarkupParser;
import spiralcraft.util.string.StringConverter;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.NamespaceResolver;

import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.AccessException;

import spiralcraft.lang.spi.FocusWrapper;
import spiralcraft.lang.spi.SimpleChannel;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.net.URI;

import java.lang.reflect.Array;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


import spiralcraft.registry.RegistryNode;

import spiralcraft.sax.PrefixResolver;

/**
 * Associates a PropertySpecifier with some value or value source
 *   in the context of an instantiated Assembly
 */
@SuppressWarnings("unchecked") // Heterogenous design- does not use generics
public class PropertyBinding
  implements PropertyChangeListener
{
  private static final MarkupParser substitutionParser
    =new MarkupParser("${","}",'\\');
  
  private Assembly _container;
  private PropertySpecifier _specifier;

  private Channel _target;
  // private Channel _sourceOptic;
  private Focus _focus;
  
  private Assembly[] _contents;
  private StringConverter _converter;
  // private RegistryNode _registryNode;
  private RegistryNode propertyNode;

  public PropertyBinding(PropertySpecifier specifier,Assembly container)
    throws BuildException
  { 
    _specifier=specifier;
    _container=container;
    _focus=new NamespaceFocus
      (container.getFocus(),specifier.getPrefixResolver());

    // Resolve the target first to determine how we are to interpret
    //   the contents/source information
    createTargetChannel();
//    createSourceChanne();  
    bindContents();
  }

  /**
   * Instantiate the sub-assemblies, resolve the property value source, retrieve the value
   *   and apply it to the target,.
   */
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
    applySource();
  }
  
  public void release()
  {
    if (_contents!=null)
    {
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isResolved())
        { assembly.release();
        }
      }
    }
  }
  

  public Assembly[] getContents()
  { return _contents;
  }
  
  public void replaceContents(Assembly[] contents)
    throws BuildException
  {
    _contents=contents;
    applySource();
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

//  private void createSourceChannel()
//  {
//    if (_container.isFactoryMode())
//    { _sourceChannel=new ThreadLocalChannel(_target.getReflector());
//    }
//    else
//    { _sourceChannel=new SimpleChannel(_target.getReflector(),null,false);
//    }
//  }
  
  /**
   * Instantiate sub-Assemblies (but not the instances they contain yet)
   */
  private void bindContents()
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
      boolean factoryMode=_container.isFactoryMode();
      _contents=new Assembly[contents.size()];
      
      int i=0;
      for (AssemblyClass assemblyClass : contents)
      { 
        Assembly assembly=assemblyClass.newInstance(factoryMode);
        if (_specifier.getExport())
        { exportSingletons(assembly);
        }
        _contents[i++]=assembly;
      }
      
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isBound())
        { assembly.bind(_container.getFocus());
        }
      }

    }
  }

  
  private void createTargetChannel()
    throws BuildException
  {
    try
    {
      _target=_container.getFocus().getSubject()
        .resolve(_container.getFocus(),_specifier.getTargetName(),null);
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
  private void applySource()
    throws BuildException
  {
    if (_contents!=null && _contents.length>0)
    {
      if (!isAggregate())
      {
        // Source is a single object
        if (_contents.length==1)
        { apply(_contents[0].get());
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
        Collection collection;
        if (_specifier.getCollectionClass()!=null)
        {
          try
          { 
            collection
              =_specifier.getCollectionClass().newInstance();
          }
          catch (Exception x)
          { 
            throw new BuildException
              ("Error instantiating "+_specifier.getCollectionClass(),x);
          } 
        }
        else if (_target.getContentType()==List.class)
        { collection=new ArrayList<Object>(_contents.length);
        }
        else if (!_target.getContentType().isInterface())
        { 
          try
          { 
            collection
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
          collection.add(_contents[i].get());
          if (_specifier.getExport())
          { exportSingletons(_contents[i]);
          }
        }
        apply(collection);
        
      }
      else
      {
        // Source is an Array
        Object array
          =Array.newInstance
            (_target.getContentType().getComponentType()
            ,_contents.length
            );
        for (int i=0;i<_contents.length;i++)
        { 
          Object val=_contents[i].get();
          if (_contents[i].getAssemblyClass().getJavaClass()==String.class)
          { val=substitute((String) val);
          }
          Array.set(array,i,val);
          if (_specifier.getExport())
          { exportSingletons(_contents[i]);
          }
        }
        apply(array);
      }
    }
    else if (_specifier.getTextData()!=null)
    {
      String text=substitute(_specifier.getTextData());
      
      _converter=StringConverter.getInstance(_target.getContentType());
      if (_converter==null)
      { 
        throw new BuildException
          ("No StringConverter registered for "
          +_target.getContentType().getName()
          );
      }
      apply(_converter.fromString(text));
    }
    else
    { 
    }
    
    if (_specifier.getSourceExpression()!=null)
    {
      try
      {
        Channel sourceChannel=_focus.bind(_specifier.getSourceExpression());

        // Property is looking for a standard Object or value
        apply(sourceChannel.get());
        if (_specifier.isDynamic())
        {
          // Propagate property changes
          sourceChannel
            .propertyChangeSupport()
              .addPropertyChangeListener(this);
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
  
  public String substitute(String raw)
    throws BuildException
  {
    final StringBuffer ret=new StringBuffer();
    try
    {
      substitutionParser.parse
        (raw
        ,new MarkupHandler()
        {

          @Override
          public void handleContent(
            CharSequence text)
            throws ParseException
          { ret.append(text);
          }

          @Override
          public void handleMarkup(
            CharSequence code)
            throws ParseException
          { 
            String substitution=System.getProperty(code.toString());
            if (substitution!=null)
            { ret.append(substitution);
            }
          }

          @Override
          public void setPosition(
            ParsePosition position)
          { } 
        }
        ,null
        );
    }
    catch (ParseException x)
    { throw new BuildException("Error parsing text property "+_specifier); 
    }

    return ret.toString();
  }
  
  public void propertyChange(PropertyChangeEvent event)
  { applySafe(event.getNewValue());
  } 
  
  public Assembly getContainer()
  { return _container;
  }

  private void apply(Object sourceVal)
    throws BuildException
  { 
    try
    {
      if (sourceVal!=null && !_target.set(sourceVal))
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
    catch (AccessException x)
    {
      throwBuildException
        ("Caught "+x.getCause().toString()
          +" writing ["+sourceVal.toString()+"] value to property \""
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
    try
    { apply(value);
    }
    catch (Exception x)
    { System.err.println(x.toString());
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

class NamespaceFocus<tFocus>
  extends FocusWrapper<tFocus>
{ 
  private NamespaceResolver resolver;
  
  public NamespaceFocus(Focus<tFocus> delegate,final PrefixResolver resolver)
  { 
    super(delegate);
    this.resolver
      =new NamespaceResolver()
    {

      @Override
      public URI getDefaultNamespaceURI()
      {
        // TODO Auto-generated method stub
        return URI.create(resolver.resolvePrefix("default"));
      }

      @Override
      public URI resolveNamespace(
        String prefix)
      {
        // TODO Auto-generated method stub
        return URI.create(resolver.resolvePrefix(prefix));
      }
    };
  }
  
  @Override
  public NamespaceResolver getNamespaceResolver()
  { return resolver;
  }

}

