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
package spiralcraft.lang.parser;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

/**
 * A reference to a relative Focus. The specific Focus is derived at bind-time from
 *   the Focus passed to the bind method. The bind method refers to the subject of
 *   the resulting focus.
 */
public abstract class FocusNode
  extends Node
{

  /**
   * Return another Focus related to the specified Focus
   */
  public abstract Focus<?> findFocus(final Focus<?> focus)
    throws BindException;
  
  @Override
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  { 
    Channel<?> ret=findFocus(focus).getSubject();
    if (ret==null)
    { return LiteralNode.NULL.bind(focus);
    }
    return ret;
  }

}
