package spiralcraft.security.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class AuthState
{
  static final AuthState DEFAULT=new AuthState();
  
  final boolean authenticated;
  final AuthPrincipal identifiedPrincipal;
  final List<Need> needed=new ArrayList<>();
  final List<String> passed=new ArrayList<>();    
  
  AuthState()
  { 
    authenticated=false;
    identifiedPrincipal=null;
  }
  
  AuthState(AuthSession authSession)
  {
    this.authenticated=authSession.isAuthenticated();
    this.identifiedPrincipal=authSession.getIdentifiedPrincipal();

    authSession.collectNeeded(needed);

    authSession.collectPassed(passed);
  }
  
  public boolean isAuthenticated()
  { return authenticated;
  }
  
  public String getIdentifiedPrincipalId()
  { return identifiedPrincipal!=null?identifiedPrincipal.getId():null;
  }
  
  public Need[] getNeeded()
  { return needed.toArray(new Need[needed.size()]);
  }
       // next steps of logins in progress
  public String[] getPassed()
  { return passed.toArray(new String[passed.size()]);
  }

  public static final class Need
  {
    private final String name;
    private final Set<String> credentialTypes;
    
    public Need(String name,Set<String> credentialTypes)
    { 
      this.name=name;
      this.credentialTypes=Collections.unmodifiableSet(credentialTypes);
    }
    
    public String getModule()
    { return name;
    } 
                 // name, or dotted path "0.1"
    public Set<String> getCredentialTypes()
    { return credentialTypes;
    }
  }
}