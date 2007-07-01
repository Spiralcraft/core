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
package spiralcraft.data.session;

import java.util.ArrayList;
import java.util.HashMap;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.FocusProvider;

import spiralcraft.lang.spi.Namespace;
import spiralcraft.lang.spi.NamespaceReflector;
import spiralcraft.lang.spi.SimpleBinding;

import spiralcraft.data.DataException;

/**
 * <P>Provides access to a set of coordinated Views of related data.
 * 
 * <P>Access is provided to the spiralcraft.lang expression language through a 
 *  spiralcraft.lang Focus for expression resolution. The namespace published
 *  in this Focus is:
 *  
 * <UL>
 *   <LI><CODE>[DataSession] data.<I>viewname</I></CODE>
 *   <BR>References the Tuple at the current cursor position of the
 *     specified view
 *   </LI>
 *   <LI><CODE>[DataSession] data.<I>viewname</I>.<I>fieldname</I></CODE>
 *   <BR>References a data field value at the current cursor position of the
 *     specified view
 *   </LI>
 *   <LI><CODE>[DataSession] view.<I>viewname</I></CODE>
 *   <BR>References the spiralcraft.data.session.View as a Java Bean
 *   </LI>
 *   <LI><CODE>[DataSession] session</CODE>
 *   <BR>References the spiralcraft.data.session.DataSession itself 
 *     as a Java Bean
 *   </LI>
 * </UL>
 * 
 * <P>Implementations of this class primaily differ in how the Views are
 *  created.
 *
 * @author mike
 *
 */
public class DataSession
  implements FocusProvider
{

  protected final ArrayList<View> views=new ArrayList<View>();
  protected final HashMap<String,View> viewMap
    =new HashMap<String,View>();
  
  private final NamespaceReflector dataSessionNamespaceReflector
    =new NamespaceReflector();
  private Namespace dataSessionNamespace;
  private SimpleBinding<Namespace> dataSessionChannel;
    
  private final NamespaceReflector dataNamespaceReflector
    =new NamespaceReflector();
  private Namespace dataNamespace;
  private SimpleBinding<Namespace> dataChannel;
  
  private final NamespaceReflector viewNamespaceReflector
    =new NamespaceReflector();
  private Namespace viewNamespace;
  private SimpleBinding<Namespace> viewChannel;
  
  
  private DefaultFocus<Namespace> localFocus;
  
  private DefaultFocus<Namespace> dataFocus;


  /**
   * Provide a Focus for expression evaluation in the general context of
   *   the specified parentFocus. The Focus will be registered under the
   *   <CODE>[DataSession]</CODE> intrinsic identifier as well as any other
   *   aliases specified.
   *   
   */
  public DefaultFocus<Namespace> createFocus
    (Focus parentFocus,String ... aliases)
    throws BindException
  {
    DefaultFocus<Namespace> focus=new DefaultFocus<Namespace>();
    if (parentFocus!=null)
    { focus.setParentFocus(focus);
    }
    focus.setSubject(dataSessionChannel);
    focus.setContext(dataSessionChannel);
    focus.addNames("DataSession");
    focus.addNames(aliases);
    
    return focus;
  }
  
  
  /**
   * Set up the namespace definitions for each View
   * 
   * @throws DataException
   */
  private void initViews()
    throws DataException
  {
    for (View view:views)
    { 
      // Connect everything together
      view.setDataSession(this);
      viewMap.put(view.getName(),view);

      try
      { 
        dataNamespaceReflector.register(view.getName(),view.getDataReflector());
        viewNamespaceReflector.register(view.getName(),view.getViewReflector());
      }
      catch (BindException x)
      { 
        throw new DataException
          ("View '"+view.getName()
          +"' could not be registered in DataSession: "
          +x
          ,x
          );
      }
    }    
    
  }
  
  /**
   * Provide a Channel for the data in each View under the appropriate name
   *   in the data namespace.
   * 
   * @throws DataException
   */
  private void bindViews()
    throws DataException
  {
    for (View view:views)
    {
      view.bindData(dataFocus);
      try
      { 
        dataNamespace.putOptic
          (view.getName()
          ,view.getTupleBinding()
          );
        
        viewNamespace.putOptic
          (view.getName()
          ,new SimpleBinding<View>(view.getViewReflector(),view,true)
          );
      }
      catch (BindException x)
      { 
        throw new DataException
          ("View '"+view.getName()
          +"' could not be added to dataNamespace in DataSession: "
          +x
          ,x
          );
      }
    }

  }
  
  @SuppressWarnings("unchecked") // Heterogeneous ops
  public void initialize()
    throws DataException
  { 
    initViews();
    
    dataSessionNamespace=new Namespace(dataSessionNamespaceReflector);
    dataSessionChannel
      =new SimpleBinding
        (dataSessionNamespaceReflector,dataSessionNamespace,true);
      
    dataNamespace=new Namespace(dataNamespaceReflector);
    dataChannel=new SimpleBinding(dataNamespaceReflector,dataNamespace,true);

    viewNamespace=new Namespace(viewNamespaceReflector);
    viewChannel=new SimpleBinding(viewNamespaceReflector,viewNamespace,true);

    try
    { 
      dataSessionNamespace.putOptic("data",dataChannel);
      dataSessionNamespace.putOptic("view",viewChannel);
      dataSessionNamespace.putOptic("session", new SimpleBinding(this,true));
    }
    catch (BindException x)
    {
      throw new DataException
      ("DataSession: could not set up DataSession namespace"
      +x
      ,x
      );
    }
    
    try
    { 
      localFocus=createFocus(null,"DataSession");
    }
    catch (BindException x)
    {
      throw new DataException
      ("DataSession: could not create local Focus: "
      +x
      ,x
      );
    }
    
    dataFocus=new DefaultFocus<Namespace>();
    dataFocus.setParentFocus(localFocus);
    dataFocus.setSubject(dataChannel);
    dataFocus.setContext(dataChannel);
    
    bindViews();
  } 
}
