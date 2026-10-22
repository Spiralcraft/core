package spiralcraft.security.spi;

import java.util.List;
import java.util.Set;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.security.auth.AuthModule;
import spiralcraft.security.auth.AuthPrincipal;
import spiralcraft.security.auth.AuthState;
import spiralcraft.security.auth.ModuleSessionContext;

public class AuthModuleGroup
  implements AuthModule
{
  private final ClassLog log
    =ClassLog.getInstance(AuthModuleGroup.class);
    
  private AuthModule[] modules=new AuthModule[0];
  protected ThreadLocalChannel<ModuleSessionContext> sessionChannel;
  private String name;

  public void setModules(AuthModule[] modules)
  { this.modules=modules;
  } 
  
  public AuthModule[] getModules()
  { return this.modules;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    this.sessionChannel
      =new ThreadLocalChannel<>(BeanReflector.getInstance(ModuleSessionContext.class));
    Focus<?> localChain=focusChain.chainFacet(sessionChannel);
    int i=0;
    for (var module: modules)
    { 
      module.bind(localChain);
      if (module.getName()==null)
      { 
        log.warning("Unnamed module at position "+i+" in group '"+name
          +"'");
      }
      i++;
    }
    return focusChain;
  }

  @Override
  public String getName()
  { return name;
  }

  public void setName(String name)
  { this.name=name;
  }

  @Override
  public Session createSession()
  { 
    Session[] sessions=new Session[modules.length];
    int i=0;
    for (var module: modules)
    { sessions[i++]=module.createSession();
    }
    
    return new GroupSession(sessions);
  }
  
  class GroupSession
    implements AuthModule.Session,ModuleSessionContext
  {

    private Session[] sessions;
    
    private GroupSession(Session[] sessions)
    { this.sessions=sessions;
    }
    
    @Override
    public String getAccountId()
    {
      try(var scope=sessionChannel.with(this))
      {     
        return isAuthenticated()? sessions[0].getAccountId() : null;
      }
    }
    
    @Override
    public Session[] getChildSessions()
    { return sessions;
    }

    @Override
    public AuthPrincipal getIdentifiedPrincipal()
    {
      try(var scope=sessionChannel.with(this))
      {     
    
        for (var session:sessions)
        {
          if (session!=null && session.isAuthenticated())
          { 
            AuthPrincipal p=session.getPrincipal();
            if (p!=null)
            { return p;
            }
          }
        }
        return null;
      }
    }
    
    /**
     * Implementation for a group is to not propogate this. Associations are
     *   narrowly targeted at specific modules only.
     */
    @Override
    public boolean associateLogin()
    { return false;
    }

    @Override
    public boolean authenticate()
    {
      try(var scope=sessionChannel.with(this))
      {     

        String id=null;
        for (var session: sessions)
        { 
          if (!session.authenticate())
          { break;
          }
          AuthPrincipal p=session.getPrincipal();
          if (p==null)
          { continue;
          }
          if (id==null)
          { 
            id=p.getId();
            continue;
          }
          if (!id.equals(p.getId()))
          {
            session.logout();
            if (!session.authenticate())
            { break;
            }
            p=session.getPrincipal();
            if (p!=null && !id.equals(p.getId()))
            { break;
            }
          }
        }
        return isAuthenticated();      
      }
    }

    @Override
    public void logout()
    {
      try(var scope=sessionChannel.with(this))
      { 
        for (var session: sessions)
        { 
          try
          { session.logout();
          }
          catch (Exception x)
          { log.warning("Error logging out session",x);
          }
        }
      }
      
    }

    @Override
    public boolean isAuthenticated()
    {
      if (sessions.length==0)
      { return false;
      }
      
      String id=null;
      boolean ret=false;

      try(var scope=sessionChannel.with(this))
      {     
      
        for (var session: sessions)
        {
          if (!session.isAuthenticated())
          { 
            ret=false;
            break;
          }
          AuthPrincipal p=session.getPrincipal();
          String sessionId=p!=null?p.getId():null;
          
          if (sessionId!=null && !sessionId.equals(id))
          { 
            if (id==null)
            { id=sessionId;
            }
            else
            { 
              ret=false;
              break;
            }
          }
          ret=true;
        }
        return ret && id!=null;
      }
    }

    @Override
    public void refresh()
    {
      try(var scope=sessionChannel.with(this))
      {     
      
        for (var session: sessions)
        { session.refresh();
        }
        
      }
      
    }

    @Override
    public void credentialsChanged()
    {
      try(var scope=sessionChannel.with(this))
      {     
        for (var session: sessions)
        { session.credentialsChanged();
        }
      }
      
    }

    @Override
    public void credentialsChanged(Set<String> changed)
    {
      try(var scope=sessionChannel.with(this))
      {     
        for (var session: sessions)
        { session.credentialsChanged(changed);
        }
      }
      
    }

    public void collectNeeded(List<AuthState.Need> out,String path) 
    { 
      int i=0;
      for (var session:sessions)
      {
        if (!session.isAuthenticated())
        { 
          session.collectNeeded(out, path+"."+i);
          break;
        }
        i++;
      }
    }
    
    public void collectPassed(List<String> out,String path) 
    { 
      int i=0;
      for (var session:sessions)
      {
        if (session.isAuthenticated())
        { 
          session.collectPassed(out, path+"."+i);
        }
        i++;
      }
    }   
    
    @Override
    public AuthPrincipal getPrincipal()
    {
      try(var scope=sessionChannel.with(this))
      {     
        return isAuthenticated() ? sessions[0].getPrincipal() : null;
      }      
    }

    @Override
    public boolean associateLogin(int[] path,int depth)
    {
    
      if (depth==path.length)
      { return associateLogin();
      }

      Session child=childAt(path,depth);
      if (child==null)
      { return false;
      }
      try(var scope=sessionChannel.with(this))
      { return child.associateLogin(path,depth+1);
      }
    }

    @Override
    public boolean isAuthenticated(int[] path,int depth)
    {
      if (depth==path.length)
      { return isAuthenticated();
      }
      try(var scope=sessionChannel.with(this))
      {     
        Session child=childAt(path,depth);
        return child!=null && child.isAuthenticated(path,depth+1);
      }
    }

    private Session childAt(int[] path,int depth)
    {
      int i=path[depth];
      if (i>=sessions.length)
      { throw new IllegalStateException("Index "+i+" out of range at depth "+depth);
      }
      return sessions[i];
    }
    

  }
}