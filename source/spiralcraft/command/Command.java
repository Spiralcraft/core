package spiralcraft.command;

/**
 * Encapsulates high level functionality executed on behalf of a user agent
 */
public interface Command
{
  /**
   * Create a new ParameterSet appropriate for this Command
   */
  ParameterSet newParameterSet();
  
  /**
   * Execute the command
   */
  Object execute(CommandContext context,ParameterSet params);
  
  /**
   * Short description of the command (ie. a tool tip), or an ID referencing
   *   such a description.
   */
  String getDescription();

  /**
   * The name of the Command
   */
  String getName();
}
