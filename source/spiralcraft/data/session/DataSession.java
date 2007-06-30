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
 * <P>Implementations may differ in how the Views are created.
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
  
  private Namespace namespace;
  private final NamespaceReflector namespaceReflector=new NamespaceReflector();
  private Focus<DataSession> localFocus;


  public Focus<DataSession> createFocus(Focus parentFocus,String ... aliases)
    throws BindException
  {
    DefaultFocus<DataSession> focus=new DefaultFocus<DataSession>();
    if (parentFocus!=null)
    { focus.setParentFocus(focus);
    }
    focus.setSubject(new SimpleBinding<DataSession>(this,true));
    focus.setContext
      (new SimpleBinding<Namespace>
        (namespaceReflector,namespace,true)
      );
    
    return focus;
  }
  
  
  @SuppressWarnings("unchecked") // Heterogeneous ops
  public void initialize()
    throws DataException
  { 
    for (View view:views)
    { 
      // Connect everything together
      view.setDataSession(this);
      viewMap.put(view.getName(),view);
    }
    
    for (View view:views)
    {
      try
      { namespaceReflector.register(view.getName(),view.getViewReflector());
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
      
    namespace=new Namespace(namespaceReflector);

    try
    { localFocus=createFocus(null,"DataSession");
    }
    catch (BindException x)
    {
      throw new DataException
      ("DataSession: could not create local Focus: "
      +x
      ,x
      );
    }
    
    for (View view:views)
    {
      
      try
      { 
        namespace.putOptic
          (view.getName()
          ,view.bindView(localFocus)
          );
      }
      catch (BindException x)
      { 
        throw new DataException
          ("View '"+view.getName()
          +"' could not be added to namespace in DataSession: "
          +x
          ,x
          );
      }
    }
  }
  
  
}
