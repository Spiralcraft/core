//
// Copyright (c) 2026 Michael Toth
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
package spiralcraft.lang.functions;

/**
 * <p>The context of one traversal of a fn:Walk, durable over the
 *   traversal, and published as [WalkCursor] only to the walk's own
 *   expressions (step, while, filter and out).
 * </p>
 *
 * <p>It holds the position of the current node in the walk, the node,
 *   whether the node is the last of the walk, and the traversal's state,
 *   whatever the walk's state expression evaluated to when the traversal
 *   started.
 * </p>
 *
 * @param <T> The type of the nodes of the walk
 * @param <S> The type of the state
 */
public class WalkCursor<T,S>
{
  private int index=-1;
  private T value;
  private boolean last;
  private S state;

  public WalkCursor(S state)
  { this.state=state;
  }

  /**
   * @return The position of the current node in the walk, the walk's
   *   source being 0
   */
  public int getIndex()
  { return index;
  }

  /**
   * @return The current node
   */
  public T getValue()
  { return value;
  }

  /**
   * @return Whether the current node is the last node of the walk: the step
   *   from it returns null, or a node for which while is false.
   *   Before the step from the current node has run, false.
   */
  public boolean isLast()
  { return last;
  }

  /**
   * @return The state of the traversal
   */
  public S getState()
  { return state;
  }

  /**
   * <p>Replace the state of the traversal, for a state an expression
   *   updates by assignment, such as a counter.
   * </p>
   */
  public void setState(S state)
  { this.state=state;
  }

  /**
   * <p>Move the cursor to a node. Called by the walk only, not a property,
   *   so that the position is read-only to expressions and to code outside
   *   this package.
   * </p>
   */
  void position(int index,T value,boolean last)
  {
    this.index=index;
    this.value=value;
    this.last=last;
  }

  @Override
  public String toString()
  {
    return super.toString()
      +"[index="+index+", last="+last+", value="+value+"]";
  }
}
