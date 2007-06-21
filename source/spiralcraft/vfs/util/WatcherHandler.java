package spiralcraft.vfs.util;

public interface WatcherHandler
{
  
  
  /**
   * Handle the event of an update to the Watched resource
   * 
   * @return A time in ms:
   * <UL>
   *   <LI>0, if the update was processed successfully</LI>
   *   <LI>+Tms, if the update was processed successfully and
   *     the Watcher should wait no less than Tms milliseconds before checking
   *     again for an update.</LI>
   *   <LI>-Tms, if the update was not processed successfully- the Watcher will
   *     wait Tms seconds before calling handleUpdate() again for the 
   *     <I>same</I> update.</LI> 
   * </UL>
   */
  int handleUpdate();
}
