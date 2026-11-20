package net.bms.roleplay_status;

import net.bms.roleplay_status.dice.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;
import static org.junit.jupiter.api.Assertions.*;

class DiceTest {
    @Test void parsesSupportedNotationAndRejectsUnsafeOrUnsupportedRolls() {
        assertEquals(new DiceExpression(1,20,0), DiceExpression.parse("d20"));
        assertEquals(new DiceExpression(2,6,0), DiceExpression.parse("2d6"));
        assertEquals(new DiceExpression(1,20,3), DiceExpression.parse("1d20+3"));
        assertEquals(new DiceExpression(1,4,-2), DiceExpression.parse("D4-2"));
        for(String invalid: List.of("0d20","21d6","d3","d0","d999","d20+1001","-1d6","2d6 test","999999999999d6","d20+9999999999")) assertNull(DiceExpression.parse(invalid),invalid);
        for(int sides: List.of(2,4,6,8,10,12,20,100)) assertNotNull(DiceExpression.parse("20d"+sides+"-1000"));
    }
    @Test void serverRandomValuesStayInBoundsAndModifierAppliesOnce() {
        var random=new Random(42);
        for(int sides: List.of(2,4,6,8,10,12,20,100)) {
            var die=new DiceExpression(20,sides,-3);
            for(int i=0;i<100;i++) {
                var results=die.roll(random);
                assertEquals(20,results.size());
                assertTrue(results.stream().allMatch(n -> n>=1 && n<=sides));
                assertEquals(results.stream().mapToInt(Integer::intValue).sum()-3,die.total(results));
            }
        }
    }
    @Test void nearbyMeansInclusiveSphericalDistanceInSameDimension() {
        assertTrue(DiceExpression.withinRange(true,625,25));
        assertFalse(DiceExpression.withinRange(true,625.01,25));
        assertFalse(DiceExpression.withinRange(false,0,25));
        assertTrue(DiceExpression.withinRange(true,0,25));
    }
    @Test void reasonIsPlainBoundedText() {
        assertEquals("Stealth check",DiceExpression.reason("  Stealth\n check\u00a7\u200b  "));
        assertEquals(128,DiceExpression.reason("a".repeat(200)).length());
        assertEquals(127,DiceExpression.reason("a".repeat(127)+"\ud83c\udfb2").length());
    }
    @Test void everyPolyhedronHasTheCorrectNumberOfRealFaces() {
        for(int sides: List.of(4,6,8,10,12,20)) {
            var mesh=DieGeometry.mesh(sides);
            assertEquals(sides,mesh.getFaces().size(),"d"+sides);
            for(var face:mesh.getFaces()) assertTrue(face.size()>=3);
            for(double angle: List.of(0.0,0.5,2.3,5.0)) {
                var projected=DieGeometry.project(sides,angle);
                assertFalse(projected.isEmpty());
                for(var face:projected) for(var pt:face.getPoints()) {
                    assertTrue(Double.isFinite(pt.getFirst())); assertTrue(Double.isFinite(pt.getSecond()));
                }
            }
        }
        assertEquals(2,DieGeometry.mesh(2).getFaces().stream().filter(f -> f.size()==20).count());
    }
    @Test void payloadRejectsForgedOrUnboundedValues() {
        assertThrows(IllegalArgumentException.class,() -> new DicePayload("Player",new DiceExpression(1,20,0),List.of(21),""));
        assertThrows(IllegalArgumentException.class,() -> new DicePayload("Player",new DiceExpression(2,6,0),List.of(1),""));
        assertThrows(IllegalArgumentException.class,() -> new DicePayload("Player",new DiceExpression(1,6,0),List.of(1),"x".repeat(129)));
    }
    @Test void exportsActualRendererGeometryForPreview() throws Exception {
        var frames=new ArrayList<Object>();
        for(int f=0;f<36;f++) {
            double age=f/12.0;
            double spin=age<1.2 ? Math.pow(1-age/1.2,2)*Math.PI*5:0;
            var dice=new LinkedHashMap<Integer,Object>();
            for(int sides:List.of(2,4,6,8,10,12,20)) dice.put(sides,DieGeometry.project(sides,spin));
            frames.add(dice);
        }
        Path output=Path.of("build/dice-preview.json");Files.createDirectories(output.getParent());
        Files.writeString(output,new Gson().toJson(frames));
    }
}
