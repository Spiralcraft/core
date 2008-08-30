//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.rules;

import spiralcraft.lang.ChannelFactory;


/**
 * <p>An assertable condition, that produces Violations
 * </p>
 * 
 * <p>The Rule produces a Channel&lt;Violation&gt; when
 *   bound against a Focus chain that provides required contextual data.
 * </p>
 * 
 * 
 * @author mike
 *
 */
public interface Rule<C,T>
  extends ChannelFactory<Violation<T>,T>
{

  /**
   * @return The application component against which the rule is implemented
   */
  public C getContext();
  
  /**
   * @return A message which directs compliance with the Rule (ie. "Must
   *   must be shorter than 100 words").
   */
  public String getMessage();
  
}
