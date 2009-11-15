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
package spiralcraft.lang;



/**
 * <p>A grouping of Focus objects that cross-cuts application layers.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
@Deprecated
public class CompoundFocus<T>
  extends SimpleFocus<T>
{


  
  public CompoundFocus
    (Focus<?> parentFocus
    ,Channel<T> subject
    )
  { 
    setParentFocus(parentFocus);
    setSubject(subject);
  }
  
  public CompoundFocus()
  {
  }
  
  /**
   * <p>Publish a Focus into the Focus chain that will be made visible via
   *   this CompoundFocus. 
   * </p>
   * 
   * <p>The differentiator will allow the provided Focus to be referenced
   *   specifically if there are other Focii in the chain which provide
   *   the same type
   * </p>
   *  
   * <p>The Focus is normally referenced using the following construct
   *   <code>[<I>namespace</I>:<I>name</I>]</code> or 
   *   <code>[<I>namespace</I>:<I>name</I>#<I>differentiator</I>]</code>
   *   in the expression language.
   * </p>
   * 
   * @deprecated All Focus implementations can add facets now. Use 
   *   addFacet(Focus<?> focus) in BaseFocus.
   */
  @Deprecated
  public synchronized void bindFocus(String differentiator,Focus<?> focus)
    throws BindException
  { addFacet(focus);
  }  


}
