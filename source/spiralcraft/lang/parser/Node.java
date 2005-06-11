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

import spiralcraft.lang.optics.Prism;

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticAdapter;

/**
 * A Node in an Expression parse tree
 */
public abstract class Node
{

  /**
   * Stubbed bind method for unimplemented nodes.
   *
   *@return An optic with no functionality
   */
  public Optic bind(Focus focus)
    throws BindException
  { 
    System.err.println(getClass().getName()+" not implemented");
    return new OpticAdapter()
    {
      public Prism getPrism()
      { 
        try
        { return OpticFactory.getInstance().findPrism(Void.class);
        }
        catch (BindException x)
        { // shouldn't happen
        }
        return null;
      }
    };
  }

  public abstract void dumpTree(StringBuffer out,String prefix);

}
