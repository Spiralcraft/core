//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.sax;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

import org.xml.sax.Attributes;


import spiralcraft.data.DataException;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Setter;
import spiralcraft.lang.parser.AssignmentNode;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;

/**
 * <p>Implements a mapping from a foreign XML data element to part of a
 *   Tuple set.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class FrameHandler
{
  protected static final ClassLog log
    =ClassLog.getInstance(FrameHandler.class);

  // Makes FrameHandler thread-safe
  class LocalStack 
    extends ThreadLocal<Stack<ForeignDataHandler.HandledFrame>>
  {
    @Override
    protected Stack<ForeignDataHandler.HandledFrame> initialValue()
    { return new Stack<ForeignDataHandler.HandledFrame>();
    }
      
    public ForeignDataHandler.HandledFrame pop()
    { 
      Stack<ForeignDataHandler.HandledFrame> val=get();
      ForeignDataHandler.HandledFrame frame=val.pop();
      if (val.isEmpty())
      { remove();
      }
      return frame;
    }
      
    public void push(ForeignDataHandler.HandledFrame val)
    { get().push(val);
    }
    
    public ForeignDataHandler.HandledFrame peek()
    { return get().peek();
    }
  }

  private String elementURI;
  private LinkedHashMap<String,FrameHandler> childMap
    =new LinkedHashMap<String,FrameHandler>();
  
  private boolean strictMapping;
  
  private LocalStack stack=new LocalStack();
  
  
  protected boolean debug;
  
  protected FrameHandler parent;

  private boolean bindCalled;
  
  private Focus<?> focus;
  
  private AttributeBinding<?>[] attributeBindings;
  
  private HashMap<String,AttributeBinding<?>> attributeMap;

  private Assignment<?>[] defaultAssignments;
  protected Setter<?>[] defaultSetters;
  
  private Expression<String> textBinding;
  private Channel<String> textChannel;
  private boolean textAssignment;
  private ThreadLocalChannel<String> text;

  
  public void setDefaultAssignments(Assignment<?>[] defaultAssignments)
  { this.defaultAssignments=defaultAssignments;
  }
  
  protected boolean isHandlingText()
  { return textChannel!=null;
  }
  
  void setParent(FrameHandler parent)
  { this.parent=parent;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>Specify a destination for character data in the XML element
   * </p>
   * 
   * @param textBinding
   */
  public void setTextBinding(Expression<String> textBinding)
  { this.textBinding=textBinding;
  }
  
  public String getElementURI()
  { return elementURI;
  }
  
  public void setElementURI(String elementURI)
  { this.elementURI = elementURI;
  }
  
  public void setAttributeBindings(AttributeBinding<?>[] attributeBindings)
  { this.attributeBindings=attributeBindings;
  }
  
  /**
   * <p>Recursively bind queries and expressions to the application context.
   * </p>
   * 
   * @param focus
   */
  public void bind()
    throws BindException
  {
    bindCalled=true;
    bindAttributes();
    bindAssignments();
    bindChildren();
  }
  
  public void setFocus(Focus<?> focus)
  { this.focus=focus;
  }
  
  @SuppressWarnings("unchecked") // Cast current Focus to requested generic
  public <X> Focus<X> getFocus()
  { 
    Focus<X> ret=(Focus<X>) focus;
    if (ret==null && parent!=null)
    { ret=parent.getFocus();
    }
    return ret;
    
  }
  
  protected void bindAssignments()
    throws BindException
  { defaultSetters=Assignment.bindArray(defaultAssignments,getFocus());
  }
  
  protected void bindAttributes()
    throws BindException
  {
    if (attributeBindings!=null)
    {
      if (attributeMap==null)
      { attributeMap=new HashMap<String,AttributeBinding<?>>();
      }
      else
      { attributeMap.clear();
      }
      
      Focus<?> focus=getFocus();
      for (AttributeBinding<?> binding: attributeBindings)
      {
        binding.bind(focus);
        attributeMap.put
          (binding.getAttribute()
          ,binding
          );
        if (debug)
        {
          log.fine
            ("URI="+elementURI+": Bound attribute "+binding.getAttribute());
        }
      }
      
    }
    if (textBinding!=null)
    { 
      if (textBinding.getRootNode() instanceof AssignmentNode)
      { 
        textAssignment=true;
        
        text=new ThreadLocalChannel<String>
          (BeanReflector.<String>getInstance(String.class));
        textChannel=getFocus().telescope(text).bind(textBinding);
      }
      else
      { textChannel=getFocus().bind(textBinding);
      }
    }
  }

  protected void bindChildren()
    throws BindException
  {
    for (FrameHandler child:childMap.values())
    { child.bind();
    }
  }
  
  /**
   * <p>When set to true, the strictMapping property indicates that an
   *   encounter of an unmapped
   *   element or attribute in the incoming data stream will cause an exception
   *   to be thrown, terminating processing. 
   * </p>
   * 
   * <p>When set to false (the default value), unmapped elements and attributes
   *   will simply be ignored
   * </p>
   * 
   * @return strictMapping 
   */
  public boolean isStrictMapping()
  { return strictMapping;
  }
  
  public void setStrictMapping(boolean strictMapping)
  { this.strictMapping=strictMapping;
  }
  
  
  public void setChildren(FrameHandler[] children)
  { 
    childMap.clear();
    for (FrameHandler child:children)
    { 
      childMap.put(child.getElementURI(), child);
      child.setParent(this);
    }
  }

  public LinkedHashMap<String,FrameHandler> getChildMap()
  { return childMap;
  }
  
  protected void applyAttributes()
  { 
    Attributes attributes=stack.peek().getAttributes();
    if (attributes==null)
    { return;
    }
    
    for (int i=0;i<attributes.getLength();i++)
    {
      String name=attributes.getLocalName(i);
      String uri=attributes.getURI(i);
      String fullName=(uri!=null?uri:"")+name;
      String value=attributes.getValue(i);
      
      
      AttributeBinding<?> binding
        =attributeMap!=null?attributeMap.get(fullName):null;
        
      if (binding!=null)
      { 
        binding.set(value);
        if (debug)
        { 
          log.fine
            ("URI="+elementURI+": Applying attribute "+fullName+" = "+value);
        }
      }
      else
      {
        if (debug)
        { log.fine("URI="+elementURI+": Ignoring attribute "+fullName);
        }
      }
    }

  }

  /**
   * Override to setup data container (Tuple or List)
   */
  protected abstract void openData()
    throws DataException;
  
  /**
   * <p>Override to finalize state of any data objects read before the parent
   *   frame receives them.
   * </p>
   */
  protected abstract void closeData()
    throws DataException;

  /**
   * <p>Interface between the DataHandler frame and the FrameHandler to
   *   indicate when a new Frame has been opened (ie. new element)
   * </p>
   * 
   * @param frame
   */
  public final void openFrame(ForeignDataHandler.HandledFrame frame)
    throws DataException
  { 
    if (!bindCalled)
    { 
      throw new DataException
        ("FrameHandler must be bound before it can be used");
    }
    
    stack.push(frame);
    
    if (debug)
    { log.fine("URI="+elementURI);
    }
    
    openData();
    applyAttributes();
  }
 
  /**
   * <p>Called when a child frame is closed to give the parent an opportunity
   *   to integrate any data read by the child.
   * </p>
   *   
   * @param child
   */
  public void closingChild(FrameHandler child)
  { 
  }
  
  /**
   * <p>Interface between the DataHandler frame and the FrameHandler to
   *   indicate when a Frame is about to be closed 
   * </p>
   * 
   * @param frame
   */
  public final void closeFrame(ForeignDataHandler.HandledFrame frame)
    throws DataException
  { 
    String chars=frame.getCharacters();
    if (chars!=null && textChannel!=null)
    { 
      
      if (textAssignment)
      { 
        text.push(chars);
        try
        { textChannel.get();
        }
        finally
        { text.pop();
        }
      }
      else
      {
        String orig=textChannel.get();
        if (orig==null)
        { textChannel.set(chars);
        }
        else
        { textChannel.set(orig+chars);
        }
      }
    }
      
    closeData();
    
    if (parent!=null)
    { parent.closingChild(this);
    }
    
    if (debug)
    { log.fine("URI="+elementURI);
    }
    if (stack.pop()!=frame)
    { 
      throw new IllegalStateException
        ("Internal Error: DataHandler and FrameHandler stack out of sync");
    }
  }
  
  protected ForeignDataHandler.HandledFrame getFrame()
  { return stack.peek();
  }

  class FrameChannel<T>
    extends AbstractChannel<T>
  {

    public FrameChannel(Reflector<T> reflector)
    { super(reflector);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected T retrieve()
    { return (T) getFrame().getObject();
    }

    @Override
    protected boolean store(
      T val)
      throws AccessException
    { 
      getFrame().setObject(val);
      return true;
    }
    
    @Override
    public boolean isWritable()
    { return true;
    }
  }  
}
