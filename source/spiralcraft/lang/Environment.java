package spiralcraft.lang;

/**
 * Provides a starting point for the evaluation of expressions by 
 * binding names to Optics which expose points in an underlying data model.
 */
public interface Environment
{
  public Optic resolve(String name);
  
  
}
