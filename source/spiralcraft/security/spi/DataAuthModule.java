//
// Copyright (c) 1998,2010 Michael Toth
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


import spiralcraft.data.access.SerialCursor;

import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.session.DataSession;


import spiralcraft.data.DataException;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;

import java.net.URI;
import java.security.Principal;

/**
 * <p>Authenticates with a spiralcraft.data.query.Queryable for the 
 *   backing store which holds login info.
 * </p>
 *   
 * <p>The default behavior uses a login/cleartext-password scheme backed by
 *   the class:/spiralcraft/security/Login data Type. Alternate behaviors and
 *   credential sets can be defined at the configuration level.
 * </p>
 *   
 * @author mike
 *
 */
public class DataAuthModule
  extends AbstractAuthModule
{
  
  private static final ClassLog log
    =ClassLog.getInstance(DataAuthModule.class);
  
  private Space space;
  private Queryable<?> providedSource;
  
  protected Query accountQuery;
  private BoundQuery<?,?> boundAccountQuery;

  private Binding<Boolean> credentialComparisonX;
  private CredentialValidator[] validators; 
  
  private Binding<String> principalIdX;
  private Binding<String> accountIdX;
  

  private Type<?> accountDataType;

  private TeleFocus<Tuple> comparisonFocus;
  private ThreadLocalChannel<Tuple> loginChannel;
  private boolean debug;
  
  private Binding<?> refreshTriggerX;
  
  private Binding<?> onAssociate;
  
  private ThreadLocalChannel<DataSession> dataSessionChannel;
    
  
  
  @SuppressWarnings("unchecked")
  public DataAuthModule()
    throws DataException
  {
    // Default values for basic username/password authentication
    accountDataType
      =Type.resolve(URI.create("class:/spiralcraft/security/Login"));

      
  
    principalIdX=new Binding(Expression.create(".username"));
    accountIdX=new Binding(Expression.create(".principalId"));

  }
  
  /** 
   * <p>The Expression which provides the "user id" from the successfully
   *   authenticated account entry. This "user id" is  provided to the
   *   application via Principal.getName().
   * </p>
   * 
   * <p>The Expression is bound against
   *   a Focus which exposes the successfully authenticated account of 
   *   accountDataType as the subject, and the active credential set as the
   *   context.  
   * </p>
   * 
   * <p>This defaults to the expression ".username"
   * </p>
   */
  public void setPrincipalIdX(Binding<String> principalIdX)
  { this.principalIdX=principalIdX;
  }
  
  /**
   * <p>The Expression which determines whether the provided credentials
   *   match the account entry that has been mapped to the context.
   * </p>
   * 
   * <p>The Expression is bound against
   *   a Focus which exposes the successfully resolved object of 
   *   accountDataType as the subject, and the active credential set as the
   *   context.  
   * </p>
   * 
   * <p>This defaults to a globally permissive "true" (implying a trusted
   *   context) due to the abstract role of this class. This can be set to
   *   something like "DigestCredential.equals(.cryptpass)"
   * </p>
   * 
   * @param credentialComparisonX
   */
  public void setCredentialComparisonX(Binding<Boolean> credentialComparisonX)
  { this.credentialComparisonX=credentialComparisonX;
  }
  
  /**
   * <p>The set of CredentialValidators which determine whether the 
   *   provided credentials match the account entry that has been mapped to
   *   the context.
   * </p>
   * 
   * <p>The CredentialValidators are bound against a Focus which exposes
   *   the successfully resolved object of accountDataType as the subject,
   *   and the active credential set as the context
   * </p>
   * 
   * <p>If any validators are supplied, at least one must return a value of
   *   "true" and none may return "false" for the Credentials to be
   *   accepted.
   * </p>
   * 
   * @param validators
   */
  public void setCredentialValidators(CredentialValidator[] validators)
  { this.validators=validators;
  }
  
  /**
   * <p>Specify the data type of the account records used to identify the
   *   user and validate authentication tokens.
   * </p>
   * 
   * <p>Defaults to 
   *   class:/spiralcraft/security/Login, which contains username and
   *   password fields.
   * </p>
   * 
   * @param accountDataType
   */
  public void setAccountDataType(Type<?> accountDataType)
  { this.accountDataType=accountDataType;
  }
  
  public Type<?> getAccountDataType()
  { return accountDataType;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  
  /**
   * @param source The Queryable which provides access to the login database
   */
  public void setSource(Queryable<?> source)
  { this.providedSource=source;
  }
  
  /**
   * @param accountQuery Returns the account entry that is relevant to
   *   the "current user" as determined from the context, which includes the
   *   provided set of Credentials as well as any data in the Focus chain.  
   */
  public void setAccountQuery(Query accountQuery)
  { this.accountQuery=accountQuery;
  }
  
  public void setOnAssociate(Binding<?> onAssociate)
  { this.onAssociate=onAssociate;
  }
  
  public void setRefreshTriggerX(Binding<?> refreshTriggerX)
  { this.refreshTriggerX=refreshTriggerX;
  }
  
  @Override
  public Session createSession()
  { return new DataAuthSession();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Focus<?> bind(Focus<?> context)
    throws BindException
  { 
    dataSessionChannel
     =new ThreadLocalChannel<DataSession>
       (BeanReflector.<DataSession>getInstance(DataSession.class));
    
    // superclass provides a credentialFocus member field which 
    //   provides values for the various accepted credentials,
    //   named according to the credential class simple name.
    super.bind(context);

    credentialFocus.addFacet(credentialFocus.chain(dataSessionChannel));
    
    
    // Resolve the source for the master credentials list
    if (providedSource==null && context!=null)
    { 
      // Look up the local Space to use as a source if no source was provided
      space=Space.find(context);
    }


    Queryable source=providedSource;
        
    if (source==null || space!=null)
    { source=space;
    }
    if (source==null)
    { 
      throw new BindException
        ("No data source for DataAuthenticator");
    }

      
    if (refreshTriggerX!=null)
    { refreshTriggerX.bind(credentialFocus);
    }
    
    if (accountQuery==null)
    { 
      throw new BindException
        ("DataAuthModule.accountQuery must be provided to resolve user " 
        +"account from context" 
        );
    }
    
    // Bind the user lookup query to the credential Focus, which serves as
    //   the parameter Focus.
    try
    { boundAccountQuery=source.query(accountQuery,credentialFocus);
    }
    catch (DataException x)
    { throw new BindException
        ("Error binding Authenticator lookup query "+accountQuery,x);
    }
    
    
    // Set up a comparison to check password/etc
    loginChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(accountDataType));
    
    comparisonFocus=new TeleFocus<Tuple>(credentialFocus,loginChannel);
    
    
    if (credentialComparisonX==null
         && validators==null
       )
    {
      // Default to permissive context (trusted external account)
      credentialComparisonX
        =new Binding(Expression.create("true"));
    }
    
    if (credentialComparisonX!=null)
    { credentialComparisonX.bind(comparisonFocus);
    }
    if (validators!=null)
    { 
      for (CredentialValidator validator: validators)
      { validator.bind(comparisonFocus);
      }
    }
    principalIdX.bind(comparisonFocus);
    accountIdX.bind(comparisonFocus);
    
    if (onAssociate!=null)
    { onAssociate.bind(credentialFocus);
    }
    return context;
  }
  
  
  

  public class DataAuthSession
    extends AbstractSession
  {

    protected Object refreshTriggerValue;
    protected DataSession dataSession;
    
    public DataAuthSession()
    { 
      dataSession=new DataSession();
      dataSession.setSpace(space);
      dataSession.setFocus(credentialFocus);
    }
    
    @Override
    public void refresh()
    {
      push();
      try
      {
        if (refreshTriggerX!=null)
        { 
          if (debug)
          { log.fine("Refresh trigger value is "+refreshTriggerValue);
          }

          Object newValue=refreshTriggerX.get();
          if (newValue!=refreshTriggerValue)
          {
            if (newValue==null 
                || refreshTriggerValue==null
                || !newValue.equals(refreshTriggerValue)
            )
            { 
              if (debug)
              { 
                log.debug
                ("Re-authenticating: trigger value changed "
                  +" from "+refreshTriggerValue+" to "+newValue
                );
              }

              principal=null;
              authenticated=false;

              authenticate();
            }
          }
        } 
      }
      finally
      { pop();
      }
    }
    
    @Override
    public synchronized boolean authenticate()
    {
      if (boundAccountQuery==null)
      { 
        log.warning
          ("DataAuthenticator.DataAuthSession.isAuthenticated: "
          +" Authentication failed- configuration error- account "
          +" query ["+accountQuery+"] not bound"
          );
        return false;
      }
      
      if (principal!=null && authenticated)
      { 
        if (debug)
        { log.fine("Already authenticated");
        }
        return true;
      }
      
      if (debug)
      { log.fine("Attempting authentication");
      }
      
      push();
      try
      {
        
        String principalId=null;
        Tuple loginEntry=queryLoginEntry();

        if (loginEntry!=null)
        {

          // We have valid username in loginEntry
          //   run the password comparison expression

          boolean valid=false;
          loginChannel.push(loginEntry);
          try
          { 
            if (credentialComparisonX!=null)
            {
              Boolean result=credentialComparisonX.get();
              valid=(result!=null && result);
              if (debug)
              { log.fine("Credential comparison expression returned "+result);
              }
            }
            
            if (validators!=null)
            {
              for (CredentialValidator validator : validators)
              {
                Boolean validatorResult
                  =validator.validate();
                if (debug)
                { 
                  log.fine
                    ("Validator "+validator+" returned "+validatorResult);
                }
                if (validatorResult!=null)
                { 
                  if (validatorResult==true)
                  { valid=true;
                  }
                  else
                  { 
                    valid=false;
                    break;
                  }
                }
              }

            }
            
            if (valid)
            { principalId=principalIdX.get();
            }
          }
          finally
          { loginChannel.pop();
          }
          
          
          if (valid)
          {
          
            // cursor.discard()
            if (debug)
            { log.fine("valid login: "+loginEntry);
            }
          
          
            if (principal==null
                || !principal.getName().equals(principalId)
               )
            {
              final String name=principalId;
              principal
                =new Principal()
              {
                
                @Override
                public String getName()
                { return name;
                }
            
                @Override
                public String toString()
                { return super.toString()+":"+name;
                }
              };
            }
            authenticated=true;
            resetRefreshTrigger();
            return true;
          }
          else
          {
            if (debug)
            { 
              log.fine
                ("failed login: credentials don't match for login "+loginEntry
                );
            }
            authenticated=false;
            resetRefreshTrigger();
            return false;
          }
        }
        else
        { 
          if (debug)
          { 
            log.fine("failed login: no account match");
          }
          authenticated=false;
          resetRefreshTrigger();
          return false;
        }

      }
      catch (DataException x)
      { 
        x.printStackTrace();
        return false;
      }
      catch (RuntimeException x)
      {
        x.printStackTrace();
        return false;
      }
      finally
      { pop();

      }
    }

    public String getAccountId()
    { 
      push();
      try
      {
        Tuple loginEntry=queryLoginEntry();

        if (loginEntry!=null)
        {

          // We have valid username in loginEntry

          loginChannel.push(loginEntry);
          try
          { return accountIdX.get();
          }
          finally
          { loginChannel.pop();
          }
        }
        return null;
      }
      catch (DataException x)
      { throw new RuntimeDataException("Error getting login entry",x);
      }
      finally
      { pop();
      }
      
    }
    
    public synchronized boolean associateLogin()
    { 
      push();
      try
      {
        if (onAssociate!=null)
        {      
          onAssociate.get();
          return true;
        }
        else
        { return false;
        }
      }
      finally
      { pop();
      }
    }
    
    private Tuple queryLoginEntry()
      throws DataException,SecurityException
    {
      SerialCursor<?> cursor=boundAccountQuery.execute();
        
      try
      {
        if (cursor.next())
        { 
          Tuple accountEntry=cursor.getTuple();
          if (debug)
          { log.fine("Found user "+accountEntry.get("username"));
          }
          
          if (cursor.next())
          { 
            throw new SecurityException
              ("Cardinality Violation: Multiple Login records for user "
                +accountEntry.get("searchname")
              );
          }
          return accountEntry.snapshot();
        }
      }
      finally
      { cursor.close();
      }
      return null;

    }

    private void resetRefreshTrigger()
    { 
      if (refreshTriggerX!=null)
      { refreshTriggerValue=refreshTriggerX.get();
      }
    }

    private void push()
    { dataSessionChannel.push(dataSession);
    }
  
    private void pop()
    { dataSessionChannel.pop();
    } 
  }




  
}
  
  
class DataPrincipal
  implements Principal
{
  private final String name;
  private final Tuple data;
  
  public DataPrincipal(String name,Tuple data)
  {
    this.name=name;
    this.data=data;
  }
  
  @Override
  public String getName()
  { return name;
  }
          
  public Tuple getData()
  { return data;
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+name;
  }
}