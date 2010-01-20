//
// Copyright (c) 1998,2010 Michael Toth
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
package spiralcraft.security.spi;


import spiralcraft.data.DataException;
import spiralcraft.data.query.Scan;
import spiralcraft.data.query.Selection;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.security.auth.DigestCredential;
import spiralcraft.security.auth.PasswordCleartextCredential;
import spiralcraft.security.auth.UsernameCredential;

/**
 * <p>A DataAuthModule which checks a username and a password against
 *   a Login database.
 * </p>
 * 
 * <p>The lookup function matches UsernameCredential.toLowerCase() to
 *   Login.searchName
 * </p>
 *   
 * @author mike
 *
 */
public class PasswordDataAuthModule
  extends DataAuthModule
{
  
  @SuppressWarnings("unchecked")
  public PasswordDataAuthModule()
    throws DataException
  { 
    super();


    setAcceptedCredentials
      (new Class[] 
        {UsernameCredential.class
        ,PasswordCleartextCredential.class
        ,DigestCredential.class
        }
      );
    
    
    setAccountQuery
      (new Selection
        (new Scan(getAccountDataType())
        ,Expression.<Boolean>create
          (".searchname==UsernameCredential.toLowerCase() ")
        )
      );
      
    
    super.setCredentialComparisonX
      (new Binding<Boolean>
        (Expression.<Boolean>create
          ("(PasswordCleartextCredential!=null "
          +"&& .clearpass==PasswordCleartextCredential)"
          +"|| (DigestCredential!=null " 
          +"    && DigestCredential" 
          +"    .equals([:class:/spiralcraft/security/auth/AuthSession] " 
          +"      .opaqueDigest(.username+.clearpass)"
          +"     )"
          +"   )"
          )
        )
      );
  
  }
  
  public void bind()
  {
  }
  
}
