package spiralcraft.builder;

import java.util.Collection;
import java.util.ArrayList;

import spiralcraft.util.StringUtil;
import spiralcraft.util.ArrayUtil;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.util.Iterator;
import java.util.List;

/**
 * Specifies a property to be defined in the context of an AssemblyClass
 *
 * The 'specifier' of the property is a dotted name which specifies a
 *   path through contained assemblies to an Optic (spiralcraft.lang)
 *   that is to be assigned a value or modified in some way.
 */
public class PropertySpecifier
{
  private final AssemblyClass _container;
  private final String[] _specifier;
  private StringBuffer _textContent;
  private String _textData;
  private ArrayList _contents;
  private String _targetName;
  private AssemblyClass _targetAssemblyClass;
  private Expression _sourceExpression;
  private Expression _focusExpression;
  private boolean _literalWhitespace;
  private String _focus;
  private String _expression;
  private boolean _persistent;
  private boolean _dynamic;
  private PropertySpecifier _baseMember;
  private String _collectionClassName;
  private Class _collectionClass;
  
  public PropertySpecifier
    (AssemblyClass container
    ,String specifier
    )
  {
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

  public PropertySpecifier
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
      ArrayUtil.formatToString(_container.getInnerPath(),"/",null)
      +"."
      +ArrayUtil.formatToString(_specifier,".",null)
      +" in "
      +_container.getSourceURI().toString();
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
  public Class getCollectionClass()
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

  public Expression getSourceExpression()
  { return _sourceExpression;
  }
  
  public Expression getFocusExpression()
  { return _focusExpression;
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
      
      
      List contents=targetAssemblyPropertySpecifier.getContents();
      if (contents==null || contents.size()==0)
      { throw new BuildException("Property '"+pathElement+"' does not contain any Assemblies");
      }

      // Add feature to index contents
      if (contents.size()>1)
      { throw new BuildException("Property '"+pathElement+"' contains more than one Assembly");
      }
      else
      { targetAssemblyClass=(AssemblyClass) contents.get(0);
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
      Iterator it=_contents.iterator();
      while (it.hasNext())
      { 
        AssemblyClass assemblyClass
          =(AssemblyClass) it.next();
        assemblyClass.resolve();
      }
    }
  }
  
  private final void resolveCollection()
    throws BuildException
  {
    System.out.println(toString()+" resolve collection");
    if (getCollectionClassName()!=null)
    { 
      try
      { 
        _collectionClass
          =Class.forName
            (getCollectionClassName()
            ,false
            ,Thread.currentThread().getContextClassLoader()
            );
        System.out.println(toString()+_collectionClass);
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
    resolveCollection();
    System.out.println(toString()+" overriding "+prop.toString());
  }
  
  public String getTargetName()
  { return _targetName;
  }

  public void addAssemblyClass(AssemblyClass assembly)
  { 
    if (_contents==null)
    { _contents=new ArrayList(1);
    }
    _contents.add(assembly);
    
  }

  public void addCharacters(char[] characters)
  {
    if (_textContent==null)
    { _textContent=new StringBuffer(characters.length);
    }
    _textContent.append(characters);
  }

  public List getContents()
  { return _contents;
  }

  public String getTextData()
  { return _textData;
  }

  public String toString()
  { 
    return super.toString()
      +":"+ArrayUtil.formatToString(_container.getInnerPath(),".","")
      +"."+ArrayUtil.formatToString(_specifier,".","");
  }
}
