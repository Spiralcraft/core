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
package spiralcraft.data.persist;



import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusProvider;
import spiralcraft.lang.SimpleFocus;

/**
 * Wraps a PersistentReference to make it available in the hierarchical Focus
 *   context of the application. If the referent object is a FocusProvider
 *   itself, the referent will provide the Focus that extends the Focus
 *   hierarchy. 
 * 
 * @author mike
 *
 * @param <Treferent>
 * @param <Tfocus>
 */
public class PersistentFocusProvider<Treferent,Tfocus>
  implements FocusProvider<Tfocus>
{
  
  private PersistentReference<Treferent> ref;

  public PersistentFocusProvider(PersistentReference<Treferent> ref)
  { this.ref=ref;
  }

  public PersistentReference<Treferent> getReference()
  { return ref;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<Tfocus> createFocus(
    Focus<?> parent)
    throws BindException
  {
    if (ref.get() instanceof FocusProvider)
    { 
      return ((FocusProvider<Tfocus>) ref.get()).createFocus(parent);
    }
    else
    { 
      SimpleFocus<Treferent> simpleParent
        =new SimpleFocus<Treferent>
        (parent
        ,ref.bind(parent)
        );
      return (Focus<Tfocus>) simpleParent;
    }
  }
}
