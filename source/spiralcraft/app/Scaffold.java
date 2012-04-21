//
//Copyright (c) 2012 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app;

import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.text.ParsePosition;

/**
 * A Scaffold creates a component tree based on some external definition 
 *   interpreted against a context in which it is bound.
 */
public interface Scaffold<T extends Scaffold<T>>
{

  /**
   * @return The children of this ScaffoldUnit
   */
  List<T> getChildren();
  
  /**
   * 
   * @return The parent of this ScaffoldUnit
   */
  T getParent();
  
  /**
   * The position in the source code document or data file that defined
   *   this Scaffold unit.
   * 
   * @return
   */
  public ParsePosition getPosition();
  
  /**
   * 
   * @param focus
   * @param parent
   * @return A bound Contextual
   */
  Component bind(Focus<?> focus,Parent parent)
    throws ContextualException;  

}
