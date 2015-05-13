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
package spiralcraft.lang;

/**
 * <p>A ChainableContext provides a way to compose a Context (the environment
 *   available to a thread after it enters a scope such as a component) from
 *   multiple providers. Providers who wish to expose data and functionality
 *   will implement ChainableContext to integrate into the environment.
 * </p> 
 * 
 * <p>Each Context chained will be bound to the scope provided by 
 *   the Context chained before it, and has access to ancestor Contexts in
 *   this fashion. A chain of ChainableContexts will be pushed into ThreadLocal
 *   context in order of their chaining, and will be popped from the ThreadLocal
 *   context in reverse order. Each Context can access whatever was pushed
 *   into its parent context.
 * </p>
 * 
 * @author mike
 *
 */
public interface ChainableContext
  extends Context
{

  
  /**
   * <p>Specify the Contextual to bind into the scope of this context, to the
   *   end of any existing chain. The
   *   bind() method will return the Focus<?> returned from the specified
   *   Contextual.bind(Focus) method.
   * </p>
   * 
   * <p>If a specified Contextual is also a Context, its push() method will be
   *   chained after this Context's push() method and its pop() method will be
   *   chained before this Context's pop() method.
   * </p>
   * 
   * <p>If the specified Contextual is a ChainableContext, this Context will 
   *   delegate the chain(method) to the supplied Context.
   * </p>
   * 
   * <p>If the specified Contextual is not a ChainableContext, it will be
   *   wrapped in one in order to maintain chainability.
   * </p>

   * <p>If the chain is already sealed (terminated) with an ordinary Contextual,
   *   an IllegalStateException will be thrown.
   * </p>
   * 
   * @param chain
   * @returns The specified context (for further chaining) if the 
   *   specified context is a ChainableContext.
   */
  ChainableContext chain(Contextual chain);
  
  /**
   * <p>Insert a ChainableContext into the Chain immediately after this 
   *   ChainableContext
   * </p>
   * @param chain
   */
  void insertNext(ChainableContext chain);
  
  /**
   * <p>Provide the last item to chain and prevent further chaining of this
   *   chain.
   * </p>
   * 
   * @param chain
   */
  void seal(Contextual chain);
}
