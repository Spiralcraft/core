package spiralcraft.ui;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.BindException;
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
 * At configuration-time a Focus is supplied which references
 *   a point in the application data model against which the
 *   modelExpression will be evaluated.
 *
 * At runtime, the UI object to bind against is supplied through
 *   the bind() method, and both Expressions are resolved.
 * 
 * For example, a Binding can be used to associate a text
 *   property in a UI text field with a String field
 *   of an object in the data model (the model property)
 *   exposed by an editor component (the Focus).
 *
 *   modelExpression (against the editor): data.customer.name
 *   uiExpression (against a text field): text
 *   
 * Bindings are usually coupled with UI specific controls
 *   which can trigger updates based on application events.
 */
public class Binding
  implements PropertyChangeListener
{
  private Expression _modelExpression;
  private Channel _modelChannel;
  private StringConverter _modelStringConverter;

  private Expression _uiExpression;
  private Channel _uiChannel;
  private StringConverter _uiStringConverter;

  private boolean _updateToModel;

  private Focus _focus;

  /**
   * Supply the Focus against which the Expression will be
   *   resolved.
   */
  public void setFocus(Focus val)
  { _focus=val;
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
   * The Expression which identifies the
   *   property in the user interface component
   */
  public void setUiExpression(String val)
    throws ParseException
  { _uiExpression=Expression.parse(val);
  }

  
  public void bind(Object uiBean)
    throws BindException
  { 
    _modelChannel=_focus.bind(_modelExpression);
    Focus uiFocus=new DefaultFocus(OpticFactory.getInstance().box(uiBean));
    _uiChannel=uiFocus.bind(_uiExpression);

    PropertyChangeSupport pcs=_modelChannel.propertyChangeSupport();
    if (pcs!=null)
    { pcs.addPropertyChangeListener(this);
    }
    
    //
    // Handle default string conversions
    //
    if (_modelChannel.getTargetClass().isAssignableFrom(_uiChannel.getTargetClass()))
    { _updateToModel=true;
    }
    else if (_uiChannel.getTargetClass()==String.class)
    { 
      _modelStringConverter=StringConverter.getInstance(_modelChannel.getTargetClass());
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
    else if (_modelChannel.getTargetClass()==String.class)
    { 
      _uiStringConverter=StringConverter.getInstance(_uiChannel.getTargetClass());
      if (_uiStringConverter==null)
      { throw new BindException("Cannot convert a String to a "+_uiChannel.getTargetClass());
      }
      _updateToModel=true;
    }

  }

  public void uiChanged()
  { 
    if (_updateToModel)
    {
      if (!_modelChannel.set(translateToModel(_uiChannel.get())))
      { updateFromModel();
      }
    }
  }

  public void updateFromModel()
  { _uiChannel.set(translateToUi(_modelChannel.get()));
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
  { _uiChannel.set(translateToUi(event.getNewValue()));
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
