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
package spiralcraft.security.spi;

import spiralcraft.security.auth.Authenticator;
import spiralcraft.security.auth.AuthSession;
import spiralcraft.security.auth.UsernameCredential;
import spiralcraft.security.auth.PasswordCleartextCredential;

import spiralcraft.data.access.SerialCursor;

import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Selection;
import spiralcraft.data.query.Scan;
import spiralcraft.data.query.BoundQuery;


import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Type;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;

import java.net.URI;
import java.security.Principal;

/**
 * <P>Authenticates with a spiralcraft.data.query.Queryable for the backing store
 *   which holds login info.
 *   
 * <P>The default behavior uses a login/cleartext-password scheme backed by
 *   the class:/spiralcraft/security/Login data Type. Alternate behaviors and
 *   credential sets can be defined at the configuration level.
 *   
 * @author mike
 *
 */
public class DataAuthenticator
  extends Authenticator
{
  
  private Queryable<?> source;
  private Focus<Space> spaceFocus;
  
  // XXX Make these both configurable
  private Type<?> loginDataType;
  private Query loginQuery;
  private BoundQuery<?,?> boundQuery;
  
  @SuppressWarnings("unchecked")
  public DataAuthenticator()
    throws DataException
  {
    
    try
    {
      setRequiredCredentials
        (new Class[] 
          {UsernameCredential.class
          ,PasswordCleartextCredential.class
          }
        );
      
      // Default values for basic username/password authentication
      loginDataType
        =Type.resolve(URI.create("class:/spiralcraft/security/Login"));

      // XXX Make this an Equijoin to search type values in parameter
      //  context
      loginQuery=new Selection
        (new Scan(loginDataType)
        ,Expression.<Boolean>parse
          (".searchname==UsernameCredential.toLowerCase() "
          +" && .clearpass==PasswordCleartextCredential"
          )
        );
      
    }
    catch (ParseException x)
    { 
      throw new RuntimeException
        ("DataAuthenticator: Error parsing built-in",x);
    }
    
  
  }
  
  public void setRealmName(String realmName)
  { this.realmName=realmName;
  }
  
  /**
   * @param source The Queryable which provides access to the login database
   */
  public void setSource(Queryable<?> source)
  { this.source=source;
  }
  
  public AuthSession createSession()
  { return new DataAuthSession();
  }
  
  @SuppressWarnings("unchecked")
  public void bind(Focus<?> context)
    throws BindException
  { 
    super.bind(context);
    if (source==null && context!=null)
    { 
      spaceFocus
        =(Focus<Space>) context.findFocus(Space.SPACE_URI);
      if (spaceFocus==null)
      { 
        throw new BindException
          ("'source' not set, and no Space was found in Focus chain");
      }
    }

    Queryable source=DataAuthenticator.this.source;
        
    if (source==null || spaceFocus!=null)
    { source=spaceFocus.getSubject().get();
    }
    if (source==null)
    { 
      throw new BindException
        ("No data source for DataAuthenticator");
    }
    try
    { boundQuery=source.query(loginQuery,credentialFocus);
    }
    catch (DataException x)
    { throw new BindException("Error binding Authenticator query "+loginQuery,x);
    }

  }
  

  public class DataAuthSession
    extends AuthSession
  {
    
    
    public DataAuthSession()
    {
    }
        
    public boolean isAuthenticated()
    {
      if (boundQuery==null)
      { 
        System.err.println
          ("DataAuthenticator.DataAuthSession.isAuthenticated: "
          +" Authentication failed- configuration error"
          );
        return false;
      }
      
      if (principal!=null && authenticated)
      { return true;
      }
      
      // Make sure the query is accessing this AuthSession
      sessionChannel.push(this);
      try
      {
        SerialCursor<?> cursor=boundQuery.execute();
        if (cursor.dataNext())
        { 
          // We have a row where all the credentials in the database
          //   match the supplied data
          
          // XXX Do something in a standard fashion to provide access to the
          //   Principal resolved by this operation
          
          // cursor.discard()
//          System.out.println
//            ("DataAuthenticator:login: "+cursor.dataGetTuple());
          
          final String name=(String) loginDataType.getField("username")
                .getValue(cursor.dataGetTuple());
          
          if (principal==null
              || !principal.getName().equals(name)
             )
          {
            principal
              =new Principal()
            {

              @Override
              public String getName()
              { return name;
              }
            
              public String toString()
              { return super.toString()+":"+name;
              }
            };
          }
          if (sticky)
          { authenticated=true;
          }
          return true;
        }
        else
        { 
//          System.out.println
//            ("DataAuthenticator:login: no match");
          return false;
        }

      }
      catch (DataException x)
      { 
        x.printStackTrace();
        return false;
      }
      finally
      { sessionChannel.pop();
      }
    }
  }
}
  
  
