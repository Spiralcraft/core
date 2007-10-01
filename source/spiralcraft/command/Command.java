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
package spiralcraft.command;

/**
 * <p>A parameterized invocation of an action performed against a target object
 * </p>
 * 
 * <p>A Command object encapsulates some action, configured in a manner
 *   determined by the implementing class (ie. Bean properties), that is
 *   executed against a target object, preserving the results of that action.
 * </p>
 * 
 * <p>The Command interface and related components provide the following
 *   benefits.
 * </p>
 * 
 * <ul>
 *   <li>Achieves the separation of multiple concerns: resolving the
 *     reference to the Command target, parameterizing the Command, 
 *     triggering the execution of the Command, and retrieving the
 *     results of execution.
 *   </li>
 *   
 *   <li>Allows for the temporal separation of Command creation and Command
 *     invocation, permitting asynchronous and remote invocation.
 *   </li>
 * </ul>
 * 
 * <h3>Lifecycle</h3>
 * 
 * <p>An instance of a specific Command is created by a CommandContext, which
 *   associates the Command with one or more target objects. This frees both
 *   the creator and the invoker of the Command from knowledge of the target.
 * </p>
 * 
 * <p>The Command is parameterized via an implementation specific mechanism
 *   (eg. Bean properties).
 * </p>
 * 
 * <p>The Command is executed via the execute() method
 * </p>
 * 
 * <p>The Command results may be retrieved via an implementation specific
 *   mechanism
 * </p>
 * 
 * <p>If supported, the Command may be undone via the undo() method.
 * </p>
 * 
 * <h3>State</h3>
 * 
 * <p>A single instance of the Command object is associated with an
 *   implementation specific set of parameters at the time that execute()
 *   is called. As such, the Command object is stateful and not thread-safe.
 * </p>
 * 
 * <p>The parameters of a Command cannot be modified during or after
 *   invocation. This restriction should be enforced by implementations by
 *   throwing a java.lang.IllegalStateException.
 * </p>
 * 
 * <p>A Command cannot be executed more than once. This restriction should be
 *   enforced by implementations by throwing a java.lang.IllegalStateException.
 * </p>
 * 
 * <p>A Command may be cloned. The clone will be in a pre-execution state, and
 *   will contain the same parameters as the original. To execute a Command
 *   more than once, clone it and execute the clone.
 * </p>
 *
 */
public interface Command
{  
  
  /**
   * Execute the Command
   */
  void execute();
  
  /**
   * 
   * @return A copy of this Command which references the same target(s) and
   *   parameters as this Command but is in a pre-execution state.
   */
  Command clone();
  
  /**
   * 
   * @return Whether this Command has started execution
   */
  boolean isStarted();
  
  /**
   * 
   * @return Whether this Command has completed execution
   */
  boolean isCompleted();
  
  /**
   * 
   * @return The Exception, if any, which caused this command to terminate 
   */
  Exception getException();
  
}
