//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.sax;

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.SAXException;

/**
 * <p>Recursively descends a ParseTree to process Nodes and construct a
 *   tree of peer objects, which are assembled in an application specific
 *   manner based on overridden behavior.
 * </p>
 * 
 * <p>This class is intended to facilitate the construction of special purpose
 *   XML data handlers in code, to handle situations where data cannot be
 *   cleanly mapped using a more general purpose mapping- for example, 
 *   configuration files.
 * </p>
 * 
 * @author mike
 *
 */
public class ElementReader<Tpeer>
{

  protected static final CharactersReader CHARACTERS_READER
    =new CharactersReader();
  
  private HashMap<String,ElementReader<?>> childMap;
  private Stack<Tpeer> stack=new Stack<Tpeer>();
  protected String elementName;
  
 
  /**
   * <p>Map the specified element name to the specified reader
   * </p>
   * 
   * @param name
   * @param reader
   */
  protected void map(String name,ElementReader<?> childReader)
  { 
    if (childMap==null)
    { childMap=new HashMap<String,ElementReader<?>>();
    }
    childMap.put(name,childReader);
  }
  
  protected void map(ElementReader<?> childReader)
  { 
    if (childReader.getElementName()!=null)
    { map(childReader.getElementName(),childReader);
    }
    else
    { 
      throw new IllegalArgumentException
        ("Child ElementReader must have a name. Either provide"
        +" a value for the elementName field "
        +"or use the map(String,ElementReader)" 
        +" method."
        );
        		
    }
  }
  
  /**
   * <p>The standard element name mapping for this reader,
   *   or null if this ElementReader is re-used for multiple types of
   *   Elements
   * </p>
   * 
   * @return
   */
  protected final String getElementName()
  { return elementName;
  }
  
  public final Tpeer read(Element element)
    throws SAXException
  { 
    
    stack.push(null);
    try
    {
      open(element);
      if (element.hasChildren())
      {
        for (Element child:element.getChildren(Element.class))
        { 
          if (childMap!=null)
          {
            ElementReader<?> childReader
              = childMap.get(child.getLocalName());
            if (childReader!=null)
            { readChild(childReader,child);
            }
            else
            { skipChild(child);
            }
          }
          else
          { skipChild(child);
          }
        }
      }
      close(element);
      return stack.peek();
    }
    finally
    { stack.pop();
    }
  }
  
  
  protected Tpeer get()
  { return stack.peek();
  }
  
  protected final void set(Tpeer val)
  { stack.set(stack.size()-1,val);
  }
  
  /**
   * <p>Override to setup the local context before processing children
   * </p>
   * 
   * @param element
   */
  protected void open(Element element)
  {
  }
  
  /**
   * <p>Calls childReader.read(child) for each child, after open(Element) is
   *   called and before close(Element) is called.
   * </p>
   * 
   * <p>Override to do something with the returned value after each child
   *   element is read.
   * </p>
   * 
   * @param childReader
   * @param child
   */
  protected Object readChild(ElementReader<?> childReader,Element child)
    throws SAXException
  { return childReader.read(child);
  }
    
  /**
   * <p>Override to do something when an unregistered Element is skipped
   * </p>
   * @param child
   */
  protected void skipChild(Element child)
  {
  }
  
  /**
   * <p>Override to finalize the local context after processing children,
   *   or to pull child values from the Element.
   * </p>
   * 
   * @param element
   */
  protected void close(Element element)
    throws SAXException
  {
  }
  

  
}


