package spiralcraft.command;

/**
 * Interface for Commands to pass messages back to a user agent
 */
public interface MessageHandler
{
  /**
   * Pass a message back to the user agent. Each Object in the supplied
   *   Object[] is represents a unit of the message. By convention,
   *   simple user agents should represent these units as lines of text,
   *   converting the Objects to Strings using the toString() method.
   */
  void handleMessage(Object[] messageLines);
}
