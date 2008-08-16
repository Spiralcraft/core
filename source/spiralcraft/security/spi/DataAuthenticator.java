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
import spiralcraft.security.auth.DigestCredential;
import spiralcraft.security.auth.UsernameCredential;
import spiralcraft.security.auth.PasswordCleartextCredential;

import spiralcraft.data.access.SerialCursor;

import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Selection;
import spiralcraft.data.query.Scan;
import spiralcraft.data.query.BoundQuery;


import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLogger;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

/**
 * <p>Authenticates with a spiralcraft.data.query.Queryable for the 
 *   backing store which holds login info.
 * </p>
 *   
 * <p>The default behavior uses a login/cleartext-password scheme backed by
 *   the class:/spiralcraft/security/Login data Type. Alternate behaviors and
 *   credential sets can be defined at the configuration level.
 * </p>
 *   
 * @author mike
 *
 */
public class DataAuthenticator
  extends Authenticator
{
  
  private static final ClassLogger log
    =ClassLogger.getInstance(DataAuthenticator.class);
  
  private Queryable<?> providedSource;
  private Focus<Space> spaceFocus;
  
  // XXX Make these both configurable
  private Type<?> loginDataType;

  private Query loginQuery;
  private BoundQuery<?,?> boundQuery;
  private Channel<String> usernameChannel;
  private boolean debug;
  private TeleFocus<Tuple> comparisonFocus;
  private ThreadLocalChannel<Tuple> loginChannel;
  private Expression<Boolean> credentialComparison;
  private Channel<Boolean> comparisonChannel;
  
  @SuppressWarnings("unchecked")
  public DataAuthenticator()
    throws DataException
  {

      setAcceptedCredentials
        (new Class[] 
          {UsernameCredential.class
          ,PasswordCleartextCredential.class
          ,DigestCredential.class
          }
        );
      
      // Default values for basic username/password authentication
      loginDataType
        =Type.resolve(URI.create("class:/spiralcraft/security/Login"));

      loginQuery=new Selection
        (new Scan(loginDataType)
        ,Expression.<Boolean>create
          (".searchname==UsernameCredential.toLowerCase() ")
        );
      
      credentialComparison
        =Expression.create
          ("(PasswordCleartextCredential!=null "
          +"&& .clearpass==PasswordCleartextCredential)"
          +"|| (DigestCredential!=null " 
          +"    && DigestCredential" 
          +"	  .equals([:class:/spiralcraft/security/auth/AuthSession] " 
          +"     opaqueDigest(.username+.clearpass)"
          +"     )"
          +"   )"
          );
  
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void setRealmName(String realmName)
  { this.realmName=realmName;
  }
  
  /**
   * @param source The Queryable which provides access to the login database
   */
  public void setSource(Queryable<?> source)
  { this.providedSource=source;
  }
  
  @Override
  public AuthSession createSession()
  { return new DataAuthSession();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void bind(Focus<?> context)
    throws BindException
  { 
    // superclass provides a credentialFocus member field which 
    //   provides values for the various accepted credentials,
    //   named according to the credential class simple name.
    super.bind(context);


    // Resolve the source for the master credentials list
    if (providedSource==null && context!=null)
    { 
      // Look up the local Space to use as a source if no source was provided
      spaceFocus
        =(Focus<Space>) context.findFocus(Space.SPACE_URI);
      if (spaceFocus==null)
      { 
        throw new BindException
          ("'source' not set, and no Space was found in Focus chain");
      }
    }

    Queryable source=providedSource;
        
    if (source==null || spaceFocus!=null)
    { source=spaceFocus.getSubject().get();
    }
    if (source==null)
    { 
      throw new BindException
        ("No data source for DataAuthenticator");
    }
    
    
    // Bind the user lookup query to the credential Focus, which serves as
    //   the parameter Focus.
    try
    { boundQuery=source.query(loginQuery,credentialFocus);
    }
    catch (DataException x)
    { throw new BindException("Error binding Authenticator query "+loginQuery,x);
    }
    
    usernameChannel
      =credentialFocus.bind(Expression.<String>create("UsernameCredential"));
    
    // Set up a comparison to check password/etc
    loginChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(loginDataType));
    
    comparisonFocus=new TeleFocus<Tuple>(credentialFocus,loginChannel);
    
    comparisonChannel=comparisonFocus.bind(credentialComparison);
    
  }
  

  public class DataAuthSession
    extends AuthSession
  {
    
    
    public DataAuthSession()
    {
    }
        
    @Override
    public boolean authenticate()
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
        Tuple loginEntry=null;
        
        if (cursor.dataNext())
        { 
          loginEntry=cursor.dataGetTuple();
        
          if (cursor.dataNext())
          { 
            throw new SecurityException
              ("Cardinality Violation: Multiple Login records for user "
                +loginEntry.get("searchname")
              );
          }

          // We have valid username in loginEntry
          //   run the password comparison expression

          boolean valid;
          loginChannel.push(loginEntry);
          try
          { 
            Boolean result=comparisonChannel.get();
            valid=(result!=null && result);
          }
          finally
          { loginChannel.pop();
          }
          
          
          if (valid)
          {
          
            // cursor.discard()
            if (debug)
            { log.fine("valid login: "+cursor.dataGetTuple());
            }
          
            final String name=usernameChannel.get();
          
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
            
                @Override
                public String toString()
                { return super.toString()+":"+name;
                }
              };
            }
            authenticated=true;
            return true;
          }
          else
          {
            if (debug)
            { log.fine("failed login: no token match for "+usernameChannel.get());
            }
            authenticated=false;
            return false;
          }
        }
        else
        { 
          if (debug)
          { log.fine("failed login: no username match for "+usernameChannel.get());
          }
          authenticated=false;
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

    @Override
    public byte[] opaqueDigest(String input)
    {
      try
      { 
        return MessageDigest.getInstance("SHA-256").digest
          ((getRealmName()+input).getBytes());
      }
      catch (NoSuchAlgorithmException x)
      { throw new RuntimeException("SHA256 not supported",x);
      }
    }
  }
}
  
  
