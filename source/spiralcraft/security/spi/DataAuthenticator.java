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
import spiralcraft.log.ClassLog;

import java.net.URI;
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
 * @deprecated Use DataAuthModule instead via Authenticator.setAuthModules()
 */
@Deprecated
public class DataAuthenticator
  extends Authenticator
{
  
  private static final ClassLog log
    =ClassLog.getInstance(DataAuthenticator.class);
  
  private Queryable<?> providedSource;
  
  // XXX Make these both configurable
  private Type<?> loginDataType;

  private Query loginQuery;
  private BoundQuery<?,?> boundQuery;
  private Channel<String> usernameCredentialChannel;
  private Channel<String> usernameChannel;
  
  private TeleFocus<Tuple> comparisonFocus;
  private ThreadLocalChannel<Tuple> loginChannel;
  private Expression<Boolean> credentialComparison;
  private Channel<Boolean> comparisonChannel;
  
  protected volatile Principal principal;
  protected volatile boolean authenticated;
  
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
          +"     .saltedDigest(.username+.clearpass)"
          +"     )"
          +"   )"
          );
  
  }

  

  
  /**
   * @param source The Queryable which provides access to the login database
   */
  public void setSource(Queryable<?> source)
  { this.providedSource=source;
  }
  
  @Override
  public AuthSession createSession()
  { 
    DataAuthSession session=new DataAuthSession(this);
    session.setDebug(debug);
    return session;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Focus<?> bind(Focus<?> context)
    throws BindException
  { 
    // superclass provides a credentialFocus member field which 
    //   provides values for the various accepted credentials,
    //   named according to the credential class simple name.
    
    context=super.bind(context);


    Space space=null;
    // Resolve the source for the master credentials list
    if (providedSource==null && context!=null)
    { 
      // Look up the local Space to use as a source if no source was provided
      space=Space.find(context);
    }

    Queryable source=providedSource;
        
    if (source==null || space!=null)
    { source=space;
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
    
    usernameCredentialChannel
      =credentialFocus.bind(Expression.<String>create("UsernameCredential"));
    
    // Set up a comparison to check password/etc
    loginChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(loginDataType));
    
    comparisonFocus=new TeleFocus<Tuple>(credentialFocus,loginChannel);
    
    comparisonChannel=comparisonFocus.bind(credentialComparison);
    usernameChannel
      =comparisonFocus.bind(Expression.<String>create(".username"));
    
    return context;
  }
  

  public class DataAuthSession
    extends AuthSession
  {
    
    
    public DataAuthSession(Authenticator authenticator)
    { 
      super(authenticator);
      authenticate();
    }
        
    @Override
    public synchronized boolean authenticate()
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
      pushSession(this);
      if (debug)
      { log.fine("Attempting authentication");
      }
      
      try
      {
        
        String username=null;
        Tuple loginEntry=queryLoginEntry();

        if (loginEntry!=null)
        {

          // We have valid username in loginEntry
          //   run the password comparison expression

          boolean valid;
          loginChannel.push(loginEntry);
          try
          { 
            Boolean result=comparisonChannel.get();
            valid=(result!=null && result);
            if (debug)
            { log.fine("Token comparison returned "+result);
            }
            if (valid)
            { username=usernameChannel.get();
            }
          }
          finally
          { loginChannel.pop();
          }
          
          
          if (valid)
          {
          
            // cursor.discard()
            if (debug)
            { log.fine("valid login: "+loginEntry);
            }
          
          
            if (principal==null
                || !principal.getName().equals(username)
               )
            {
              final String name=username;
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
            { 
              log.fine
                ("failed login: no token match for "
                +usernameCredentialChannel.get()
                );
            }
            authenticated=false;
            return false;
          }
        }
        else
        { 
          if (debug)
          { log.fine("failed login: no username match for "+usernameCredentialChannel.get());
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
      catch (RuntimeException x)
      {
        x.printStackTrace();
        return false;
      }
      finally
      { popSession();
      }
    }


    
    private Tuple queryLoginEntry()
      throws DataException,SecurityException
    {
      SerialCursor<?> cursor=boundQuery.execute();
        
      try
      {
        if (cursor.next())
        { 
          Tuple loginEntry=cursor.getTuple();
          if (debug)
          { log.fine("Found user "+loginEntry.get("username"));
          }
          
          if (cursor.next())
          { 
            throw new SecurityException
              ("Cardinality Violation: Multiple Login records for user "
                +loginEntry.get("searchname")
              );
          }
          return loginEntry.snapshot();
        }
      }
      finally
      { cursor.close();
      }
      return null;

    }
    
    /**
     * @return The Principal currently authenticated in this session.
     * 
     * <P>In the case of Principal escalation, the most privileged Principal
     *   will be returned.
     */
    @Override
    public synchronized Principal getPrincipal()
    { return principal;
    }
  
    @Override
    public synchronized  boolean isAuthenticated()
    { 
      return authenticated;
    }    
    
    @Override
    public synchronized void logout()
    { 
      principal=null;
      authenticated=false;
      super.logout();
    }
  }
}
  
  
