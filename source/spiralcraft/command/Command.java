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
 * <p>The Command interface and related components enable the following
 *   concerns to be separated: 
 * </p>
 * 
 * <ul>
 *   <li>Command instantiation: The CommandFactory interface separates the
 *     creation and configuration of the Command from the UI component that
 *     uses it, minimizing the coupling between UI components and specific
 *     means of resolving and configuring the Command objects they reference.
 *   </li>
 *   
 *   <li>Command target resolution: The Command target may be set by any
 *     component that has access to the Command before it executes, to allow
 *     a different component to reference the command target than the one which
 *     references the command. This allows, for instance, multiple "action"
 *     components to reference different commands that operate on the same
 *     target, referenced by a common container. This also permits a system to
 *     resolve the target of a remote command on the "server" side.
 *   </li>
 *     
 *   <li>Command execution: A Command may be executed in a different time or
 *     thread context than the action which triggers it, enabling asynchronous
 *     or remote invocation.
 *   </li>
 *   
 *   <li>Command result processing: The "result" of a command, as well as any
 *     Exception generated during execution, may be processed in a different
 *     time or thread context. This allows a remote system to instantiate a
 *     Command, pass it to a "server" for execution, and receive the result for
 *     local processing. 
 *   </li>
 *   
 * </ul>
 * 
 * <h3>Lifecycle</h3>
 * 
 * <ul>
 *  <li>An instance of a specific Command is created by a CommandFactory, which
 *   creates the Command object and provides it with configuration information.
 *  </li>
 * 
 *  <li>The Command is provided with a target object.
 *  </li>
 * 
 *  <li>The Command is executed via the execute() method
 *  </li>
 * 
 *  <li>The Command results may be retrieved via the generic getResult() method
 *   which returns an implementation specific interface or class.
 *  </li>
 * 
 *  <li>If supported, the Command may be undone via the undo() method.
 *  </li>
 * 
 * </ul>
 * 
 * <h3>State</h3>
 * 
 * <p>A single instance of the Command object is associated with an
 *   implementation specific set of parameters and a specific target at the
 *   time that execute() is called. As such, the Command object is stateful 
 *   and not thread-safe.
 * </p>
 * 
 * <p>The parameters and target of a Command cannot be modified during or after
 *   invocation. This restriction should be enforced by implementations by
 *   throwing a java.lang.IllegalStateException.
 * </p>
 * 
 * <p>A Command cannot be executed more than once. This restriction should be
 *   enforced by implementations by throwing a java.lang.IllegalStateException.
 * </p>
 * 
 * <p>A Command may be cloned. The clone will be in a pre-execution state, and
 *   will contain the same parameters and target as the original. To execute a
 *   Command more than once, clone it and execute the clone.
 * </p>
 *
 */
public interface Command<Ttarget,Tresult>
{  
  
  /**
   * Execute the Command
   */
  void execute();

  /**
   * Undo the command, if undo is supported.
   */
  void undo();
  
  /**
   * Specify the target of this command
   * 
   * @param target
   */
  void setTarget(Ttarget target); 

  /**
   * 
   * @return The target of this command
   */
  Ttarget getTarget();
  
  /**
   * 
   * @return A copy of this Command which references the same target(s) and
   *   parameters as this Command but is in a pre-execution state.
   */
  Command<Ttarget,Tresult> clone()
    throws CloneNotSupportedException;
  
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
  
  
  /**
   * @return The result of this command execution, if any 
   */
  Tresult getResult();
  
  /**
   * 
   * @return Whether the Command is reversable. This may depend on the state
   *   of the Command target.
   */
  boolean isUndoable();
  
  
  
}
