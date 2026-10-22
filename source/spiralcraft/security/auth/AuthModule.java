//
// Copyright (c) 2010 Michael Toth
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import spiralcraft.lang.Contextual;

/**
 * Authenticates a set of credentials
 * 
 * @author mike
 *
 */
public interface AuthModule
  extends Contextual
{
  /**
   * The name by which clients will reference the state
   *   of a specific AuthModule.Session
   * 
   * @return
   */
  public String getName();
  
  /**
   * Authenticate the credentials provided to the AuthSession
   * 
   * @return A Session, if the module has anything to contribute to the
   *   authentication process. 
   */
  Session createSession();
  
  /**
   * Return any child modules of this module
   * @return
   */
  default AuthModule[] getModules()
  { return null;
  }
  
  /**
   * Register the names of this module's descendants in the module map,
   * as index paths starting from this module's own path.
   *
   * Called by the Authenticator before bind(). The map is live, so bind()
   * can resolve modules that have already been registered.
   *
   * @param moduleMap name -> index path, shared across the whole tree
   * @param prefix this module's own path
   */
  default void mapChildren(HashMap<String,int[]> moduleMap,int[] prefix)
  {
    AuthModule[] children=getModules();
    if (children==null)
    { return;
    }
    for (int i=0;i<children.length;i++)
    {
      AuthModule child=children[i];
      int[] path=Arrays.copyOf(prefix,prefix.length+1);
      path[prefix.length]=i;

      String name=child.getName();
      if (name!=null)
      { 
        int[] existing=moduleMap.putIfAbsent(name,path);
        if (existing!=null)
        { 
          throw new IllegalStateException
            ("Duplicate AuthModule name '"+name+"' at "
            +Arrays.toString(path)+" (already mapped at "
            +Arrays.toString(existing)+")"
            );
        }
      }
      child.mapChildren(moduleMap,path);
    }
  }  
  
  public interface Session
  {
    /**
     *
     * @return The principalId that this module session authenticated, or
     *   null if nothing was authenticated
     */
    public String getAccountId();

    /**
     * Associate the contextual authentication tokens with the currently
     *   active Login. Used to bind an external credential to the common 
     *   principal within a modules data store.
     */   
    public boolean associateLogin();


    /**
     * Attempt to authenticate the credentials in this session 
     */   
    public boolean authenticate();
    
    public void logout();
    
    public boolean isAuthenticated();
    
    public void refresh();
    
    public void credentialsChanged();
    
    default void credentialsChanged(Set<String> changedTypes)
    { credentialsChanged();   // old behavior for modules that don't override
    }
        
    public AuthPrincipal getPrincipal();
    
    public default Session[] getChildSessions()
    { return null;
    } 
    
    public default boolean associateLogin(int[] path,int depth)
    {
      if (depth!=path.length)
      { throw new IllegalStateException("Path descends into a leaf at depth "+depth);
      }
      return associateLogin();
    }

    public default boolean isAuthenticated(int[] path,int depth)
    {
      if (depth!=path.length)
      { throw new IllegalStateException("Path descends into a leaf at depth "+depth);
      }
      return isAuthenticated();
    }
    
    public default AuthPrincipal getIdentifiedPrincipal() 
    { return isAuthenticated()?getPrincipal():null; 
    } 
    
    default void collectNeeded(List<AuthState.Need> out,String path) 
    { 
    }
    
    default void collectPassed(List<String> out,String path) 
    { 
    }    
  }
}
