package spiralcraft.security.spi;

import java.security.Principal;
import java.util.Map;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.security.auth.AuthModule;
import spiralcraft.security.auth.AuthSession;
import spiralcraft.security.auth.Authenticator;
import spiralcraft.security.auth.Credential;

public abstract class AbstractAuthModule
  implements AuthModule
{

  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  
  protected Focus<AuthSession> sessionFocus;
  protected Focus<Map<String,Credential<?>>> credentialFocus;
  protected Class<? extends Credential<?>>[] acceptedCredentials;
  protected Authenticator authenticator;
  protected boolean usesActiveCredentials;
  protected String name;
  protected boolean debug;
  

  @Override
  public abstract Session createSession();

  public void setName(String name)
  { this.name=name;
  }

  @Override
  public String getName()
  { return name;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    Focus<Authenticator> authFocus
      =focusChain.findFocus(Authenticator.FOCUS_URI);
    if (authFocus==null)
    { throw new BindException("Authenticator not found in focus chain");
    }
    authenticator=authFocus.getSubject().get();
    if (authenticator==null)
    { 
      throw new BindException
        ("Authenticator must be available at binding time (reference is null)");
    }
    
    authenticator.registerCredentials(acceptedCredentials);
    
    sessionFocus=focusChain.<AuthSession>findFocus(AuthSession.FOCUS_URI);
    
    // Chain the credential subject- credentialFocus is used as authModule
    //   local focus- facets may be added
    credentialFocus
      =(Focus<Map<String,Credential<?>>>) 
        focusChain.chain(focusChain.getSubject());
    
    
    return focusChain;
  }

  public void setAcceptedCredentials
    (Class<? extends Credential<?>>[] acceptedCredentials)
  { 
    this.acceptedCredentials=acceptedCredentials;
    usesActiveCredentials
      =acceptedCredentials!=null
        && acceptedCredentials.length>0;
  }
  
  public abstract class AbstractSession
    implements Session
  {
    protected Principal principal;
    protected boolean authenticated;
    protected AuthSession authSession
      =sessionFocus.getSubject().get();
  
    @Override
    public abstract boolean authenticate();
    
    @Override
    public boolean isAuthenticated()
    { return authenticated;
    }
    
    @Override
    public Principal getPrincipal()
    { return principal;
    }
    
    @Override
    public void refresh()
    {
    }
    
    @Override
    public void credentialsChanged()
    {
      if (usesActiveCredentials)
      {
        principal=null;
        if (debug)
        { log.fine("Deauthenticating");
        }
        authenticated=false;
      }
    }
    
    /**
     * Logs out the user by clearing all credentials, attributes and principal 
     *   data and setting authenticated to false.
     */
    @Override
    public synchronized void logout()
    {
      principal=null;
      if (debug)
      { log.fine("Deauthenticating");
      }      
      authenticated=false;
    }    
  }
}
