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

import java.util.Collection;
import java.util.ArrayList;

import spiralcraft.util.StringUtil;
import spiralcraft.util.ArrayUtil;

import spiralcraft.lang.Expression;

import java.util.List;

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
  private AssemblyClass _targetAssemblyClass;
  private int _targetSequence;
  private Expression<?> _sourceExpression;
  private Expression<?> _focusExpression;
  private boolean _literalWhitespace;
  private String _focus;
  private String _expression;
  private boolean _persistent;
  private boolean _dynamic;
  private PropertySpecifier _baseMember;
  private String _collectionClassName;
  private Class<? extends Collection<?>> _collectionClass;
  private boolean _export;
  
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
  
  public String getSourceCodeLocation()
  { 
    return 
      ArrayUtil.format(_container.getInnerPath(),"/",null)
      +"."
      +ArrayUtil.format(_specifier,".",null)
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
   * Specify the expression that will be bound at built-time to provide
   *   
   */
  public void setFocus(String val)
  { _focus=val;
  }

  public String getFocus()
  { return _focus;
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
  
  public Expression<?> getFocusExpression()
  { return _focusExpression;
  }

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
    return assembly.getPropertyBinding(_targetSequence);
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

      
    if (_focus!=null)
    { 
      if (_contents!=null)
      { throw new BuildException("Properties cannot have both a focus and Assembly definitions");
      }
      if (_textData!=null)
      { throw new BuildException("Properties cannot have both expressions and text data");
      }

      try
      { _focusExpression=Expression.parse(_focus);
      }
      catch (Exception x)
      { throw new BuildException("Error parsing focus "+_focus,x);
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

      try
      { _sourceExpression=Expression.parse(_expression);
      }
      catch (Exception x)
      { throw new BuildException("Error parsing expression: "+_expression,x);
      }
    }
    
    resolveContents();
    
    resolveTarget();
    
  }

  /**
   * Resolve the target spec (property pathname string)
   */
  private final void resolveTarget()
    throws BuildException
  {
    _targetName=_specifier[_specifier.length-1];

    AssemblyClass targetAssemblyClass=_container;
    // Register specifier with appropriate assembly or subassembly

    for (int i=0;i<_specifier.length-1;i++)
    { 
      // When a path is specified, a subassembly override is indicated (paths are
      //   merely shorthand for overriding a series of nested assemblies to
      //   chain the overridden property to the context in which it was specifed). 
      
      AssemblyClass localContainer=targetAssemblyClass;
      String pathElement=_specifier[i];
      
      PropertySpecifier targetAssemblyPropertySpecifier
        =targetAssemblyClass.getMember(pathElement);

      if (targetAssemblyPropertySpecifier==null)
      { throw new BuildException("Member assembly '"+pathElement+"' not found");
      }
      
      
      List<AssemblyClass> contents
        =targetAssemblyPropertySpecifier.getContents();
      if (contents==null || contents.size()==0)
      { throw new BuildException("Property '"+pathElement+"' does not contain any Assemblies");
      }

      // Add feature to index contents
      if (contents.size()>1)
      { throw new BuildException("Property '"+pathElement+"' contains more than one Assembly");
      }
      else
      { targetAssemblyClass=contents.get(0);
      }
      
      // When we recurse, make sure we are dealing with a local class,
      //  not a base class
      targetAssemblyClass=localContainer.ensureLocalClass
        (pathElement,targetAssemblyClass);
        
      
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
      { assemblyClass.resolve();
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

  public void addAssemblyClass(AssemblyClass assemblyClass)
  { 
    if (_contents==null)
    { _contents=new ArrayList<AssemblyClass>(1);
    }
    _contents.add(assemblyClass);
    assemblyClass.setContainingProperty(this);
    
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

  
  public List<AssemblyClass> getCombinedContents()
  { 
    List<AssemblyClass> ret=new ArrayList<AssemblyClass>();
    if (_baseMember!=null)
    { ret.addAll(_baseMember.getCombinedContents());
    }
    if (_contents!=null)
    { ret.addAll(_contents);
    }
    return ret;
  }

 
  public String getTextData()
  { return _textData;
  }

  public String toString()
  { 
    return super.toString()
      +":"+ArrayUtil.format(_container.getInnerPath(),".","")
      +"."+ArrayUtil.format(_specifier,".","");
  }
}
