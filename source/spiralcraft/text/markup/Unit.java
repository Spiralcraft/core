package spiralcraft.text.markup;

import java.util.LinkedList;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.BindException;

/**
 * A Unit of marked up text
 */
public abstract class Unit
{
  private LinkedList _children;
  private Unit _parent;
  private String _name;
  
  
  public void addChild(Unit child)
  { 
    if (_children==null)
    { _children=new LinkedList();
    }
    _children.add(child);
    child.setParent(this);
  }
  
  public void setParent(Unit parent)
  { _parent=parent;
  }
  
  public Unit getParent()
  { return _parent;
  }
  
  public String getName()
  { return _name;
  }
  
  protected void setName(String name)
  { _name=name;
  }

  public Unit[] getChildren()
  { 
    if (_children==null)
    { return new Unit[0];
    }
    Unit[] children=new Unit[_children.size()];
    _children.toArray(children);
    return children;
  }  
  
  public boolean isOpen()
  { return false;
  }
  
  public void close()
    throws MarkupException
  {
  }
   
}
