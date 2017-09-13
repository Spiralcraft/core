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
package spiralcraft.security.auth;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import spiralcraft.common.ContextualException;
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.BeanFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.ClassUtil;

//import spiralcraft.log.ClassLog;


/**
 * <p>Authenticates a client using one or more sets of credentials. 
 * </p>
 *   
 * <p>An Authenticator provides an AuthSession to handle the exchange
 *   of credentials and to communicate the status of the authentication process
 *   to the client.
 * </p>
 */
public class Authenticator
  implements Contextual,Declarable
{
  private final ClassLog log
    =ClassLog.getInstance(Authenticator.class);
  
  public static final URI FOCUS_URI
    =BeanReflector.getInstance(Authenticator.class).getTypeURI();

//  private static final ClassLog log
//    =ClassLog.getInstance(Authenticator.class);
  
  protected String realmName;
  protected Binding<String> realmNameX;
  protected ThreadLocalChannel<AuthSession> sessionChannel;

  protected GenericReflector<AuthSession> sessionReflector;
  
  protected Focus<AuthSession> sessionFocus;
  protected final HashMap<String,Credential<?>> protoMap
    =new HashMap<String,Credential<?>>();
  protected Class<? extends Credential<?>>[] acceptedCredentials;
  
  protected Focus<Map<String,Credential<?>>> credentialFocus;
  protected AuthModule[] authModules;
  protected boolean debug;

  protected HashMap<String,Integer> moduleMap;
  
  protected String digestAlgorithm="SHA-256";

  protected Authorizer authorizer;
  
  protected DeclarationInfo declarationInfo;
  protected boolean bound;
  
  protected DigestFunction digestFunction
    =new DigestFunction()
  {

    @Override
    public byte[] digest(
      String clearToken)
    { return saltedDigest(clearToken);
    }
  };
  
  /**
   * @return The name of the realm this Authenticator will be serving.
   * 
   * <p>The realm name should be as unique and as stable as possible. 
   * Application names that may change (eg. paths in a web tree) should be 
   * avoided, as the realm name is a factor in
   * authentication cryptography, and may require that all passwords be reset
   * or cause other data loss if it is changed.
   * </p>
   * 
   * <p>ie. MyRealmName@MyDomain.com
   * </p>
   * 
   */
  public String getRealmName()
  { return realmName;
  }
  
  public void setRealmName(String realmName)
  { 
    if (sessionChannel!=null)
    { throw new IllegalStateException("Cannot change realm name after binding");
    }
    this.realmName=realmName;
  }
  
  public void setRealmNameX(Binding<String> realmNameX)
  { this.realmNameX=realmNameX;
  }

  public void setDigestAlgorithm(String digestAlgorithm)
    throws NoSuchAlgorithmException
  {

    MessageDigest.getInstance(digestAlgorithm);
    this.digestAlgorithm=digestAlgorithm;
  }
  
  
  /**
   * <P>Create a new AuthSession, which represents a login session in the
   *   realm of the Authenticator.
   */
  public AuthSession createSession()
  { 
    
    AuthSession session=new AuthSession(this);
    session.setDebug(debug);
    return session;
  }
  
  AuthModule[] getAuthModules()
  { return authModules;
  }
  
  public void setAuthModules(AuthModule[] authModules)
  { this.authModules=authModules;
  }
  
  public void setAuthorizer(Authorizer authorizer)
  { this.authorizer=authorizer;
  }
  
  public DigestFunction getDigestFunction()
  { return digestFunction;
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }

  @Override
  public void setDeclarationInfo(DeclarationInfo di)
  { this.declarationInfo=di;
  }
  
  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
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
    String realmName=getRealmName();
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
   * <p>Provide the Authenticator with a Focus for it to resolve data sources
   *   and other resources from the context. The context can be null, as long
   *   as no contextual resources are required by the specific Authenticator.
   * </p>
   *   
   * @param context
   * @throws BindException
   */
  @Override
  public Focus<?> bind(Focus<?> context)
    throws ContextualException
  { 
    
    if (realmNameX!=null)
    {
      realmNameX.bind(context);
      setRealmName(realmNameX.get());
    }
    this.sessionReflector
      =new GenericReflector<AuthSession>
        (BeanReflector.<AuthSession>getInstance(AuthSession.class));
    
    
    this.sessionChannel
      =new ThreadLocalChannel<AuthSession>
        (this.sessionReflector);
    
    if (context!=null)
    { this.sessionFocus=new SimpleFocus<AuthSession>(context,sessionChannel);
    }
    else
    { this.sessionFocus=new SimpleFocus<AuthSession>(sessionChannel);
    }
    sessionFocus.addFacet(new BeanFocus<Authenticator>(this));
    
    credentialFocus
      =sessionFocus.chain(new CredentialSetChannel(protoMap,sessionChannel));

    if (authModules!=null)
    {
      moduleMap=new HashMap<String,Integer>();
      int i=0;
      for (AuthModule authModule:authModules)
      { 
        authModule.bind(credentialFocus);
        if (authModule.getName()!=null)
        { moduleMap.put(authModule.getName(),i);
        }
        i++;
      }
    }
    if (authorizer!=null)
    { 
      authorizer.bind(sessionFocus);
//      sessionFocus.addFacet(new BeanFocus<Authorizer>(authorizer));
      
    }
    bound=true;
    return sessionFocus;
  }
  
  public void pushSession(AuthSession session)
  { sessionChannel.push(session);
  }
  
  public void popSession()
  { sessionChannel.pop();
  }
  
  public Integer getSessionIndex(String moduleName)
  { 
    if (moduleMap==null)
    { 
      if (bound)
      { log.warning("No moduleMap for authenticator defined at "+getDeclarationInfo());
      }
      else
      { log.warning("Authenticator is not bound "+getClass().getName()+"  "+getDeclarationInfo());
      }
      return null;
    }
    return moduleMap.get(moduleName);
  }
  

  
  public Role[] getRolesForPrincipal(Principal principal)
  { 
    
    // Look up principal id in 
    return null;
    
  }
  
  protected void setAcceptedCredentials
    (Class<? extends Credential<?>>[] acceptedCredentials)
  { 
    this.acceptedCredentials=acceptedCredentials;
    registerCredentials(acceptedCredentials);
  }
  
  public void registerCredentials(Class<? extends Credential<?>>[] classes)
  {
    if (classes==null)
    { return;
    }
    for (Class<? extends Credential<?>> credClass: classes)
    { 
      if (protoMap.get(credClass.getSimpleName())==null)
      { 
        try
        { protoMap.put(credClass.getSimpleName(),ClassUtil.construct(credClass));
        }
        catch (InvocationTargetException x)
        { 
          throw new IllegalArgumentException
            ("Credential class "
            +credClass.getName()
            +" could not be instantiated: "
            +x
            ,x
            );
        }
      }
    }
  }    
  
}





