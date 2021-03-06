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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.data.DataException;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Setter;
import spiralcraft.lang.parser.AssignmentNode;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.string.StringConverter;
import spiralcraft.util.string.StringPool;

/**
 * <p>Implements a mapping from a foreign XML data element to part of a
 *   Tuple set.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractFrameHandler
  implements FrameHandler
{
  
  public static final String combineName(String uri,String name)
  { return (uri!=null && !uri.isEmpty())?uri+"#"+name:name;
  }
  
  public static final String localName(String uri)
  { 
    int pos=uri.indexOf("#");
    if (pos<0)
    { return uri;
    }
    else
    { return uri.substring(pos+1);
    }
  }
  
  /**
   * Turn a value with a namespace prefix into a URI qualified name.
   * 
   * @param value
   * @param resolver
   * @return The URI qualified name, with the name as the URI fragment
   * @throws SAXException
   */
  public final String transformNamespace
    (String value,PrefixResolver resolver)
    throws DataException
  {
    int colonPos=value.indexOf(':');
    if (colonPos==0)
    { value=value.substring(1,value.length());
    }
    else if (colonPos>0)
    { 
      String nsPrefix=value.substring(0,colonPos);
      URI nsURI=resolver.resolvePrefix(nsPrefix);
      if (nsURI==null)
      { 
        throw new DataException
          ("Namespace prefix '"+nsPrefix+"' not found.");
      }
      value=combineName(nsURI.toString(),value.substring(colonPos+1));
    }
    else if (resolver.resolvePrefix("")!=null)
    { value=combineName(resolver.resolvePrefix("").toString(),value);
    }
    else
    { 
      if (debug)
      { log.fine("Default namespace is not defined for value '"+value+"'");
      }
    }
    return stringPool.get(value);
  }
  
  protected final ClassLog log
    =ClassLog.getInstance(getClass());

  // Makes FrameHandler thread-safe
  class LocalStack 
    extends ThreadLocal<Stack<ForeignDataHandler.HandledFrame>>
  {
      
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
    { 
      
      if (get()==null)
      { set(new Stack<ForeignDataHandler.HandledFrame>());
      }
      
      get().push(val);
    }
    
    public ForeignDataHandler.HandledFrame peek()
    { return get().peek();
    }
    
    public boolean isEmpty()
    { return get()==null;
    }
  }

  private String elementURI;
  private FrameHandler[] children;
  
  private LinkedHashMap<String,FrameHandler> childMap
    =new LinkedHashMap<String,FrameHandler>();
  
  private boolean strictMapping;
  
  private LocalStack stack=new LocalStack();
  
  
  protected boolean debug;
  
  private FrameHandler parent;

  private boolean bindCalled;
  
  private Focus<?> focus;
  private ClosureFocus<?> closureFocus;
  
  private AttributeBinding<?>[] attributeBindings;
  
  private HashMap<String,AttributeBinding<?>> attributeMap;

  private Assignment<?>[] defaultAssignments;
  protected Setter<?>[] defaultSetters;
  
  private Expression<Object> textBinding;
  private Channel<Object> textChannel;
  private boolean textAssignment;
  private ThreadLocalChannel<String> text;
  private StringConverter<Object> textConverter;
  
  private String id;
  private boolean recursive;
  
  private boolean allowMixedContent;

  private HashMap<String,URI> prefixMappings;

  private ElementAssignment<?>[] elementAssignments;
  @SuppressWarnings({ "rawtypes" })
  private HashMap<String,Setter> elementSetterMap;
  
  private URI defaultURI;
  
  private StringPool stringPool;
  private boolean captureChildObject;
  private Binding<?> afterClose;

  public void setDefaultURI(URI defaultURI)
  { 
    this.defaultURI=defaultURI;
    if (prefixMappings==null)
    { prefixMappings=new HashMap<String,URI>();
    }
    prefixMappings.put("",this.defaultURI);
  }
    
  
  @Override
  public String getElementURI()
  { return elementURI;
  }
  
  public void setElementURI(String elementURI)
  { this.elementURI = elementURI;
  }
  
  
  public void setElementAssignments(ElementAssignment<?>[] elementAssignments)
  { 
    this.elementAssignments=elementAssignments;
  }
  
  
  public void setDefaultAssignments(Assignment<?>[] defaultAssignments)
  { this.defaultAssignments=defaultAssignments;
  }

  @Override
  public void setStringPool(StringPool stringPool)
  { this.stringPool=stringPool;
  }
  
  protected boolean isHandlingText()
  { return textChannel!=null;
  }
  
  public void setPrefixMappings(PrefixMapping[] mappings)
  {
    prefixMappings=new HashMap<String,URI>();
    for (PrefixMapping mapping:mappings)
    { prefixMappings.put(mapping.getPrefix(),mapping.getURI());
    }
    
    if (defaultURI!=null && prefixMappings.get("")==null)
    { prefixMappings.put("",defaultURI);
    }
  }
  
  @Override
  public URI resolvePrefix(String prefix)
  {
    URI mapping=prefixMappings!=null?prefixMappings.get(prefix):null;
    if (mapping==null && parent!=null)
    { mapping=parent.resolvePrefix(prefix);
    }
    return mapping;
  }
  
  @Override
  public Map<String,URI> computeMappings()
  { 
    Map<String,URI> computedMappings=new HashMap<String,URI>();

    Map<String,URI> parentMappings
      =parent!=null?parent.computeMappings():null;
    if (parentMappings!=null)
    { computedMappings.putAll(parentMappings);
    }
    
    if (prefixMappings!=null)
    { computedMappings.putAll(prefixMappings);
    }
    return computedMappings;
  }    
  
  /**
   * Allows a nested FrameHandler to refer to this FrameHandler using the
   *   findFrameHandler(String id) method.
   * 
   * @param id
   */
  public void setId(String id)
  {
    this.id=id;
    this.recursive=true;
  }
  
  public void setAfterClose(Binding<?> afterClose)
  { this.afterClose=afterClose;
  }
  
  @Override
  public FrameHandler findFrameHandler(String id)
  {
    if (this.id!=null && id.equals(this.id))
    { return this;
    }
    else if (parent!=null)
    { return parent.findFrameHandler(id);
    }
    else
    { return null;
    }
  }
  
  /**
   * Called only by another FrameHandler when children are set up
   * 
   * @param parent
   */
  @Override
  public void setParent(FrameHandler parent)
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
  public void setTextBinding(Expression<Object> textBinding)
  { this.textBinding=textBinding;
  }
  
  /**
   * <p>Specify the StringConverter that will convert character data
   *   to the destination type
   * </p>
   * @param stringConverter
   */
  public void setTextConverter(StringConverter<Object> stringConverter)
  { this.textConverter=stringConverter;
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
  @Override
  public void bind()
    throws BindException
  {
    bindCalled=true;
    if (stringPool==null)
    { stringPool=StringPool.INSTANCE;
    }
    bindAttributes();
    bindAssignments();
    bindChildren();
    bindElementAssignments();
    if (afterClose!=null)
    { afterClose.bind(getFocus());
    }
  }
  
  public void setFocus(Focus<?> focus)
  { this.focus=focus;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Cast current Focus to requested generic
  public <X> Focus<X> getFocus()
  { 
    Focus<X> ret=(Focus<X>) focus;
    if (ret==null && parent!=null)
    { 
      ret=parent.getFocus();
      if (recursive)
      { 
        // Push the ClosureFocus into this object as soon as a focus is
        //   first requested.
        closureFocus=new ClosureFocus<X>(ret);
        ret=(Focus<X>) closureFocus;
        focus=closureFocus;
      }
    }
    return ret;
    
  }
  
  protected void bindAssignments()
    throws BindException
  { defaultSetters=Assignment.bindArray(defaultAssignments,getFocus());
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void bindElementAssignments()
    throws BindException
  { 
    if (elementAssignments!=null)
    {
      elementSetterMap=new HashMap<String,Setter>();
      for (ElementAssignment assignment:elementAssignments)
      { 
        try
        {
          String elementName=transformNamespace(assignment.getName(),this);
          FrameHandler child=childMap.get(elementName);
          if (child==null)
          { 
            throw new BindException
              ("Child element "+elementName
              +" not found binding assignment with target "
              +assignment.getTarget().getText()
              );
          }
          
          try
          {
            elementSetterMap.put
              (elementName
              ,assignment.bind 
                (getFocus().telescope(child.getFocus().getSubject()))
              );
          }
          catch (BindException x)
          { 
            throw new BindException
              ("Error binding assignent of "+elementName+" child of "
              +elementURI
              ,x
              );
          }
        }
        catch (DataException x)
        { 
          throw new BindException
            ("Error mapping element namespace for Assignment",x);
        }
      }
    }
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
          (stringPool.get(binding.getAttribute())
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
      if (textBinding.getRootNode() instanceof AssignmentNode<?,?>)
      { 
        textAssignment=true;
        
        text=new ThreadLocalChannel<String>
          (BeanReflector.<String>getInstance(String.class));
        textChannel=getFocus().telescope(text).bind(textBinding);
      }
      else
      { 
        textChannel=getFocus().bind(textBinding);
        if (!String.class.isAssignableFrom
              (textChannel.getReflector().getContentType())
            )
        {
          textConverter=createTextConverter();
        }
      }
    }
  }

  protected StringConverter<Object> createTextConverter()
  { return textChannel.getReflector().getStringConverter();
  }
  
  protected void bindChildren()
    throws BindException
  {
    childMap.clear();
    if (children!=null)
    {
      for (FrameHandler child:children)
      { 
        child.setParent(this);
        child.setStringPool(stringPool);
        child.bind();
        try
        {
          if (child.getElementURI()==null)
          { throw new BindException("Element URI must be specified for "+child);
          }
          String elementURI=transformNamespace(child.getElementURI(),child);
          childMap.put(stringPool.get(elementURI), child);
          if (debug)
          { log.fine(elementURI+": Mapped child name "+elementURI+" to "+child);
          }
        }
        catch (DataException x)
        { throw new BindException
            ("Error mapping namespace in "+child.getElementURI(),x);
        }
        
      }

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
  @Override
  public boolean isStrictMapping()
  { return strictMapping;
  }
  
  public void setStrictMapping(boolean strictMapping)
  { this.strictMapping=strictMapping;
  }
  
  
  public void setChildren(FrameHandler[] children)
  { this.children=children;

  }
  
  public FrameHandler[] getChildren()
  { return this.children;
  }

  @Override
  public LinkedHashMap<String,FrameHandler> getChildMap()
  { return childMap;
  }
  
  protected void applyAttributes()
    throws DataException
  { 
    Attributes attributes=stack.peek().getAttributes();
    if (attributes==null)
    { return;
    }
    
    for (int i=0;i<attributes.getLength();i++)
    {
      String name=stringPool.get(attributes.getLocalName(i));
      String uri=stringPool.get(attributes.getURI(i));
      String fullName=stringPool.get(combineName(uri,name));
      String value=stringPool.get(attributes.getValue(i));
      
      
      AttributeBinding<?> binding
        =attributeMap!=null?attributeMap.get(fullName):null;
        
      if (binding!=null)
      { 
        if (binding.getTransformNamespace())
        { value=transformNamespace(value,stack.peek());
        }
        
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
  @Override
  public final void openFrame(ForeignDataHandler.HandledFrame frame)
    throws DataException
  { 
    if (!bindCalled)
    { 
      throw new DataException
        ("FrameHandler must be bound before it can be used");
    }
    
    if (stack.isEmpty() && closureFocus!=null)
    { 
      // Only for the initial frame- RecursiveFrame will do this for others
      closureFocus.push();
    }
    
    stack.push(frame);
    
    if (debug)
    { 
      log.fine("Opening Frame: URI="+elementURI+" frame="+frame);
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
  @Override
  @SuppressWarnings({ "rawtypes" })
  public final void closingChild(FrameHandler child)
    throws DataException
  { 
    if (elementSetterMap!=null)
    {
      String childName=transformNamespace(child.getElementURI(),this);
      Setter setter=elementSetterMap.get(childName);
      
      if (debug)
      { log.fine("Child "+childName+" assignment: "+setter);
      }
      
      if (setter!=null)
      { setter.set();
      }
    }
      
    
  }
  
  /**
   * <p>Interface between the DataHandler frame and the FrameHandler to
   *   indicate when a Frame is about to be closed 
   * </p>
   * 
   * @param frame
   */
  @Override
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
      else if (textConverter!=null)
      { textChannel.set(textConverter.fromString(chars));
      }
      else
      {
        
        String orig=(String) textChannel.get();
        if (orig==null)
        { textChannel.set(chars);
        }
        else
        { textChannel.set(stringPool.get(orig+chars));
        }
      }
    }
      
    closeData();
    if (afterClose!=null)
    { afterClose.get();
    }
    
    if (parent!=null)
    { parent.closingChild(this);
    }
    
    if (debug)
    { log.fine("Closing Frame: URI="+elementURI+" frame="+frame);
    }
    
    if (stack.pop()!=frame)
    { 
      throw new IllegalStateException
        ("Internal Error: DataHandler and FrameHandler stack out of sync");
    }
    
    if (stack.isEmpty() && closureFocus!=null)
    { 
      // Only for the initial frame- RecursiveFrame will do this for others
      closureFocus.pop();
    }
  }
  
  /**
   * 
   * @return A RecursionContext which pushes channel data from the last
   *   segment of a recursive chain to the first.
   */
  @Override
  public ClosureFocus<?>.RecursionContext 
    getRecursionContext(Focus<?> focusChain)
  { 
    return closureFocus.getRecursionContext(focusChain);
  }
  
  protected ForeignDataHandler.HandledFrame getFrame()
  { return stack.peek();
  }
  
  
  public void setAllowMixedContent(boolean allowMixedContent)
  { this.allowMixedContent=allowMixedContent;
  }
  
  @Override
  public boolean getAllowMixedContent()
  { return allowMixedContent;
  }

  /**
   * <p>Whether this frame should capture the object created by its child 
   *   frame when the child frame closes.
   * </p>
   * 
   * @param captureChildObject
   */
  @Override
  public void setCaptureChildObject(boolean captureChildObject)
  { this.captureChildObject=captureChildObject;
  }

  @Override
  public boolean getCaptureChildObject()
  { return captureChildObject;
  }
  
  @Override
  public void capturedChildObject
    (Object childObject,ForeignDataHandler.HandledFrame myFrame)
  {
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
