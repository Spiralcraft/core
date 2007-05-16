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

public interface Tree<Nc,T>
  extends Iterable<Nc>
{
  void set(T object);
  
  T get();
  
  Nc getParent();
  
  void setParent(Nc parent);
  
  void addChild(Nc child);
  
  void removeChild(Nc child);
  
  boolean isLeaf();
  
  public Nc[] getChildren();
}
