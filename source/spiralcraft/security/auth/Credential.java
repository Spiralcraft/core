//
//Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.security.auth;

/**
 * <P>A data token used for Authentication. The Authenticator will
 *   consume this data, which is produced by the client specific
 *   connector.
 *   
 * <P>Credentials are identified by their Class, and are immutable.
 */
public abstract class Credential<T>
{
  private final Class<T> tokenType;
  private final T value;
  
  protected Credential(Class<T> dataType,T value)
  { 
    this.tokenType=dataType;
    this.value=value;
  }
  
  public final Class<T> getTokenType()
  { return tokenType;
  }
  
  public final T getValue()
  { return value;
  }
  
}
