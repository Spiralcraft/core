package spiralcraft.builder;

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
  private boolean _preference;
  private boolean _dynamic;
  
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
   * Indicate whether the value for this property should be persisted within
   *   the preferences subsystem
   */
  public boolean isPreference()
  { return _preference;
  }

  /**
   * Indicate whether the value for this property should be persisted within
   *   the preferences subsystem
   */
  public void setPreference(boolean val)
  { _preference=val;
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

    _targetName=_specifier[_specifier.length-1];

    AssemblyClass targetAssemblyClass=_container;
    // Register specifier with appropriate assembly or subassembly
    for (int i=0;i<_specifier.length-1;i++)
    { 
      PropertySpecifier targetAssemblyPropertySpecifier
        =targetAssemblyClass.getMember(_specifier[i]);

      if (targetAssemblyPropertySpecifier==null)
      { throw new BuildException("Member assembly '"+_specifier[i]+"' not found");
      }
      
      List contents=targetAssemblyPropertySpecifier.getContents();
      if (contents==null || contents.size()==0)
      { throw new BuildException("Property '"+_specifier[i]+"' does not contain any Assemblies");
      }

      // Add feature to index contents
      if (contents.size()>1)
      { throw new BuildException("Property '"+_specifier[i]+"' contains more than one Assembly");
      }
      else
      { targetAssemblyClass=(AssemblyClass) contents.get(0);
      }
    }
    _targetAssemblyClass=targetAssemblyClass;
    _targetAssemblyClass.registerMember(_targetName,this);
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
}
