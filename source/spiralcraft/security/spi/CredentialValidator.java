//
//Copyright (c) 2010 Michael Toth
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
package spiralcraft.security.spi;

import spiralcraft.lang.Contextual;

/**
 * Determines whether a particular credential evaluation operation is
 *   applicable, and whether it succeeds or fails. 
 * 
 * @author mike
 *
 */
public interface CredentialValidator
  extends Contextual
{
  
  /**
   * This method returns either true, false
   * 
   * @return true or false, if the validator is applicable to the supplied
   *   credentials, or null if the validator does not contribute to the
   *   chain.
   */
  Boolean validate();

}
