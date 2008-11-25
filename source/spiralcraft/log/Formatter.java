//
package spiralcraft.log;


public interface Formatter
{
  /**
   * Format the event
   */
  public String format(Event evt);

}