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
package spiralcraft.data.sax;


import spiralcraft.lang.ParseException;
import spiralcraft.lang.util.DictionaryBinding;

/**
 * <p>Associates a loosely bound name, such as an XML attribute name or 
 *   a variable in a URLEncoded query string with an Expression bound to
 *   a data model.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class AttributeBinding<T>
  extends DictionaryBinding<T>
{
  
  private boolean transformNamespace;
    
  /**
   * Create an AttributeBinding
   */
  public AttributeBinding()
  {
  }
  
  /**
   * Whether the attribute value is prefixed with a namespace declaration
   *   that should be resolved.
   * 
   * @param transformNamespace
   */
  public void setTransformNamespace(boolean transformNamespace)
  { this.transformNamespace=transformNamespace;
  }
  
  public boolean getTransformNamespace()
  { return transformNamespace;
  }
  
  /**
   * <p>Create an AttributeBinding using the shorthand method. The syntax
   *   of the shorthand string is:
   * </p>
   * 
   *   <code><i>attributeName</i> ( "=" <i>targetExpression</i> ) </code>
   *  
   * <p>If the targetExpression is excluded, it will be set to the same as
   *   the attribute name, which relies on an identically named field in
   *   the Type associated with the bound scope. 
   * </p> 
   * 
   * @param shortHand
   */
  public AttributeBinding(String shortHand)
    throws ParseException
  { super(shortHand);
  }

  public void setAttribute(String attribute)
  { setName(attribute);
  }
  
  /**
   * <p>The name of the attribute
   * </p>
   * 
   * @return The attribute name
   */
  public String getAttribute()
  { return getName();
  }
  
}
