package spiralcraft.builder;

import java.util.ArrayList;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.util.Iterator;
import java.util.List;

/**
 * Specifies a property to be defined in the context of an AssemblyClass
 *
 * The 'specifier' of the property is a name expression (spiralcraft.lang)
 *   evaluated in the context of the containing Assembly which identifies
 *   a property in the containing Assembly or SubAssemblies that is to
 *   be assigned a value or modified in some way.
 */
public class PropertySpecifier
{
  private final AssemblyClass _container;
  private final String _specifier;
  private StringBuffer _textContent;
  private String _textData;
  private ArrayList _contents;
  private Expression _targetExpression;
  private Expression _sourceExpression;
  private boolean _literalWhitespace;

  public PropertySpecifier
    (AssemblyClass container
    ,String specifier
    )
  {
    _container=container;
    _specifier=specifier;
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

    try
    { _targetExpression=Expression.parse(_specifier);
    }
    catch (Exception x)
    { throw new BuildException("Error parsing "+_specifier,x);
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
    
  }

  public Expression getTargetExpression()
  { return _targetExpression;
  }

  PropertyBinding bind(Assembly container)
    throws BuildException
  { return new PropertyBinding(this,container);
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
    { _textContent=new StringBuffer();
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
