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
package spiralcraft.security.auth;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import spiralcraft.log.ClassLog;

/**
 * <p>Represents the state of the authentication process from a client's
 *   perspective.
 * </p>
 *   
 * <p>Provides an interface to the credentials received from the client
 *   for authentication functionality.
 * </p>
 * 
 * @author mike
 */
public class AuthSession
{
  private static final ClassLog log
    =ClassLog.getInstance(AuthSession.class);
  
  public static final URI FOCUS_URI
    =URI.create("class:/spiralcraft/security/auth/AuthSession");

  protected final ArrayList<Credential<?>> credentialList
    =new ArrayList<Credential<?>>();
  
  protected final HashMap<String,Credential<?>> credentialMap
    =new HashMap<String,Credential<?>>();
  
  protected boolean debug;


  
  protected final HashMap<String,Object> attributes
    =new HashMap<String,Object>();
  
  protected final Authenticator authenticator;
  protected AuthModule.Session[] sessions;
  protected AuthModule.Session primarySession;
  
  public AuthSession(Authenticator authenticator)
  { this.authenticator=authenticator;
  }
  
  /**
   * @return The Principal currently authenticated in this session.
   * 
   * <P>In the case of Principal escalation, the most privileged Principal
   *   will be returned.
   */
  public synchronized Principal getPrincipal()
  { 
    if (primarySession!=null)
    { return primarySession.getPrincipal();
    }
    return null;

  }
  
  public synchronized boolean isAuthenticated()
  { 
    if (primarySession!=null)
    { return primarySession.isAuthenticated();
    }
    else
    { return false;
    }
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @SuppressWarnings("unchecked") // Required downcast
  protected synchronized <T extends Credential> T 
    getCredential(Class<T> clazz)
  {
    for (Credential cred: credentialList)
    { 
      if (cred.getClass()==clazz)
      { return (T) cred;
      }
    }
    return null;
  }
  
  /**
   * Called internally to retreived the Credential map
   * 
   * @return
   */
  Map<String,Credential<?>> getCredentialMap()
  { return credentialMap;
  }
    
  public synchronized void addCredentials(Credential<?>[] credentials)
  { 
    if (debug)
    { 
      log.fine("Adding credentials "+Arrays.deepToString(credentials));
      

    }
    

    for (Credential<?> cred: credentials)
    { 
      Iterator<Credential<?>> it=credentialList.iterator();
      while (it.hasNext())
      { 
        if (it.next().getClass()==cred.getClass())
        { it.remove();
        }
      }
      credentialList.add(cred);
      credentialMap.put(cred.getClass().getSimpleName(),cred);
    }
    credentialsChanged();
  }

  /**
   * Authenticate the supplied credentials.
   * 
   * @return Whether a successful authentication has been performed 
   */
  public boolean authenticate()
  {
    authenticator.pushSession(this);
    try
    { return authenticateModules();
    }
    finally
    { authenticator.popSession();
    }
  }
  
  /**
   * <p>Provide an opportunity for modules to update the authentication state
   *   based on the application context.
   * </p>
   * 
   * <p>The resulting change in authentication state may result in a new
   *   primary session, possibly originating from a different module.
   * </p>
   */
  public void refresh()
  {
    if (sessions==null)
    { return;
    }
    
    authenticator.pushSession(this);
    try
    {
      AuthModule.Session newPrimary=null;
      for (AuthModule.Session session:sessions)
      {
        if (session!=null)
        { session.refresh();
        }
        if (newPrimary==null && session.isAuthenticated())
        { newPrimary=session;
        }
        if (newPrimary!=null && primarySession!=newPrimary)
        { primarySession=newPrimary;
        }
      }
      
    }
    finally
    { authenticator.popSession();
    }
    
  }
  
  private void credentialsChanged()
  {
    if (sessions==null)
    { return;
    }
    
    authenticator.pushSession(this);
    try
    {
      for (AuthModule.Session session:sessions)
      {
        if (session!=null)
        { session.credentialsChanged();
        }
      }
    }
    finally
    { authenticator.popSession();
    }
    
  }  
  
  private boolean authenticateModules()
  {
    
    AuthModule[] modules=authenticator.getAuthModules();

    sessions=new AuthModule.Session[modules.length];

    int i=0;

    for (AuthModule module: modules)
    {
      AuthModule.Session session=module.createSession();
      sessions[i++]=session;
      if (session!=null)
      {
        // First authenticated Session becomes the primary Session
        //   which determines the result of 
        //   isAuthenticated() and getPrincipal()
        if (session.isAuthenticated() && primarySession==null)
        { primarySession=session;
        }
      }
    }
    return primarySession!=null;
    
  }
  
  private void logoutModules()
  {
    authenticator.pushSession(this);
    try
    {
      for (int i=0;i<sessions.length;i++)
      {
        AuthModule.Session session=sessions[i];
        if (session!=null)
        { session.logout();
        }
        sessions[i]=null;
      }
      primarySession=null;
    }
    finally
    { authenticator.popSession();
    }
  }
  
  
  /**
   * <p>Compute a message digest which includes the specified input token
   *   (eg. a password in some form) for later comparison as an authentication
   *   step. 
   * </p>
   * 
   * <p>The realm name is used as a salt at the current time.
   * </p>
   * 
   * <p>Future behavior will permit incorporating
   *   a one time random secret and a random secret associated with the user
   *   account via the AuthModule interface.
   * </p>
   *    
   * <p>The digest may be used as part of an authentication "ticket"
   * </p>
   * 
   * @param clearPass
   * @return
   */
  public byte[] opaqueDigest(String input)
  {
    try
    { 
      // authModule.getAccountSecret()
      // randomSalt(randomSaltLength)
      return MessageDigest.getInstance("SHA-256").digest
        ((authenticator.getRealmName()+input).getBytes());
    }
    catch (NoSuchAlgorithmException x)
    { throw new RuntimeException("SHA-256 not supported",x);
    }
  }    
    
  /**
   * <p>Associate arbitrary application state with this LoginSession
   * </p>
   * 
   * @param name
   * @param value
   */
  public <T> void setAttribute(String name,T value)
  { attributes.put(name,value);
  }
  
  /**
   * <p>Obtain arbitrary application state associated with this LoginSession
   * </p>
   * 
   * @param name
   */  
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String name)
  { return (T) attributes.get(name);
  }
  
  /**
   * Logs out the user by clearing all credentials, attributes and principal 
   *   data and setting authenticated to false.
   */
  public synchronized void logout()
  {
    credentialList.clear();
    credentialMap.clear();
    attributes.clear();
    logoutModules();
    
  }
  
}
