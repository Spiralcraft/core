package spiralcraft.lang;

/**
 * Base interface for functionality extensions applied to Optics. 
 */ 
public interface Decorator
{
  /**
   * Associate the decorator with an optic
   */
  public void bind(Optic optic)
    throws BindException;
    
}
