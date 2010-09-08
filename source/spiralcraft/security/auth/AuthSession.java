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

import java.io.UnsupportedEncodingException;
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

  protected String digestAlgorithm="SHA-256";

  
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
  
  public void setDigestAlgorithm(String digestAlgorithm)
    throws NoSuchAlgorithmException
  {

    MessageDigest.getInstance(digestAlgorithm);
    this.digestAlgorithm=digestAlgorithm;
  }
  
  /**
   * Returns the local identifier for the account as shared between
   *   authentication providers.
   * 
   * @return
   */
  public synchronized String getAccountId()
  {
    push();
    try
    {
      if (primarySession!=null && primarySession.isAuthenticated())
      { return primarySession.getAccountId();
      } 
      else
      {
        for (AuthModule.Session session:sessions)
        { 
          if (session.getAccountId()!=null)
          { return session.getAccountId();
          }
        }
        return null;
      }
    }
    finally
    { pop();
    }
  }

  /**
   * Associate the contextual authentication tokens with the currently
   *   active Login
   * 
   * @param moduleName
   */
  public synchronized boolean associateLogin
    (String moduleName)
  { 
    push();
    try
    { return getModuleSession(moduleName).associateLogin();
    }
    finally
    { pop();
    }
  }
 
  public synchronized boolean isAuthenticated(String moduleName)
  { 
    push();
    try
    { return getModuleSession(moduleName).isAuthenticated();
    }
    finally
    { pop();
    }
  }

  public synchronized boolean isAuthenticated()
  { 
    
      
    if (primarySession!=null)
    { 
      push();
      try
      { return primarySession.isAuthenticated();
      }
      finally
      { pop();
      }
    }
    else
    { return false;
    }
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  private void push()
  { authenticator.pushSession(this);
  }
  
  private void pop()
  { authenticator.popSession();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Required downcast
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
  public synchronized boolean authenticate()
  {
    push();
    try
    { return authenticateModules();
    }
    finally
    { pop();
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
  public synchronized void refresh()
  {
    if (sessions==null)
    { return;
    }
    
    push();
    try
    {
      for (AuthModule.Session session:sessions)
      {
        if (session!=null)
        { session.refresh();
        }
      }
      recomputePrimarySession();
    }
    finally
    { pop();
    }
    
  }
  
  private AuthModule.Session getModuleSession(String moduleName)
  { 
    Integer sessionIndex=authenticator.getModuleMap().get(moduleName);
    if (sessionIndex!=null && sessions[sessionIndex]!=null)
    { return sessions[sessionIndex];
    }
    else
    { throw new IllegalArgumentException("AuthModule '"+moduleName+"' not found");
    }
    
  }
  
  private void credentialsChanged()
  {
    if (sessions==null)
    { return;
    }
    
    push();
    try
    {
      for (AuthModule.Session session:sessions)
      {
        if (session!=null)
        { session.credentialsChanged();
        }
      }
      recomputePrimarySession();
    }
    finally
    { pop();
    }
    
  }  
  
  /**
   * Called with this session in the thread context
   */
  private void recomputePrimarySession()
  {
    if (sessions!=null)
    {
      AuthModule.Session newPrimary=null;
      for (AuthModule.Session session : sessions)
      { 
        if (session!=null && session.isAuthenticated())
        { 
          if (newPrimary==null)
          { newPrimary=session;
          }
        }
      }
      if (newPrimary!=null && primarySession!=newPrimary)
      { 
        if (debug)
        { log.fine("New primary session is "+newPrimary);
        }        
        primarySession=newPrimary;
      }
    }
  }
  
  private boolean authenticateModules()
  {
    
    AuthModule[] modules=authenticator.getAuthModules();
    if (modules==null)
    { return false;
    }

    if (sessions==null)
    {  sessions=new AuthModule.Session[modules.length];
    }

    int i=0;
    
    for (AuthModule module: modules)
    {
      AuthModule.Session session=sessions[i];
      if (session==null)
      {
        session=module.createSession();
        sessions[i]=session;
        
      }
      
      if (session!=null)
      { session.authenticate();
      }
      i++;
    }
    recomputePrimarySession();
    return primarySession!=null && primarySession.isAuthenticated();
    
  }
  
  private void logoutModules()
  {
    push();
    try
    {
      for (int i=0;i<sessions.length;i++)
      {
        AuthModule.Session session=sessions[i];
        if (session!=null)
        { session.logout();
        }
      }
      primarySession=null;
      recomputePrimarySession();
    }
    finally
    { pop();
    }
  }
  
  
  /**
   * <p>Compute a message digest which includes the specified input 
   *   token (eg. a password in some form) for later comparison as an 
   *   authentication step. 
   * </p>
   * 
   * <p>The configured property digestAlgorithm is used. SHA-256 is
   *   the default algorithm.
   * </p>
   * 
   * <p>The realm name is used as a prepended salt at the current time to
   *   ensure uniqueness across realms
   * </p>
   *    
   * <p>The digest may be used as part of an authentication "ticket"
   * </p>
   * 
   * @param clearPass
   * @return
   */
  public byte[] saltedDigest(String input)
  {
    String realmName=authenticator.getRealmName();
    if (realmName!=null)
    {
      return digest(realmName+input);
    }
    else
    { 
      log.warning
        ("Security risk: A permanent Authenticator realmName should"
        +" be specified BEFORE generating token digests."
        );
      return digest(input);
    }
  }    
  
  /**
   * The RealmName used as part of the salt of a password hash to ensure 
   *   uniqueness across domains.
   * 
   * @return
   */
  public String getRealmName()
  { return authenticator.getRealmName();
  }
  
  /**
   * <p>Creates a SHA-256 digest of the specified string.
   * </p>
   * 
   * @param input
   * @return
   */
  public byte[] digest(String input)
  {
    try
    { 
      return MessageDigest.getInstance(digestAlgorithm).digest
        (input.getBytes("UTF-8"));
    }
    catch (NoSuchAlgorithmException x)
    { throw new RuntimeException(digestAlgorithm+" not supported",x);
    }
    catch (UnsupportedEncodingException x)
    { throw new RuntimeException("UTF-8 not supported",x);
    }
  }
    
  /**
   * <p>Associate arbitrary application state with this LoginSession
   * </p>
   * 
   * @param name
   * @param value
   */
  public synchronized<T> void setAttribute(String name,T value)
  { attributes.put(name,value);
  }
  
  /**
   * <p>Obtain arbitrary application state associated with this LoginSession
   * </p>
   * 
   * @param name
   */  
  @SuppressWarnings("unchecked")
  public synchronized <T> T getAttribute(String name)
  { return (T) attributes.get(name);
  }
  
  /**
   * Logs out the user by clearing all credentials, attributes and principal 
   *   data and destroying all AuthModule sessions
   */
  public synchronized void logout()
  {
    credentialList.clear();
    credentialMap.clear();
    attributes.clear();
    logoutModules();
    
  }
  
  /**
   * Clears any active credentials and allows any modules which depend on them
   *   to re-evaluate their authentication state.
   */
  public synchronized void clearCredentials()
  {
    credentialList.clear();
    credentialMap.clear();
    attributes.clear();
    credentialsChanged();
    
  }
  
}
