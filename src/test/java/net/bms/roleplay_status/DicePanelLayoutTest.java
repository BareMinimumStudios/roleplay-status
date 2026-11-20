package net.bms.roleplay_status;
import net.bms.roleplay_status.client.DicePanelLayout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DicePanelLayoutTest {
    @Test void defaultAvoidsTopRightMinimap() {
        var b=DicePanelLayout.bounds(640,360,0,0,100);
        assertEquals(8,b.getX()); assertEquals(8,b.getY());
        assertEquals(222,b.getWidth()); assertEquals(113,b.getHeight());
    }
    @Test void allSizesAndCornersFitAfterResolutionChanges() {
        for(int w:new int[]{160,320,640,1920}) for(int h:new int[]{90,180,360,1080})
        for(int scale:new int[]{50,75,100,150}) for(int x:new int[]{0,50,100}) for(int y:new int[]{0,50,100}) {
            var b=DicePanelLayout.bounds(w,h,x,y,scale);
            assertTrue(b.getX()>=0 && b.getY()>=0);
            assertTrue(b.getX()+b.getWidth()<=w);
            assertTrue(b.getY()+b.getHeight()<=h);
            assertTrue(b.getScale()>0);
        }
    }
    @Test void sizeActuallyChangesPanelAndDraggingRoundTrips() {
        assertEquals(111,DicePanelLayout.bounds(640,360,0,0,50).getWidth());
        assertEquals(333,DicePanelLayout.bounds(640,360,0,0,150).getWidth());
        for(int percent:new int[]{0,25,50,75,100}) {
            var b=DicePanelLayout.bounds(640,360,percent,percent,75);
            assertEquals(percent,DicePanelLayout.position(b.getX(),640,b.getWidth()));
            assertEquals(percent,DicePanelLayout.position(b.getY(),360,b.getHeight()));
        }
        assertEquals(0,DicePanelLayout.position(-100,640,222));
        assertEquals(100,DicePanelLayout.position(9999,640,222));
        assertEquals(0,DicePanelLayout.position(10,16,16));
    }
}
