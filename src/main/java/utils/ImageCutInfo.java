package utils;

/**
 * Created by larryfu on 2015/12/22.
 */

/**
* @author larryfu
*/
public class ImageCutInfo {

    public ImageCutInfo(int h, int viewHeight, int viewWidth, int w, int x, int y) {
        this.h = h;
        this.viewHeight = viewHeight;
        this.viewWidth = viewWidth;
        this.w = w;
        this.x = x;
        this.y = y;
    }

    private int viewWidth, viewHeight, x, y, w, h;

   public int getViewWidth() {
       return viewWidth;
   }

   public void setViewWidth(int viewWidth) {
       this.viewWidth = viewWidth;
   }

   public int getViewHeight() {
       return viewHeight;
   }

   public void setViewHeight(int viewHeight) {
       this.viewHeight = viewHeight;
   }

   public int getX() {
       return x;
   }

   public void setX(int x) {
       this.x = x;
   }

   public int getY() {
       return y;
   }

   public void setY(int y) {
       this.y = y;
   }

   public int getW() {
       return w;
   }

   public void setW(int w) {
       this.w = w;
   }

   public int getH() {
       return h;
   }

   public void setH(int h) {
       this.h = h;
   }
}
