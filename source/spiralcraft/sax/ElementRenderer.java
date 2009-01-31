//
// Copyright (c) 2009,2009 Michael Toth
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

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import spiralcraft.lang.util.DictionaryBinding;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.spi.SimpleChannel;

import spiralcraft.text.Renderer;
import spiralcraft.text.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Renders an XML document Element mapped to data obtained from the
 *   FocusChain. Provides bindings for attributes and leaf elements.
 * </p>
 * 
 * <p>Extend this class for specific Elements and configuration mechanisms,
 *   for instance, a subclass that sets up attribute bindings and leaves
 *   according to Expressions set through bean properties, or a subclass
 *   that uses a data file for mapping.
 * </p>
 * 
 * <p>The structural recursion associated with the overall output is provided
 *   at a higher API layer.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class ElementRenderer
  implements Wrapper, FocusChainObject
{

  private List<DictionaryBinding<?>> attributes;
  private List<DictionaryBinding<?>> leaves;
  
  protected boolean document;

  @Override
  public void render(
    Writer writer,Renderer nestedContent)
    throws IOException
  {
    XmlWriter xmlWriter=new XmlWriter(writer,null);
    try
    { 
      if (document)
      { xmlWriter.startDocument();
      }
      xmlWriter.startElement
        (getURI(),getLocalName(),getQName(), generateAttributes());
      renderXML(xmlWriter);
      if (nestedContent!=null)
      { 
        // Not SAX, but we need to tell writer to not wait for content
        //   before writing the '>' in the open tag.
        xmlWriter.startElementContent();
        
        nestedContent.render(writer);
      }
      xmlWriter.endElement(getURI(),getLocalName(),getQName());
      if (document)
      { xmlWriter.endDocument();
      }
    }
    catch (SAXException x)
    { throw new IOException(x);
    }
  }
  
  protected String getURI()
  { return null;
  }
  
  protected abstract String getLocalName();
  
  protected String getQName()
  { return getLocalName();
  }
  
  
  /**
   * Add an attribute binding. For convenience, does nothing if expression is
   *   null
   * 
   * @param name
   * @param expression
   */
  @SuppressWarnings("unchecked")
  protected void addAttributeBinding(String name,Expression expression)
  { 
    if (attributes==null)
    { attributes=new ArrayList<DictionaryBinding<?>>();
    }
    DictionaryBinding<?> binding=new DictionaryBinding();
    binding.setName(name);
    binding.setTarget(expression);
    attributes.add(binding);
  }
  
  /**
   * Add a leaf (a child element that contains only characters).
   *  For convenience, does nothing if expression is null.
   * 
   * @param name
   * @param expression
   */
  @SuppressWarnings("unchecked")
  protected void addLeaf(String name,Expression expression)
  {
    if (expression==null)
    { return;
    }
    
    if (leaves==null)
    { leaves=new ArrayList<DictionaryBinding<?>>();
    }
    DictionaryBinding<?> binding=new DictionaryBinding();
    binding.setName(name);
    binding.setTarget(expression);
    leaves.add(binding);
  }
  
  protected void addLeaf(DictionaryBinding<?> binding)
  {
    if (leaves==null)
    { leaves=new ArrayList<DictionaryBinding<?>>();
    }
    leaves.add(binding);
  }
  
  
  protected void renderXML(ContentHandler handler)
    throws SAXException
  {
    if (leaves!=null)
    {
      for (DictionaryBinding<?> binding:leaves)
      {
        String val=binding.get();
        if (val!=null)
        {
          handler.startElement
            (null,binding.getName()
            ,binding.getName()
            ,ArrayAttributes.EMPTY_INSTANCE
            ); // TODO We may need a binding renderer
          
          char[] characters=val.toCharArray();
          handler.characters(characters,0,characters.length);
          handler.endElement(null,binding.getName(),binding.getName());
        }
      }
    }
  }
    
  protected Attributes generateAttributes()
  { 
    
    if (attributes!=null)
    {
      Attribute[] ret=new Attribute[attributes.size()];
      int i=0;
      for (DictionaryBinding<?> binding:attributes)
      {
        ret[i++]=new Attribute(binding.getName(),binding.get());
      }
      return new ArrayAttributes(ret);
    }
    else
    { return ArrayAttributes.EMPTY_INSTANCE;
    }
  }
  
  /**
   * Subclasses overriding this method should call the superclass method
   *   after adding attributes and leaves.
   */
  @Override 
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    Focus<?> focus
      =focusChain.chain(new SimpleChannel<ElementRenderer>(this,true));
    if (attributes!=null)
    { 
      for (DictionaryBinding<?> binding:attributes)
      { binding.bind(focus);
      }
    }
    if (leaves!=null)
    { 
      for (DictionaryBinding<?> binding:leaves)
      { binding.bind(focus);
      }
    }
      
    return focus;
  }


}
