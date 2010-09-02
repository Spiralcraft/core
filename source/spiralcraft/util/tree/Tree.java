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
package spiralcraft.util.tree;

/**
 * An interface which allows application specific Tree implementations to
 *   be manipulated by general purpose components.
 * 
 * @author mike
 *
 * @param <Nc>
 * @param <T>
 */
public interface Tree<Nc,T>
  extends Iterable<Nc>
{
  /**
   * Associate a peer object with this node
   * 
   * @param object
   */
  void set(T object);
  
  /**
   * Retrieve the peer object associated with this node
   * 
   * @return
   */
  T get();
  
  /**
   * Return the parent node
   * 
   * @return
   */
  Nc getParent();
  
  /**
   * Reset the parent node. This method will always be called by addChild when
   *   a Child is added to a parent.
   * 
   * @param parent
   */
  void setParent(Nc parent);
  
  /**
   * Add a child to this node. This method should call setParent(this) on
   *   the child node.
   * 
   * @param child
   */
  void addChild(Nc child);
  
  /**
   * Remove a child from this node. This method will NOT call setParent(null)
   * 
   * @param child
   */
  void removeChild(Nc child);
  
  /**
   * Whether the node has any children
   * 
   * @return
   */
  boolean isLeaf();
  
  /**
   * The children of this node
   * 
   * @return
   */
  public Nc[] getChildren();
}
