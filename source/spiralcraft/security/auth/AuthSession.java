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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import spiralcraft.log.ClassLog;
import spiralcraft.time.Clock;
import spiralcraft.util.refpool.URIPool;

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
    =URIPool.create("class:/spiralcraft/security/auth/AuthSession");

  protected static final Clock clock=Clock.instance();
  
  protected final ArrayList<Credential<?>> credentialList
    =new ArrayList<Credential<?>>();
  
  protected final HashMap<String,Credential<?>> credentialMap
    =new HashMap<String,Credential<?>>();

  private static String idOf(AuthPrincipal p)
  { return p!=null?p.getId():null;
  }

    
  protected boolean debug;


  
  protected final HashMap<String,Object> attributes
    =new HashMap<String,Object>();
  
  protected final Authenticator authenticator;
  protected AuthModule.Session[] sessions;
  protected AuthModule.Session primarySession;
  
  protected long stagedSince;

  protected AuthState authState;
  
  public AuthSession(Authenticator authenticator)
  { this.authenticator=authenticator;
  }
  
  /**
   * @return The login Principal currently authenticated in this session.
   * 
   * <p>In the case of Principal escalation, the most privileged Principal
   *   will be returned.
   * </p>
   */
  public synchronized AuthPrincipal getPrincipal()
  { 
    if (primarySession!=null)
    { return primarySession.getPrincipal();
    }
    return null;

  }
  
  public DigestFunction getDigestFunction()
  { return authenticator.getDigestFunction();
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
      { return null;
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
  
    int[] path=resolve(moduleName);
    if (path==null)
    { throw new IllegalArgumentException("Module '"+moduleName+"' not found in AuthSession");
    }    
    if (sessions==null || sessions[path[0]]==null)
    { return false;
    }
    push();
    try
    { return sessions[path[0]].associateLogin(path,1);
    }
    finally
    { pop();
    }
  
  

  }

  
  public synchronized boolean isAuthenticated(String moduleName)
  { 

    int[] path=resolve(moduleName);
    if (path==null)
    { throw new IllegalArgumentException("Module '"+moduleName+"' not found in AuthSession");
    }
    
    if (sessions==null || sessions[path[0]]==null)
    { return false;
    }

    push();
    try
    { return sessions[path[0]].isAuthenticated(path,1);
    }
    finally
    { pop();
    }
  }

  private int[] resolve(String moduleName)
  { return authenticator.getSessionIndex(moduleName);
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
  

  public synchronized void discardCredentials(Set<String> types)
  {
    for (String type: types)
    { credentialMap.remove(type);
    }
    authState=null;
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
    
  public synchronized AuthState getAuthState()
  {
    if (authState==null)
    { authState=calcAuthState();
    }
    return authState;
  }
  
  private AuthState calcAuthState()
  {
    if (getIdentifiedPrincipal()==null)
    { return AuthState.DEFAULT;
    }
    else return new AuthState(this);
  }
  
  void collectNeeded(List<AuthState.Need> out) 
  { 
    if (sessions==null || isAuthenticated())
    { return;
    }
    String id=idOf(getIdentifiedPrincipal());
    for (int i=0;i<sessions.length;i++)
    {
      var s=sessions[i];
      if (s==null || s.isAuthenticated())
      { continue;
      }
      String sid=idOf(s.getIdentifiedPrincipal());
      if (sid!=null && sid.equals(id))
      { s.collectNeeded(out,String.valueOf(i));
      }
    }
  }
  
  void collectPassed(List<String> out)
  {
    if (sessions==null)
    { return;
    }
    String id=idOf(getIdentifiedPrincipal());
    for (int i=0;i<sessions.length;i++)
    {
      var s=sessions[i];
      if (s==null)
      { continue;
      }
      String sid=idOf(s.getIdentifiedPrincipal());
      if (sid!=null && sid.equals(id))
      { s.collectPassed(out,String.valueOf(i));
      }
    }
  }
  
    
  public synchronized void addCredentials(Credential<?>[] credentials)
  { 
    if (debug)
    { log.fine("Adding credentials "+Arrays.deepToString(credentials));
    }
    
    Set<String> changed=new HashSet<>();
    
    for (Credential<?> cred: credentials)
    { 
      String type=cred.getClass().getSimpleName();
      Credential<?> prior=credentialMap.get(type);
      if (prior==null || !Objects.deepEquals(prior.getValue(),cred.getValue()))
      { changed.add(type);
      }
      
      credentialList.removeIf(c -> c.getClass()==cred.getClass());

      credentialList.add(cred);
      credentialMap.put(type,cred);
    }
    if (!changed.isEmpty())
    { 
      credentialsChanged(changed);
      authState=null;
    }
    if (stagedSince==0) 
    { stagedSince=clock.approxTimeMillis(); 
    }
    
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
    { 
      authState=null;
      return authenticateModules();
    }
    finally
    { pop();
    }
  }
  
  /**
   * Indicate whether the current Principal has the specified Permission
   * 
   * @param permission
   * @return
   */
  public boolean hasPermission(Permission permission)
  { 
    push();
    try
    { return authenticator.authorizer.hasPermission(this,permission);
    }
    finally
    { pop();
    }
  }
  
  /**
   * Indicate whether the current Principal has all of the specified 
   *   Permissions
   * 
   * @param permission
   * @return
   */
  public boolean hasPermissions(Permission[] permissions)
  { 
    push();
    try
    { return authenticator.authorizer.hasPermissions(this,permissions);
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
    authState=null;
    if (sessions==null)
    { return;
    }
    
    push();
    try
    {
      long timeout=authenticator.getStagedTimeoutMinutes()*60_000L;
      if (timeout>0
           && stagedSince > 0
           && !credentialList.isEmpty()
           && clock.approxTimeMillis()-stagedSince > timeout
           && !isAuthenticated()
         )
      { clearCredentials();
      }
          
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

  public synchronized AuthPrincipal getIdentifiedPrincipal()
  {
    if (isAuthenticated())
    { return getPrincipal();
    }
    if (sessions==null)
    { return null;
    }
    
    for (var session:sessions)
    {
      if (session!=null )
      { 
        AuthPrincipal p=session.getIdentifiedPrincipal();
        if (p!=null)
        { return p;
        }
      }
    }
    return null;
  }
  
  
  private void credentialsChanged(Set<String> changed)
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
        { session.credentialsChanged(changed);
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
    if (primarySession!=null && primarySession.isAuthenticated())
    {
      stagedSince=0;
      return true;
    }
    else
    { return false;
    }
    
  }
  
  private void logoutModules()
  {
    if (sessions==null)
    { return;
    }
    
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
  { return authenticator.saltedDigest(input);
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

  public byte[] digest(String input)
  { return authenticator.digest(input);
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
    stagedSince=0;
    authState=null;
    
  }
  
  /**
   * Clears any active credentials and allows any modules which depend on them
   *   to re-evaluate their authentication state.
   */
  public synchronized void clearCredentials()
  {
    Set<String> changed=new HashSet<>(credentialMap.keySet());
    credentialList.clear();
    credentialMap.clear();
    attributes.clear();
    credentialsChanged(changed); 
    stagedSince=0;
    authState=null;
  }
  
  
}
