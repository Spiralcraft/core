package spiralcraft.builder;

import java.util.ArrayList;

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
  private ArrayList _contents;

  public PropertySpecifier(AssemblyClass container,String specifier)
  {
    _container=container;
    _specifier=specifier;
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

}
