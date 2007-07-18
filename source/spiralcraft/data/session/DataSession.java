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

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.FocusProvider;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.Namespace;
import spiralcraft.lang.spi.NamespaceReflector;
import spiralcraft.lang.spi.SimpleBinding;

import spiralcraft.builder.Lifecycle;
import spiralcraft.builder.LifecycleException;

import spiralcraft.data.DataException;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Type;

import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.data.lang.StaticTupleBinding;


/**
 * <P>Provides access to a set of coordinated Views of related data.
 * 
 * <P>Access is provided to the spiralcraft.lang expression language through a 
 *  spiralcraft.lang Focus for expression resolution. The namespace published
 *  in this Focus is:
 *  
 * <UL>
 *   <LI><CODE>[dataSession] data.<I>viewname</I></CODE>
 *   <BR>References the Tuple at the current cursor position of the
 *     specified view
 *   </LI>
 *   <LI><CODE>[dataSession] data.<I>viewname</I>.<I>fieldname</I></CODE>
 *   <BR>References a data field value at the current cursor position of the
 *     specified view
 *   </LI>
 *   <LI><CODE>[dataSession] view.<I>viewname</I></CODE>
 *   <BR>References the spiralcraft.data.session.View as a Java Bean
 *   </LI>
 *   <LI><CODE>[dataSession] .</CODE>
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
  implements FocusProvider<DataSession>,Lifecycle
{

  protected final ArrayList<View<?>> views=new ArrayList<View<?>>();
  protected final HashMap<String,View<?>> viewMap
    =new HashMap<String,View<?>>();
  
  private final NamespaceReflector dataSessionNamespaceReflector
    =new NamespaceReflector();
  private SimpleBinding<Namespace> dataSessionChannel;
    
  private final NamespaceReflector dataNamespaceReflector
    =new NamespaceReflector();
  private SimpleBinding<Namespace> dataChannel;
  
  private final NamespaceReflector viewNamespaceReflector
    =new NamespaceReflector();
  private SimpleBinding<Namespace> viewChannel;
  
  private Reflector<EditableTuple> inReflector;
  private StaticTupleBinding<EditableTuple> inChannel;
  
  private SimpleFocus<DataSession> localFocus;
  
  private SimpleFocus<Namespace> dataFocus;
  
  private URI inTypeURI;


  /**
   * Provide a Focus for expression evaluation in the general context of
   *   the specified parentFocus. The Focus will be registered under the
   *   <CODE>[DataSession]</CODE> intrinsic identifier as well as any other
   *   aliases specified.
   *   
   */
  public SimpleFocus<DataSession> createFocus
    (Focus<?> parentFocus,String namespace,String name)
    throws BindException
  {
    CompoundFocus<DataSession> focus=new CompoundFocus<DataSession>();
    if (parentFocus!=null)
    { focus.setParentFocus(focus);    
    }
    focus.setSubject(new SimpleBinding<DataSession>(this,true));
    focus.setContext(dataSessionChannel);
    focus.addNamespaceAlias(namespace);
    focus.setName(name);
    
    return focus;
  }
  
  public void setViews(View<?>[] views)
  { 
    this.views.clear();
    for (View<?> view:views)
    { this.views.add(view);
    }
  }
  
  
  /**
   * Set up the namespace definitions for each View
   * 
   * @throws DataException
   */
  private void initViews()
    throws DataException
  {
    for (View<?> view:views)
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
  @SuppressWarnings("unchecked") // View type is heterogeneous
  private void bindViews()
    throws DataException
  {
    for (View<?> view:views)
    {
      view.bindData(dataFocus);
      try
      { 
        dataChannel.get().putOptic
          (view.getName()
          ,view.getTupleBinding()
          );
        
        viewChannel.get().putOptic
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
  
  @Override
  public void start()
    throws LifecycleException
  {
    try
    { initialize();
    }
    catch (DataException x)
    { 
      x.printStackTrace();
      throw new LifecycleException(x.toString(),x);
    }
  }
  
  @Override
  public void stop()
  { }
  

  @SuppressWarnings("unchecked") // Heterogeneous ops
  private void initialize()
    throws DataException
  { 
    initViews();
    try
    {
      dataSessionNamespaceReflector.register("data",dataNamespaceReflector);
      dataSessionNamespaceReflector.register("view",viewNamespaceReflector);
      dataSessionNamespaceReflector.register("in",inReflector);
    
      Namespace dataSessionNamespace
        =new Namespace(dataSessionNamespaceReflector);
      dataSessionChannel
        =new SimpleBinding
          (dataSessionNamespaceReflector,dataSessionNamespace,true);
      
      if (inTypeURI!=null)
      {
        Type inType=Type.resolve(inTypeURI);
        EditableTuple inTuple=new EditableArrayTuple(inType.getScheme());
        inChannel
          =new StaticTupleBinding<EditableTuple>(inType.getScheme(),inTuple);
        dataSessionNamespace.putOptic("in",inChannel);
      }

      Namespace dataNamespace=new Namespace(dataNamespaceReflector);
      dataChannel=new SimpleBinding(dataNamespaceReflector,dataNamespace,true);
      dataSessionNamespace.putOptic("data",dataChannel);
      

      Namespace viewNamespace=new Namespace(viewNamespaceReflector);
      viewChannel=new SimpleBinding(viewNamespaceReflector,viewNamespace,true);
      dataSessionNamespace.putOptic("view",viewChannel);

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
      localFocus=createFocus(null,"data","DataSession");
    }
    catch (BindException x)
    {
      throw new DataException
      ("DataSession: could not create local Focus: "
      +x
      ,x
      );
    }
    
    dataFocus=new SimpleFocus<Namespace>();
    dataFocus.setParentFocus(localFocus);
    dataFocus.setSubject(dataChannel);
    dataFocus.setContext(dataChannel);
    
    bindViews();
  } 
}
