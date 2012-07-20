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
package spiralcraft.security.spi;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;

import spiralcraft.common.ContextualException;
import spiralcraft.data.Type;
import spiralcraft.data.util.RelationalMap;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.security.auth.Role;
import spiralcraft.util.ArrayUtil;

/**
 * 
 * @author mike
 *
 * @param <TprincipalData>
 * @param <TprincipalId>
 */
public class DataAuthorizer<TprincipalData,TprincipalId>
  extends AbstractAuthorizer
{
  
  private Type<?> principalRoleType;

  private Type<TprincipalData> principalType;
  private String principalNameFieldName;
  private String principalIdFieldName;
  private String roleIdFieldName;
  private String roleMapPrincipalIdFieldName;

  
  private ThreadLocalChannel<String> principalNameChannel;
  
  private RelationalMap<TprincipalData,TprincipalData,String,String> principalMap;
  private ThreadLocalChannel<TprincipalData> principalDataChannel;
  
  private RelationalMap<TprincipalId,TprincipalId,TprincipalData,TprincipalData>
    principalIdMap;
  
  private ThreadLocalChannel<TprincipalId> principalIdChannel;
  
  private ThreadLocalChannel<TprincipalId[]> principalIdsChannel;
  private Channel<URI[]> roleIdsChannel;

  private RelationalMap<URI[],URI,TprincipalId[],TprincipalId>
    roleIdMap;
  
  private Binding<URI>[] roleBindings;
  
  @Override
  public Focus<?> bind(Focus<?> chain) 
    throws ContextualException
  { 
    chain=super.bind(chain);
    
    principalNameChannel
      =new ThreadLocalChannel<String>
        (BeanReflector.<String>getInstance(String.class));
      
    principalMap
      =new RelationalMap<TprincipalData,TprincipalData,String,String>();
    principalMap.setType(principalType);
    principalMap.setUpstreamFieldName(principalNameFieldName);
    principalMap.setUnique(true);
    principalMap.bind(chain);
    
    principalDataChannel
      =new ThreadLocalChannel<TprincipalData>
        (principalMap.bindChannel(principalNameChannel,chain,null),true);
    
    Focus<?> principalDataFocus=chain.chain(principalDataChannel);
    
    principalIdMap
      =new RelationalMap<TprincipalId,TprincipalId,TprincipalData,TprincipalData>();
    principalIdMap.setType(principalType);
    principalIdMap.setDownstreamFieldName(principalIdFieldName);
    principalIdMap.setUnique(true);
    principalIdMap.bind(chain);
    
    principalIdChannel
      =new ThreadLocalChannel<TprincipalId>
        (principalIdMap.bindChannel(principalDataChannel,chain,null),true);
    
    principalIdsChannel
      =new ThreadLocalChannel<TprincipalId[]>
        (ArrayReflector.getInstance(principalIdChannel.getReflector()));
          
    if (principalRoleType!=null)
    {
      roleIdMap
        =new RelationalMap<URI[],URI,TprincipalId[],TprincipalId>();
      roleIdMap.setType(principalRoleType);
      roleIdMap.setDownstreamFieldName(roleIdFieldName);
      roleIdMap.setUpstreamFieldName(roleMapPrincipalIdFieldName);
      roleIdMap.bind(chain);
      
      roleIdsChannel
        =roleIdMap.bindChannel(principalIdsChannel,chain,null);
    }
        
    if (roleBindings!=null)
    {
      for (Binding<URI> binding:roleBindings)
      { binding.bind(principalDataFocus);
      }
    }
    return chain;
        
  }
  
  
  public void setPrincipalRoleType(Type<?> principalRoleType)
  { this.principalRoleType=principalRoleType;
  }
  
  public void setRoleMapPrincipalIdFieldName(String fieldName)
  { this.roleMapPrincipalIdFieldName=fieldName;
  }
  
  public void setPrincipalType(Type<TprincipalData> principalType)
  { this.principalType=principalType;
  }
  
  public void setPrincipalNameFieldName(String principalNameFieldName)
  { this.principalNameFieldName=principalNameFieldName;
  }

  public void setPrincipalIdFieldName(String principalIdFieldName)
  { this.principalIdFieldName=principalIdFieldName;
  }

  public void setRoleIdFieldName(String roleIdFieldName)
  { this.roleIdFieldName=roleIdFieldName;
  }
  
  public void setRoleBindings(Binding<URI>[] roleBindings)
  { this.roleBindings=roleBindings;
  }
  
  public TprincipalData getPrincipalData(Principal principal)
  {
    if (principal==null)
    { return null;
    }
    
    principalNameChannel.push(principal.getName());
    try
    { return principalDataChannel.get();
    }
    finally
    { principalNameChannel.pop();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Role[] getRolesForPrincipal(Principal principal)
  {
    if (principal==null)
    { return null;
    }
    
    
    
    principalNameChannel.push(principal.getName());
    try
    { 
      principalIdChannel.push();
      try
      {

        URI[] roleIds=null;
        if (roleBindings!=null)
        { 
          ArrayList<URI> roleIdList=new ArrayList<URI>();
          for (Binding<URI> binding:roleBindings)
          {
            try
            { 
              URI roleId=binding.get();
              if (roleId!=null)
              { roleIdList.add(roleId);
              }
            }
            catch (RuntimeException x)
            { 
              throw new RuntimeException
                ("Error accessing roleBinding "+binding,x);
            }
          
          }
          roleIds=roleIdList.toArray(new URI[roleIdList.size()]);
        }
       
        
        
        TprincipalId[] principalIds
          =(TprincipalId[]) new Object[] {principalIdChannel.get()};
        
        // TODO: Expand principalIds using group memberships- for now we
        //   just take the user itself
        
        principalIdsChannel.push(principalIds);
        try
        { 
          
          
          if (principalRoleType!=null)
          { roleIds=ArrayUtil.concat(roleIds,roleIdsChannel.get());
          }
          
          
          if (roleIds!=null)
          { return getRolesByIds(roleIds);
          }
          else
          { return null;
          }
        }
        finally
        { principalIdsChannel.pop();
        }
      }
      finally
      { principalIdChannel.pop();
      }
      
    }
    finally
    { principalNameChannel.pop();
    }
    
  }

}
