package spiralcraft.security.spi;

import java.security.Principal;
import java.util.Map;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.security.auth.AuthModule;
import spiralcraft.security.auth.AuthSession;
import spiralcraft.security.auth.Authenticator;
import spiralcraft.security.auth.Credential;

public abstract class AbstractAuthModule
  implements AuthModule
{

  protected Focus<AuthSession> sessionFocus;
  protected Focus<Map<String,Credential<?>>> credentialFocus;
  protected Class<? extends Credential<?>>[] acceptedCredentials;
  protected Authenticator authenticator;
  protected boolean usesActiveCredentials;
  

  @Override
  public abstract Session createSession();

  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
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
    
    credentialFocus=(Focus<Map<String,Credential<?>>>) focusChain;
    
    return focusChain;
  }

  protected void setAcceptedCredentials
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
  
    public abstract boolean authenticate();
    
    public boolean isAuthenticated()
    { return authenticated;
    }
    
    public Principal getPrincipal()
    { return principal;
    }
    
    public void refresh()
    {
    }
    
    public void credentialsChanged()
    {
      if (usesActiveCredentials)
      {
        principal=null;
        authenticated=false;
      }
    }
    
    /**
     * Logs out the user by clearing all credentials, attributes and principal 
     *   data and setting authenticated to false.
     */
    public synchronized void logout()
    {
      principal=null;
      authenticated=false;
    }    
  }
}
