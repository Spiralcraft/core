//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.ui;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;

import spiralcraft.lang.spi.SimpleChannel;

/**
 * Associates a UI component with a group of Bindings which bind
 *   properties of the UI component with the application data model.
 *
 * The BindingGroup is used by Controls to coordinates a number of individual
 *   bindings which all respond to the same events, are associated with
 *   the same UI component and/or are associated with the same model 
 *   component.
 *
 * Subclasses should call updateFromModel() when they
 *   want to manually refresh the UI outside the presence
 *   of model triggered update, and will normally call
 *   updateFromModel() immediately after initializing 
 *   the component.
 *
 * Subclasses should call uiChanged() when data should
 *   be propogated from the UI into the model, presumably
 *   upon receiving an appropriate event.
 */
public abstract class BindingGroup
  implements Lifecycle
{

  private Binding[] _bindings;
  
  public void setBindings(Binding[] bindings)
  { _bindings=bindings;
  }

  /**
   * Update the UI component from model data
   */
  protected void updateFromModel()
  { 
    if (_bindings!=null)
    {
      for (int i=0;i<_bindings.length;i++)
      { _bindings[i].updateFromModel();
      }
    }
  }

  /**
   * Update the model data from the UI
   */
  protected void uiChanged()
  { 
    if (_bindings!=null)
    {
      for (int i=0;i<_bindings.length;i++)
      { _bindings[i].uiChanged();
      }
    }
  }

  /**
   * Return the Object that the binding uiExpression
   *   is evaluated against, or null if this
   *   object will differ across individual bindings.
   */
  protected abstract Object getUiObject();

  /**
   * Return the Object that the binding modelExpression
   *   is evaluated against, or null if this
   *   object will differ across individual bindings.
   */
  protected abstract Object getModelObject();

  @SuppressWarnings("unchecked") // Not a specific use of generics
	public void start()
    throws LifecycleException
	{  
    try
    { 
      final Object uiObject=getUiObject();
      Focus uiFocus=null;
      if (uiObject!=null)
      { uiFocus=new SimpleFocus(new SimpleChannel(uiObject,true));
      }

      final Object modelObject=getModelObject();
      Focus modelFocus=null;
      if (modelObject!=null)
      { modelFocus=new SimpleFocus(new SimpleChannel(modelObject,true));
      }
       
      if (_bindings!=null)
      {
        for (int i=0;i<_bindings.length;i++)
        { 
          if (uiFocus!=null)
          { _bindings[i].setUiFocus(uiFocus);
          }
          if (modelFocus!=null)
          { _bindings[i].setModelFocus(modelFocus);
          }
          _bindings[i].bind();
        }
      }
    }
    catch (BindException x)
    { x.printStackTrace(); 
    }
  }

  public void stop()
    throws LifecycleException
  {
    if (_bindings!=null)
    {
      for (int i=0;i<_bindings.length;i++)
      { _bindings[i].release();
      }
    }
  }

}
