package spiralcraft.stream.batch;

import spiralcraft.util.Arguments;

import spiralcraft.stream.Resource;

public interface Operation
{
  /**
   * The operation that will be invoked after this one finishes
   */
  void setNextOperation(Operation next);
  
  /**
   * Invoke this operation on the specified resource
   */
  void invoke(Resource resource)
    throws OperationException;
  
  /**
   * Accept a configuration option from the specified argument set
   *
   *@return true if the option is recognized
   */
  boolean processOption(Arguments args,String option);
  
  /**
   * Accept an argument from the specified argument set
   *
   *@return true if the argument is recognized
   */
  boolean processArgument(Arguments args,String option);
}
