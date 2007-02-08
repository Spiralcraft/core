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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.WriteException;
import spiralcraft.lang.OpticFactory;

import spiralcraft.util.StringConverter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;

/**
 * Binds a property of a UI component with a property
 *   in the application data model by propogating
 *   data values bidirectionally in response to UI and
 *   application events.
 *
 * Expressions (spiralcraft.lang) are evaluated at runtime.
 *
 * At configuration-time a model Focus is supplied which references
 *   a point in the application data model against which the
 *   modelExpression will be evaluated.
 *
 * At configuration time, a UI focus is supplied which
 *   references a point in the UI to bind the UI expression to. 
 *
 * At runtime, the bind() method causes both Expressions to be resolved.
 * 
 * For example, a Binding can be used to associate a text
 *   property in a UI text field with a String field
 *   of an object in the data model (the model property)
 *   exposed by an editor component (the model Focus).
 *
 *   modelExpression (against the editor): data.customer.name
 *   uiExpression (against a text field): text
 *   
 */
public class Binding
  implements PropertyChangeListener
{
  private Expression _modelExpression;
  private Channel _modelChannel;
  private StringConverter _modelStringConverter;
  private Focus _modelFocus;

  private Expression _uiExpression;
  private Channel _uiChannel; 
  private Focus _uiFocus;
  private StringConverter _uiStringConverter;

  private boolean _updateToModel;


  /**
   * Supply the Focus against which the model Expression will be
   *   resolved.
   */
  public void setModelFocus(Focus val)
  { _modelFocus=val;
  }

  /**
   * The Expression which identifies the
   *   property in the application data model.
   */
  public void setModelExpression(String val)
    throws ParseException
  { _modelExpression=Expression.parse(val);
  }

  /**
   * Supply the Focus against which the UI Expression will be
   *   resolved (optional).
   */
  public void setUiFocus(Focus val)
  { _uiFocus=val;
  }

  /**
   * The Expression which identifies the
   *   property in the user interface component
   */
  public void setUiExpression(String val)
    throws ParseException
  { _uiExpression=Expression.parse(val);
  }

  
  public void bind()
    throws BindException
  {
    _modelChannel=_modelFocus.bind(_modelExpression);
    _uiChannel=_uiFocus.bind(_uiExpression);

    PropertyChangeSupport pcs=_modelChannel.propertyChangeSupport();
    if (pcs!=null)
    { pcs.addPropertyChangeListener(this);
    }
    
    //
    // Handle default string conversions
    //
    if (_modelChannel.getContentType().isAssignableFrom(_uiChannel.getContentType()))
    { _updateToModel=true;
    }
    else if (_uiChannel.getContentType()==String.class)
    { 
      _modelStringConverter=StringConverter.getInstance(_modelChannel.getContentType());
      if (_modelStringConverter!=null)
      { _updateToModel=true;
      }
      else
      { 
        // We can only update to the UI, because we can't convert
        //   the UI type to the model type.
        _modelStringConverter=StringConverter.getOneWayInstance();
      }
    }
    else if (_modelChannel.getContentType()==String.class)
    { 
      _uiStringConverter=StringConverter.getInstance(_uiChannel.getContentType());
      if (_uiStringConverter==null)
      { throw new BindException("Cannot convert a String to a "+_uiChannel.getContentType());
      }
      _updateToModel=true;
    }

  }

  public void uiChanged()
  { 
    if (_updateToModel)
    {
      try
      {
        if (!_modelChannel.set(translateToModel(_uiChannel.get())))
        { updateFromModel();
        }
      }
      catch (WriteException x)
      { 
        // XXX Review what to do here- tell controller about error?
        x.printStackTrace();
        updateFromModel();
      }
      
    }
  }

  public void updateFromModel()
  { 
    try
    { _uiChannel.set(translateToUi(_modelChannel.get()));
    }
    catch (WriteException x)
    { 
      // XXX Review what to do here- tell controller about error?
      x.printStackTrace();
    }
  }

  private Object translateToUi(Object value)
  {
    if (_modelStringConverter!=null)
    { return _modelStringConverter.toString(value);
    }
    else if (_uiStringConverter!=null)
    { return _uiStringConverter.fromString((String) value);
    }
    else
    { return value;
    }
  }

  private Object translateToModel(Object value)
  {
    if (_uiStringConverter!=null)
    { return _uiStringConverter.toString(value);
    }
    else if (_modelStringConverter!=null)
    { return _modelStringConverter.fromString((String) value);
    }
    else
    { return value;
    }
  }

  public void propertyChange(PropertyChangeEvent event)
  { 
    try
    { _uiChannel.set(translateToUi(event.getNewValue()));
    }
    catch (WriteException x)
    { 
      // XXX Review what to do here- tell controller about error?
      x.printStackTrace();
    }
  }

  public void release()
  { 
    PropertyChangeSupport pcs=_modelChannel.propertyChangeSupport();
    if (pcs!=null)
    { pcs.removePropertyChangeListener(this);
    }
    _modelChannel=null;
    _uiChannel=null;
    _modelStringConverter=null;
    _uiStringConverter=null;
    _updateToModel=false;
  }

}
