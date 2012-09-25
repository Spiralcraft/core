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
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.ContextDictionary;
import spiralcraft.util.string.StringConverter;

import spiralcraft.common.declare.DeclarationContext;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Focus;

import spiralcraft.lang.BindException;
import spiralcraft.lang.AccessException;
import spiralcraft.log.ClassLog;


import java.util.List;
import java.util.ArrayList;
import java.util.Collection;


import java.lang.reflect.Array;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * Associates a PropertySpecifier with some value or value source
 *   in the context of an instantiated Assembly
 */
@SuppressWarnings({"unchecked","rawtypes"}) // Heterogenous design- does not use generics
public class PropertyBinding
  implements PropertyChangeListener
{

  private static final ClassLog log
    =ClassLog.getInstance(PropertyBinding.class);
  
  private Assembly _container;
  private PropertySpecifier _specifier;

  private Channel _target;
  // private Channel _sourceOptic;
  private Focus _focus;
  
  private Assembly[] _contents;
  private StringConverter _converter;
  private boolean existingTargetValueUsed;

  public PropertyBinding(PropertySpecifier specifier,Assembly container)
    throws BuildException
  { 
    _specifier=specifier;
    _container=container;
    _focus=container.getFocus();
    if (specifier.getPrefixResolver()!=null)
    { _focus=_focus.chain(specifier.getPrefixResolver());
    }
    
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
    // 2009-04-30 miketoth
    //
    //   Don't apply member contents instantiated by default.
    // 
    boolean defaultMember=_specifier.isDefaultMember();
    
    Object defaultVal=null;
    
    if (!isAggregate() 
       && _contents!=null
       && _contents.length==1
       && !_contents[0].getAssemblyClass().isSingleton()
       )
    { 
      // Give the containing object an opportunity to construct a default instance
      //   to use as the source object, instead of having the sub-assembly construct
      //   one.
      try
      { defaultVal=_target.get();
      }
      catch (RuntimeException x)
      { 
        throw new BuildException
          ("Error resolving default value for "+_specifier.getTargetName()
          ,_specifier.getDeclarationLocation()
          );
      }
      
      existingTargetValueUsed=defaultVal!=null;
    }
    
    if (_contents!=null)
    {
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isResolved())
        { 
          if (defaultMember)
          { assembly.resolveDefault();
          }
          else
          { assembly.resolve(defaultVal);
          }
        }
      }
    }
    
    if (!defaultMember)
    { applySource();
    }
  }
  
  /**
   * Called instead of resolve() when in a default sub-tree to avoid
   *   instantiating anything
   * 
   * @throws BuildException
   */
  public void resolveDefault()
    throws BuildException
  {   
    if (_contents!=null)
    {
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isResolved())
        { assembly.resolveDefault();
        }
      }
    }
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
  

  public Assembly[] pullContents()
  {
    if (isAggregate())
    { return _contents;
    }
    else
    {
      Object val=_target.get();
      if (val!=null)
      {
        if (_contents==null || _contents.length==0)
        { 
          try
          {
            AssemblyClass newAssemblyClass
              =AssemblyLoader.getInstance().findAssemblyClass(val.getClass());
        
            Assembly newAssembly
              =newAssemblyClass.wrap(_container.getFocus(),val);
        
            return new Assembly[] {newAssembly};
          }
          catch (BuildException x)
          { throw new RuntimeException(x);
          }
        
        } 
        else
        { 
          _contents[0].getFocus().getSubject().set(val);
          return _contents;
        }
      }
      else
      { 
        // XXX We need to consider further what to do with null object value
        
        return _contents;
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
  }

  
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
        throwBuildException
          (_specifier.getTargetName()
          +" ("+_target.getContentType()+") "
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
        Assembly assembly=assemblyClass.newInstance
          (_container.getFocus(),factoryMode);
        
        if (_specifier.getExport())
        { exportSingletons(assembly);
        }
        _contents[i++]=assembly;
      }
      
      for (Assembly assembly:_contents)
      { 
        if (!assembly.isBound())
        { assembly.bind();
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
      throwBuildException
        ("Property '"+_specifier.getTargetName()+"' not found"
        +" ("+_specifier.getSourceCodeLocation()+"): available properties are "
        +"["
        +ArrayUtil.format
          (_specifier.getTargetAssemblyClass()
            .getAllPropertyNames()
          ,","
          ,""
          )
        +"]"
        ,x
        );
    }
  }

  /**
   * Indicate whether the target is an aggregate type
   */
  public boolean isAggregate()
  { return AssemblyClass.isAggregate(_target.getContentType());
  }
  
  /**
   * Apply the literally included contents (Assemblies) of the property
   * 
   * @throws BuildException
   */
  private void applyContents()
    throws BuildException
  {
    if (!isAggregate())
    {
      // Source is a single object
      if (_contents.length==1)
      { apply(_contents[0].get());
      }
      else
      {
        throwBuildException
          (_specifier.getTargetName()
          +" ("+_target.getContentType()+") "
          +" in "+_container.getAssemblyClass().getJavaClass()
          +" cannot have multiple values"
          );
      }
    }
    else if (Collection.class.isAssignableFrom(_target.getContentType()))
    { 
      Collection collection=null;
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
        throwBuildException
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
        { 
          try
          { 
            if (_specifier.getContextualize())
            { val=ContextDictionary.substitute((String) val);
            }
          }
          catch (ParseException x)
          { throwBuildException("Error parsing properties in "+val,x);
          }
        }
        try
        { Array.set(array,i,val);
        }
        catch (IllegalArgumentException x)
        { 
          throwBuildException
            ("Error setting index "+i+" of array "+array+" to "+val
            +" for target "+_target
            ,x);
        }
        
        if (_specifier.getExport())
        { exportSingletons(_contents[i]);
        }
      }
      apply(array);
    }
    
  }
  
  /**
   * Apply the textual data provided in the specifier
   * 
   * @throws BuildException
   */
  private void applyText()
    throws BuildException
  {
    if (!isAggregate())
    {
      String text=null;
      
      // Push context to ensure that local AssemblyClass context is
      ///  visible
      _specifier.getContainer().pushContext();
      try
      { 
        text=_specifier.getTextData();
        if (_specifier.getContextualize())
        { text=ContextDictionary.substitute(text);
        }
        
      }
      catch (ParseException x)
      { 
        throwBuildException
          ("Error parsing properties in "+_specifier.getTextData(),x);
      }
      finally
      { _specifier.getContainer().popContext();
      }
      
      _converter=_target.getReflector().getStringConverter();
      if (_converter==null)
      { 
        throwBuildException
          ("No StringConverter registered for "
          +_target.getContentType().getName()
          );
      }
    
      NamespaceContext.push(_specifier.getPrefixResolver());
      try
      {
        Object value=_converter.fromString(text);
        apply(value);
      }
      finally
      { NamespaceContext.pop();
      }
    }
    else
    {
      try
      {
        CollectionDecorator cd
          =(CollectionDecorator) _target.decorate(CollectionDecorator.class);
      
        _converter=cd.getComponentReflector().getStringConverter();
        Object value=cd.newCollection();
        
        // Push context to ensure that local AssemblyClass context is
        ///  visible
        _specifier.getContainer().pushContext();
        try
        {
          for (String textData:_specifier.getTextDataList())
          { 

            // log.fine("String value "+textData);

            try
            { 
              if (_specifier.getContextualize())
              { textData=ContextDictionary.substitute(textData);
              }
            }
            catch (ParseException x)
            { 
              throwBuildException
                ("Error parsing properties in "+textData,x);
            }

            if (_converter==null)
            {
              throwBuildException
                ("Cannot construct a "
                  +cd.getComponentReflector().getContentType().getName()
                  +" from text"
                );
            }
            NamespaceContext.push(_specifier.getPrefixResolver());
            DeclarationContext.push(_specifier.getDeclarationLocation());
            // log.fine("Collection value "+elementValue);
            try
            { 
              Object elementValue=_converter.fromString(textData);
              value=cd.add(value,elementValue);
            }
            finally
            { 
              DeclarationContext.pop();
              NamespaceContext.pop();
            }
          }
        }
        finally
        { _specifier.getContainer().popContext();
        }
        
        if (_specifier.getDebugLevel().isDebug())
        { log.fine("Result is "+value+" from "+cd);
        }
        apply(value);
      }
      catch (BindException x)
      { 
        throw new BuildException
          ("Error resolving Collection conversion for "
          +_target.getContentType().getName()
          );
      }
    }
  }
  
  /**
   * Apply the expression provided in the specifier
   * 
   * @throws BuildException
   */
  private void applyExpression()
    throws BuildException
  {
    DeclarationContext.push(_specifier.getDeclarationLocation());
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
      throwBuildException
        ("Error binding "+_specifier.getSourceExpression().getText(),x);
    }
    finally
    { DeclarationContext.pop();
    }
      
    
  }
  
  /**
   * Read the property value from data
   * 
   * @throws BuildException
   */
  private void applyData()
    throws BuildException
  { 
    try
    {
      apply
        (AbstractXmlObject.create
            (null
            , _specifier.getDataURI()
            ).get()
        );
    }
    catch (BindException x)
    { 
      throwBuildException
        ("Error creating property value "+_specifier.getDataURI(),x);
    }
  }
  
  
  /**
   * Resolve the source data for this property. At this point, all 
   *   sub-assemblies must have had their contents instantiated and configured.
   */
  private void applySource()
    throws BuildException
  {
    try
    {
      if (_contents!=null && _contents.length>0)
      { applyContents();
      }
      else if (_specifier.getTextData()!=null)
      { applyText();
      }
      else if (_specifier.getSourceExpression()!=null)
      { applyExpression();
      }
      else if (_specifier.getDataURI()!=null)
      { applyData();
      }
    }
    catch (BuildException x)
    { throw x;
    }
    catch (RuntimeException x)
    { 
      throw new BuildException
        ("Error applying value to property '"
        +_specifier.getTargetName()+"': "
        +_specifier.getSourceCodeLocation()
        ,x);
    }
  }
  

  
  @Override
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
        if (!existingTargetValueUsed)
        {
          if (!_target.isWritable())
          { 
            log.warning("Non-writable property '"+_specifier.getTargetName()
                        +"'- rejected value ["+sourceVal+"] "
                        +", defined at "+_specifier.getSourceCodeLocation()
                        +": target is "+_target
                        );
            
          }
          else
          {
            log.warning("Target '"+_specifier.getTargetName()
                        +"' rejected value ["+sourceVal+"] "
                        +", defined at "+_specifier.getSourceCodeLocation()
                        );
          }
        }
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

  private void throwBuildException(String message)
    throws BuildException
  { throw new BuildException(message+" ("+_specifier.getSourceCodeLocation()+")");
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
  {
    Class[] interfaces=source.getSingletons();
    if (interfaces!=null)
    { _container.registerSingletons(interfaces,source);
    }
    
  }
}



