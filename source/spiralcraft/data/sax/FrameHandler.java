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

import java.util.LinkedHashMap;

import spiralcraft.common.NamespaceResolver;
import spiralcraft.data.DataException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ClosureFocus;


/**
 * <p>Implements a mapping from a foreign XML data element to part of a
 *   Tuple set.
 * </p>
 * 
 * @author mike
 *
 */
public interface FrameHandler
  extends NamespaceResolver
{
  
  
  /**
   * <p>Recursively bind queries and expressions to the application context.
   * </p>
   * 
   * @param focus
   */
  void bind()
    throws BindException;
  

  /**
   * <p>Return the mapping of elementURIs to child FrameHandlers
   * </p>
   * 
   *
   */
  LinkedHashMap<String,FrameHandler> getChildMap();
  
  /**
   * <p>Interface between the DataHandler frame and the FrameHandler to
   *   indicate when a new Frame has been opened (ie. new element)
   * </p>
   * 
   * @param frame
   */
  void openFrame(ForeignDataHandler.HandledFrame frame)
    throws DataException;
 
  
  /**
   * <p>Interface between the DataHandler frame and the FrameHandler to
   *   indicate when a Frame is about to be closed 
   * </p>
   * 
   * @param frame
   */
  void closeFrame(ForeignDataHandler.HandledFrame frame)
    throws DataException;

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
  boolean isStrictMapping();
  
  /**
   * Find an ancestor FrameHandler by id
   * @param id
   * @return
   */
  FrameHandler findFrameHandler(String id);  
  

  /**
   * Return the Focus exported by this FrameHandler to its children
   * 
   * @param <X>
   * @return
   */
  <X> Focus<X> getFocus();  
  
  
  /**
   * Return the fully qualified URI for the element handled by this 
   *   FrameHandler
   * 
   * @return
   */
  String getElementURI();
  
  /**
   * Provide this FrameHandler with its parent context
   * 
   * @return
   */
  void setParent(FrameHandler parent);
  
  /**
   * Whether this Frame accepts both non-whitespace character data and
   *   contained elements
   *   
   * @return
   */
  boolean getAllowMixedContent();
  
  /**
   * Called once child frame processing has been completed to allow a parent
   *   to read the data context
   */
  void closingChild(FrameHandler child)
    throws DataException;
  
  /**
   * 
   * @return A RecursionContext which pushes channel data from the last
   *   segment of a recursive chain to the first.
   */
  ClosureFocus<?>.RecursionContext getRecursionContext(Focus<?> focusChain);
  
}
