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

import java.beans.PropertyDescriptor;

import java.net.URI;
import java.util.Collection;
import java.util.ArrayList;

import spiralcraft.text.ParsePosition;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.string.StringUtil;

import spiralcraft.beans.MappedBeanInfo;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.lang.Expression;

import java.util.List;

import spiralcraft.sax.SaxPrefixResolver;

/**
 * Specifies a property to be defined in the context of an AssemblyClass
 *
 * The 'specifier' of the property is a dotted name which specifies a
 *   path through referenced assemblies to a specific
 *   bean property that is to be assigned a value
 *   or modified in some way- eg. "connection.password"
 */
public class PropertySpecifier
{
  private final AssemblyClass _container;
  private final String[] _specifier;
  private StringBuffer _textContent;
  private String _textData;
  private ArrayList<AssemblyClass> _contents;
  
  private String _targetName;
  private String _targetSelector;
  
  private AssemblyClass _targetAssemblyClass;
  private int _targetSequence=-1;
  private Expression<?> _sourceExpression;
  private boolean _literalWhitespace;
  private String _expression;
  private boolean _persistent;
  private boolean _dynamic;
  private PropertySpecifier _baseMember;
  private String _collectionClassName;
  private Class<? extends Collection<?>> _collectionClass;
  private boolean _export;
  private URI _dataURI;
  
  private SaxPrefixResolver prefixResolver;
  private PropertyDescriptor descriptor;
  private boolean defaultMember;
  
  private Class<?> propertyType;
  
  private ParsePosition position;

  public PropertySpecifier
    (AssemblyClass container
    ,PropertyDescriptor descriptor
    )
  {
    if (container==null)
    { throw new IllegalArgumentException("Container cannot be null");
    }
    _container=container;
    _specifier=new String[] {descriptor.getName()};
    this.descriptor=descriptor;
  }
  
  public PropertySpecifier
    (AssemblyClass container
    ,String specifier
    )
  {
    if (container==null)
    { throw new IllegalArgumentException("Container cannot be null");
    }
    _container=container;
    _specifier=StringUtil.tokenize(specifier,".");
  }

  public PropertySpecifier
    (AssemblyClass container
    ,String specifier
    ,String value
    )
  {
    this(container,specifier);
    addCharacters(value.toCharArray());
  }

  /**
   * <P>Create a PropertySpecifier which holds as its content an inner-subclass
   *   of some AssemblyClass.
   *   
   * This is only called from AssemblyClass.ensureLocalClass()
   *   
   */
  PropertySpecifier
    (AssemblyClass container
    ,String specifier
    ,AssemblyClass content
    )
  {
    this(container,specifier);
    addAssemblyClass(content);
  }
  
  
  public void setParsePosition(ParsePosition position)
  { this.position=position;
  }
  
  public PropertyDescriptor getPropertyDescriptor()
  { 
    if (this.descriptor!=null)
    { return this.descriptor;
    }
    else if (_baseMember!=null)
    { return _baseMember.getPropertyDescriptor();
    }
    else
    { return null;
    }
       
  }
  
  public void setPropertyDescriptor(PropertyDescriptor descriptor)
  { this.descriptor=descriptor;
  }
  
  public boolean isDefaultMember()
  { return defaultMember;
  }
  
  public Class<?> getPropertyType()
    throws BuildException
  { 
    if (propertyType==null)
    { resolveType();
    }
    if (propertyType==null)
    { throwUnresolvable();
    }
    return propertyType;
  }
  
  private void throwUnresolvable()
    throws BuildException
  { 
    
    throw new BuildException
      ("Error resolving property "
      +_specifier+" in "+getSourceCodeLocation()+": No property '"+_targetName
      +" in "+_targetAssemblyClass.getJavaClass().getName()+"- available "
      +" properties are ["
      +ArrayUtil.format
        (_targetAssemblyClass.getBeanInfo().getAllPropertyNames(),",","")
      +"]"
          
      );
  }
  
  /**
   * Specified when this member is created automatically by the discovery
   *   mechanism as opposed to being explicitly declared.
   *   
   * @param defaultMember
   */
  public void setDefaultMember(boolean defaultMember)
  { this.defaultMember=defaultMember;
  }
  
  public void setPrefixResolver(SaxPrefixResolver resolver)
  { this.prefixResolver=resolver;
  }
  
  public SaxPrefixResolver getPrefixResolver() 
  { return this.prefixResolver;
  }
  
  
  public String getSourceCodeLocation()
  { 
    return 
      ArrayUtil.format(_container.getInnerPath(),"/",null)
      +"."
      +ArrayUtil.format(_specifier,".",null)
      +(position!=null?" ("+position.toString()+") ":"")
      +" in "
      +(_container.getSourceURI()!=null?
         _container.getSourceURI().toString():"(unknown source)");
  }

  /**
   * Indicate whether the value for this property should be persisted to
   *   non-volatile storage
   */
  public boolean isPersistent()  
  {
    return 
      (!_persistent && _baseMember!=null)
      ?_baseMember.isPersistent()
      :_persistent
      ;
  }
  
  public void setExport(boolean export)
  { _export=export;
  }
  
  public boolean getExport()
  { return _export;
  }
  
  public void setCollectionClassName(String name)
  { _collectionClassName=name;
  }
  
  public String getCollectionClassName()
  { 
    return 
      (_collectionClassName==null && _baseMember!=null)
      ?_baseMember.getCollectionClassName()
      :_collectionClassName
      ;
  }
  
  /**
   * Return the implementation of java.util.Collection which should be
   *   used to hold the contents for this property.
   */
  public Class<? extends Collection<?>> getCollectionClass()
  { return _collectionClass;
  }
  
  /**
   * Indicate whether the value for this property should be persisted to
   *   non-volatile storage.
   */
  public void setPersistent(boolean val)
  { _persistent=val;
  }

  public boolean isDynamic()
  { return _dynamic;
  }
  
  public void setDynamic(boolean val)
  { _dynamic=val;
  }
  
  /** 
   * Indicate whether whitespace is to be treated literally or 
   *   trimmed.
   *@param val true if whitespace is to be treated literally, 
   *  false to trim whitespace (default)
   */
  public void setLiteralWhitespace(boolean val)
  { _literalWhitespace=val;
  }


  /**
   * Specify the expression that will be bound and evaluated at build-time when the
   *   Assembly is instantiated to provide a value for the property.
   */
  public void setExpression(String val)
  { _expression=val;
  }

  public Expression<?> getSourceExpression()
  { return _sourceExpression;
  }
  
  /**
   * Specify the URI of the data resource that will provide the value
   *   for this property. 
   * 
   * @param dataURI
   */
  public void setDataURI(URI dataURI)
  { this._dataURI=dataURI;
  }
  
  public URI getDataURI()
  { return _dataURI;
  }
  
  /**
   * Using a PropertySpecifier scoped to an AssemblyClass, efficiently
   *   return the associated PropertyBinding from an instance of the
   *   AssemblyClass.
   * 
   * @param assembly
   * @return
   */
  public PropertyBinding getPropertyBinding(Assembly<?> assembly)
  { 
    if (assembly.getAssemblyClass()!=_targetAssemblyClass)
    { 
      if (assembly.getAssemblyClass().getBaseClass()!=_targetAssemblyClass)
      {
        
        throw new IllegalArgumentException
          ("Assembly "
          +assembly.getAssemblyClass()
          +" is not instance of specifier '"+_targetName+"' target "
          +_targetAssemblyClass
          );
      }
      else
      {
//        throw new IllegalArgumentException
//          ("Assembly "
//          +assembly.getAssemblyClass()
//          +" is not instance of specifier '"+_targetName+"' target "
//          +_targetAssemblyClass
//          +" but extends it- use PropertyDescriptor in subAssemblyClass instead"
//          );
        
      }
    }
    try
    { return assembly.getPropertyBinding(_targetSequence);
    }
    catch (ArrayIndexOutOfBoundsException x)
    { 
      throw new RuntimeException
        ("Unexpected error getting binding for property "+this,x);
    }
  }
  
  /**
   * Create a new PropertyBinding for this specifier that is bound to the
   *   provided Assembly.
   * 
   * @param assembly
   * @return
   */
  public PropertyBinding bind(Assembly<?> assembly)
    throws BuildException
  { 
    
    
    return new PropertyBinding(this,assembly);
  }
  
  void setTargetSequence(int sequence)
  { _targetSequence=sequence;
  }
  
  int getTargetSequence()
  { return _targetSequence;
  }
  
  /**
   * Resolve anything required and recurse into contents
   */
  void resolve()
    throws BuildException
  { 
    
    if (_textContent!=null)
    { 
      String trimmedText=_textContent.toString().trim();
      if (trimmedText.length()>0 && _contents!=null)
      { throw new BuildException("Properties cannot contain both text data and Assembly definitions");
      }

      if (_literalWhitespace)
      { _textData=_textContent.toString();  
      }
      else if (trimmedText.length()>0)
      { _textData=trimmedText;
      }
    }

          
    if (_expression!=null)
    { 
      if (_contents!=null)
      { throw new BuildException("Properties cannot have both expressions and Assembly definitions");
      }
      if (_textData!=null)
      { throw new BuildException("Properties cannot have both expressions and text data");
      }

      NamespaceContext.push(prefixResolver);
      try
      { 
        _sourceExpression=Expression.parse(_expression);
      }
      catch (Exception x)
      { throw new BuildException("Error parsing expression: "+_expression,x);
      }
      finally
      { NamespaceContext.pop();
      }
    }
    
    resolveContents();
    
    resolveTarget();
    
    if (this.descriptor==null)
    { this.descriptor=_targetAssemblyClass.getPropertyDescriptor(_targetName);
    }
    
    resolveType();

  }

  private void resolveType()
  { 
    MappedBeanInfo beanInfo
      =_targetAssemblyClass.getBeanInfo();
    
    PropertyDescriptor desc=getPropertyDescriptor();
    if (desc!=null)
    { propertyType=beanInfo.getCovariantPropertyType(getPropertyDescriptor());
    }
    
  }
  
  /**
   * The AssemblyClass that this PropertySpecifier ultimately refers (which is
   *   different than the AssemblyClass which defined this PropertySpecifier
   *   if the dotted-name notation is used).
   *   
   *   
   * 
   * @return
   */
  public AssemblyClass getTargetAssemblyClass()
  { return _targetAssemblyClass;
  }
  
  /**
   * Resolve the target spec (property pathname string)
   */
  private final void resolveTarget()
    throws BuildException
  {
    _targetName=_specifier[_specifier.length-1];
    
    int selectorIndex=_targetName.indexOf('-');
    if (selectorIndex>=1)
    { 
      _targetSelector=_targetName.substring(selectorIndex+1);
      _targetName=_targetName.substring(0,selectorIndex);
    }

    AssemblyClass targetAssemblyClass=_container;
    // Register specifier with appropriate assembly or subassembly
    //   by recursing into the structure and ensuring an appropriate
    //   targetAssemblyClass.

    for (int i=0;i<_specifier.length-1;i++)
    { 
      // When a path is specified, a subassembly override is indicated (paths are
      //   merely shorthand for overriding a series of nested assemblies to
      //   chain the overridden property to the context in which it was specifed). 
      
      AssemblyClass localContainer=targetAssemblyClass;
      
      String pathElement=_specifier[i];
      String selector=null;
      selectorIndex=pathElement.indexOf('-');
      if (selectorIndex>=1)
      { 
        selector=pathElement.substring(selectorIndex+1);
        pathElement=pathElement.substring(0,selectorIndex);
      }
      
      
      PropertySpecifier targetAssemblyPropertySpecifier
        =targetAssemblyClass.discoverMember(pathElement);

      if (targetAssemblyPropertySpecifier==null)
      { 
        // XXX Try to accomodate missing intermediate PropertySpecifier
        // Maybe just be a bean property that needs to be implicitly added
        throw new BuildException("Member property '"+pathElement+"' not found");
      }
      else
      {
      
        List<AssemblyClass> contents
          =targetAssemblyPropertySpecifier.getCombinedContents();
        
        if (selector!=null)
        { 
          //
          // Multi-element mode- selector specified
          //

          if (Character.isDigit(selector.charAt(0)))
          { 
            int index=Integer.parseInt(selector);
            if (index>=contents.size())
            { 
              throw new BuildException
                ("Property '"+pathElement
                +"'- Bad index, property only has "+contents.size()+" elements"
                );
            }
            
            targetAssemblyClass=contents.get(index);
          }
          else
          {
            boolean found=false;
            for (AssemblyClass aclazz: contents)
            {
              if (selector.equals(aclazz.getId()))
              {
                targetAssemblyClass=aclazz;
                found=true;
                break;
              }
            }
            if (!found)
            {
              throw new BuildException
                ("Property '"+pathElement
                +"'- no AssemblyClass in contents with id='"+selector+"'"
                );
            }

          }
          
        }
        else
        {
          //
          // Single element mode- no selector specified
          //
          
          if (contents==null || contents.size()==0)
          { 
            throw new BuildException
              ("Property '"+pathElement
              +"' does not contain any Assemblies (in "
              +localContainer
              +"'"
              );
          }

          if (contents.size()>1)
          {
            
            throw new BuildException
              ("Property '"+pathElement+"' contains more than one Assembly: "
              +contents.toString()
              );
          }
          else
          { targetAssemblyClass=contents.get(0);
          }
          // When we recurse, make sure we are dealing with a local class,
          //  not a base class
          targetAssemblyClass=localContainer.ensureLocalClass
            (pathElement,targetAssemblyClass);
          
        }
      
      }  
      
    }
    
    
    targetAssemblyClass.registerMember(_targetName,this);
    
    // Keep the target around for future reference (not critical right now)
    _targetAssemblyClass=targetAssemblyClass;

  }
  
  private final void resolveContents()
    throws BuildException
  {
    if (_contents!=null)
    {
      for (AssemblyClass assemblyClass:_contents)
      { 
        if (!assemblyClass.isResolved())
        { assemblyClass.resolve();
        }
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private final void resolveCollection()
    throws BuildException
  {
    if (getCollectionClassName()!=null)
    { 
      try
      { 
        _collectionClass
          =(Class<? extends Collection<?>>) Class.forName
            (getCollectionClassName()
            ,false
            ,Thread.currentThread().getContextClassLoader()
            );
      }
      catch (ClassNotFoundException x)
      { 
        throw new BuildException
          ("Collection class not found: '"+getCollectionClassName()+"'",x);
      }
      
      if (!Collection.class.isAssignableFrom(_collectionClass))
      { 
        throw new BuildException
          ("Collection class '"+getCollectionClassName()+"' "
          +"does not implement interface java.util.Collection"
          );
      }
    }
  }
      
  /**
   * A pre-existing  member which this member overrides
   */
  void setBaseMember(PropertySpecifier prop)
    throws BuildException
  { 
    if (prop==this)
    { 
      throw new IllegalArgumentException
        ("baseMember "+prop+" cannot be a self reference");
    }
    _baseMember=prop;
    _targetSequence=prop.getTargetSequence();
    resolveCollection();
    
  }
  
  /**
   * The member which this member overrides
   */
  public PropertySpecifier getBaseMember()
  { return _baseMember;
  }
  
  public String getTargetName()
  { return _targetName;
  }

  /**
   * Add an assembly class owned by this PropertySpecifier
   * 
   * @param assemblyClass
   */
  public void addAssemblyClass(AssemblyClass assemblyClass)
  { 
    if (_contents==null)
    { _contents=new ArrayList<AssemblyClass>(1);
    }
    _contents.add(assemblyClass);
    if (!assemblyClass.isResolved())
    { assemblyClass.setContainingProperty(this);
    }
    
  }

  
  public void addCharacters(char[] characters)
  {
    if (_textContent==null)
    { _textContent=new StringBuffer(characters.length);
    }
    _textContent.append(characters);
  }

  public List<AssemblyClass> getContents()
  { return _contents;
  }

  
  /**
   * Retrieve the combined Contents of this PropertySpecifier and any
   *   base PropertySpecifiers.
   *   
   * @return
   */
  public List<AssemblyClass> getCombinedContents()
  { 
    List<AssemblyClass> ret=new ArrayList<AssemblyClass>();
    if (_baseMember!=null)
    { ret.addAll(_baseMember.getCombinedContents());
    }
    if (_contents!=null)
    { 
      if (_targetSelector!=null)
      {
        for (int i=0;i<ret.size();i++)
        { 
          if (_targetSelector.equals(ret.get(i).getId()))
          { 
            ret.set(i,_contents.get(0));
            break;
          }
        }
      }
      else if 
        (_contents.size()==1 
        && !AssemblyClass.isAggregate(_contents.get(0).getJavaClass())
        )
      { 
        // If this is not an aggregate property, and we are specifying a
        //   value here, replace the value specified in the base type
        if (ret.size()>0)
        { ret.set(0,_contents.get(0));
        }
        else
        { ret.add(_contents.get(0));
        }
      }
      else
      { ret.addAll(_contents);
      }
    }
    return ret;
  }

 
  public String getTextData()
  { return _textData;
  }

  @Override
  public String toString()
  { 
    return super.toString()
      +":"+ArrayUtil.format(_container.getInnerPath(),".","")
      +"."+ArrayUtil.format(_specifier,".","")
      +"("+_container.getSourceURI()+")";
  }
}
