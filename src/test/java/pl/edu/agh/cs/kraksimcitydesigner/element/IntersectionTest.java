package pl.edu.agh.cs.kraksimcitydesigner.element;

import junit.framework.Assert;
import org.custommonkey.xmlunit.XMLAssert;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.testng.annotations.Test;
import pl.edu.agh.cs.kraksimcitydesigner.parser.ModelParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class IntersectionTest {
    
    private static ElementManager createElementManager(File sourceFile) {
    
        try {
            DisplaySettings ds = new DisplaySettings();
            ElementManager em = new ElementManager(ds);
            ModelParser.parse(em, sourceFile);
            return em;
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    
    static public String getContents(File aFile) throws IOException {

        StringBuilder contents = new StringBuilder();
        BufferedReader input =  new BufferedReader(new FileReader(aFile));
        try {
            String line = null; //not declared within while loop
            while (( line = input.readLine()) != null){
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        }
        finally {
            input.close();
        }
        return contents.toString();
    }

    
    @Test 
    public void allOutcomingLinksUsedTest() {
        
        ElementManager em;
        File testFile;
        Intersection intersection;
        
        testFile = new File("./tests_maps/allOutcomingLinksUsed/No1.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allOutcomingLinksUsed() == false;
        
        testFile = new File("./tests_maps/allOutcomingLinksUsed/No2.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allOutcomingLinksUsed() == false;
        
        testFile = new File("./tests_maps/allOutcomingLinksUsed/Yes1.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allOutcomingLinksUsed() == true;
        
        testFile = new File("./tests_maps/allOutcomingLinksUsed/Yes2.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allOutcomingLinksUsed() == true;
        
        testFile = new File("./tests_maps/allOutcomingLinksUsed/Yes3.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allOutcomingLinksUsed() == true;
    }

    @Test
    public void allIncomingLanesUsedTest() {
        
        ElementManager em;
        File testFile;
        Intersection intersection;
        
        testFile = new File("./tests_maps/allIncomingLanesUsed/Not1.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 3;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allIncomingLanesUsed() == false;
          
        testFile = new File("./tests_maps/allIncomingLanesUsed/Not2.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 3;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allIncomingLanesUsed() == false;
        
        // left lane not used
        testFile = new File("./tests_maps/allIncomingLanesUsed/Not3.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 3;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allIncomingLanesUsed() == false;
        
        // center lane not used, left used
        testFile = new File("./tests_maps/allIncomingLanesUsed/Not4.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 3;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allIncomingLanesUsed() == false;
        
        testFile = new File("./tests_maps/allIncomingLanesUsed/Yes1.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 3;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allIncomingLanesUsed() == true;
        
        // with left lane
        testFile = new File("./tests_maps/allIncomingLanesUsed/Yes2.xml");
        em = createElementManager(testFile);
        assert em.getNodes().size() == 3;
        intersection = (Intersection)em.findNodeById("I0");
        assert intersection.allIncomingLanesUsed() == true;

    }
    
    @Test(dependsOnGroups = {"getOrderedReachableNodes" })
    public void createDefaultActions3WaySimpleTest1() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions3WaySimple/correct1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        intersection.createDefaultActions3WaySimpleAngles(false);
        
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());
        
        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions3WaySimple/result1.xml"));
        
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test(dependsOnGroups = {"getOrderedReachableNodes" })
    public void createDefaultActions3WaySimpleTest2() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions3WaySimple/correct2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        intersection.createDefaultActions3WaySimpleAngles(false);
        
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());
        
        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions3WaySimple/result2.xml"));
        
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    
    
    @Test(dependsOnGroups = {"checkFor3WaySimpleAngles" })
    public void createDefaultActions3WaySimpleAnglesTest1() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions3WaySimpleAngles/correct1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        intersection.createDefaultActions3WaySimpleAngles(false);

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions3WaySimpleAngles/result1.xml"));
        
        /*
        System.out.println("---result-----");
        System.out.println(resultXML);
        System.out.println("--------");
        System.out.println(controlXML);
        System.out.println("---control-----");
        */
        
        //XMLAssert.assertTrue(TestHelper.checkXMLsimilar(controlXML, resultXML));
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test(dependsOnGroups = {"checkFor3WaySimpleAngles" })
    public void createDefaultActions3WaySimpleAnglesTest2() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions3WaySimpleAngles/correct2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        intersection.createDefaultActions3WaySimpleAngles(false);

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        String resultXML = outp.outputString(em.modelToDocument());
        
        //System.out.println(resultXML);
        String controlXML = getContents(new File("./tests_maps/createDefaultActions3WaySimpleAngles/result2.xml"));

        //XMLAssert.assertTrue(TestHelper.checkXMLsimilar("<a> <b><e/><c/></b> <b><f/><d/></b> </a>", "<a> <b><d/><f/></b> <b><c/><e/></b> </a>"));
        //XMLAssert.assertTrue(TestHelper.checkXMLsimilar(controlXML, resultXML));
        XMLAssert.assertXMLEqual(controlXML, resultXML);
        //XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test(dependsOnGroups = {"checkFor3WaySimpleAngles" })
    public void createDefaultActions3WaySimpleAnglesTest3() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions3WaySimpleAngles/test3.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean result = intersection.createDefaultActions3WaySimpleAngles(false);
        XMLAssert.assertFalse(result);
    }
    
    @Test(dependsOnGroups = {"checkFor3WaySimpleAngles" })
    public void createDefaultActions3WaySimpleAnglesTest4() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions3WaySimpleAngles/test4.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        XMLAssert.assertEquals(3, intersection.getArmActionsList().size());
        intersection.createDefaultActions3WaySimpleAngles(true);
        XMLAssert.assertEquals(3, intersection.getArmActionsList().size());

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions3WaySimpleAngles/result1.xml"));
        
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test(groups="areInLineWith")
    public void areInLineWithMeTest1() {
        
        DisplaySettings ds = new DisplaySettings();
        Intersection inter = new Intersection("X0",10,150,ds);
        
        Gateway gateway1 = new Gateway("G0",40,100,ds);
        Gateway gateway2 = new Gateway("G0",10,200,ds);
        
        // angle = 148 degree
        assert inter.areInLineWithMe(gateway1, gateway2) == false;
        
        Gateway gateway3 = new Gateway("G0",37,100,ds);
        Gateway gateway4 = new Gateway("G0",10,200,ds);
        
        // angle = 152 degree
        assert inter.areInLineWithMe(gateway3, gateway4) == true;
        
        // angle = 180 degree
        Gateway gateway5 = new Gateway("G0",10,40,ds);
        Gateway gateway6 = new Gateway("G0",10,361,ds);
        assert inter.areInLineWithMe(gateway5, gateway6) == true;
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestOder1() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/Order1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result != null;
        Assert.assertEquals("G1", result.get(0).getId());
        Assert.assertEquals("G2", result.get(1).getId());
        Assert.assertEquals("G0", result.get(2).getId());
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestOder2() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/Order2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result != null;
        Assert.assertEquals("G0", result.get(0).getId());
        Assert.assertEquals("G2", result.get(1).getId());
        Assert.assertEquals("G1", result.get(2).getId());
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestOder3() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/Order3.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result != null;
        Assert.assertEquals("G0", result.get(0).getId());
        Assert.assertEquals("G2", result.get(1).getId());
        Assert.assertEquals("G1", result.get(2).getId());
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestIncorrect1() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/incorrect1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result == null;
    }
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestIncorrect2() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/incorrect2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result == null;
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestCorrect1() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/correct1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result != null;
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestCorrect2() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/correct2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result != null; 
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkAnglesFor3WaySimpleTestCorrect3() {
        
        File testFile = new File("./tests_maps/checkAnglesFor3WaySimple/correct3.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        
        List<Node> result = intersection.checkAnglesFor3WaySimple();
        assert result != null; 
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkRoadsFor3WaySimpleTestCorrect1() {
        
        File testFile = new File("./tests_maps/checkRoadsFor3WaySimple/correct1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean result = intersection.checkRoadsFor3WaySimple();
        Assert.assertEquals(true, result);
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkRoadsFor3WaySimpleTestCorrect2() {
        
        File testFile = new File("./tests_maps/checkRoadsFor3WaySimple/correct2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean result = intersection.checkRoadsFor3WaySimple();
        Assert.assertEquals(true, result);
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkRoadsFor3WaySimpleTestCorrect3() {
        
        File testFile = new File("./tests_maps/checkRoadsFor3WaySimple/correct3.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean result = intersection.checkRoadsFor3WaySimple();
        Assert.assertEquals(true, result);
    }
    
    @Test(groups="checkFor3WaySimpleAngles")
    public void checkRoadsFor3WaySimpleTestIncorrect1() {
        
        File testFile = new File("./tests_maps/checkRoadsFor3WaySimple/incorrect1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 4;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean result = intersection.checkRoadsFor3WaySimple();
        Assert.assertEquals(false, result);
    }
    
    @Test(groups="getOrderedReachableNodes")
    public void getOrderedReachableNodesTest1() throws Exception {
        
        File testFile = new File("./tests_maps/getOrderedReachableNodes/test1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 5;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        List<Node> orderedNodes = intersection.getOrderedReachableNodes();
        Assert.assertEquals("G3", orderedNodes.get(0).getId());
        Assert.assertEquals("G2", orderedNodes.get(1).getId());
        Assert.assertEquals("G1", orderedNodes.get(2).getId());
        Assert.assertEquals("G0", orderedNodes.get(3).getId());
    }
    
    @Test(groups="getOrderedReachableNodes")
    public void getOrderedReachableNodesTest2() throws Exception {

        File testFile = new File("./tests_maps/getOrderedReachableNodes/test2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 5;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        List<Node> orderedNodes = intersection.getOrderedReachableNodes();
        Assert.assertEquals("G2", orderedNodes.get(0).getId());
        Assert.assertEquals("G3", orderedNodes.get(1).getId());
        Assert.assertEquals("G0", orderedNodes.get(2).getId());
        Assert.assertEquals("G1", orderedNodes.get(3).getId());

    }
    
    @Test(groups="getOrderedReachableNodes")
    public void getOrderedReachableNodesTest3() throws Exception {
        
        File testFile = new File("./tests_maps/getOrderedReachableNodes/test3.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 5;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        List<Node> orderedNodes = intersection.getOrderedReachableNodes();
        Assert.assertEquals("G2", orderedNodes.get(0).getId());
        Assert.assertEquals("G0", orderedNodes.get(1).getId());
        Assert.assertEquals("G3", orderedNodes.get(2).getId());
        Assert.assertEquals("G1", orderedNodes.get(3).getId());
        
    }
    
    @Test(dependsOnGroups = {"getOrderedReachableNodes" })
    public void createDefaultActions4WaySimpleTest1() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions4WaySimple/correct1.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 5;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean ok = intersection.createDefaultActions4WaySimple(false);
        Assert.assertTrue(ok);

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions4WaySimple/result1.xml"));
        
        //XMLAssert.assertTrue(TestHelper.checkXMLsimilar(controlXML, resultXML));
        /*
        System.out.println("result:");
        System.out.println(resultXML);
        System.out.println();
        */
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test(dependsOnGroups = {"getOrderedReachableNodes" })
    public void createDefaultActions4WaySimpleTest2() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions4WaySimple/correct2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 5;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean ok = intersection.createDefaultActions4WaySimple(true);
        Assert.assertTrue(ok);
        
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());
        
        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions4WaySimple/result1.xml"));
        
        //XMLAssert.assertTrue(TestHelper.checkXMLsimilar(controlXML, resultXML));
        /*
        System.out.println("result:");
        System.out.println(resultXML);
        System.out.println();
         */
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test(dependsOnGroups = {"getOrderedReachableNodes" })
    public void createDefaultActions4WaySimpleTest3() throws Exception {
        
        File testFile = new File("./tests_maps/createDefaultActions4WaySimple/correct2.xml");
        ElementManager em = createElementManager(testFile);
        assert em.getNodes().size() == 5;
        Intersection intersection = (Intersection)em.findNodeById("I0");
        boolean ok = intersection.createDefaultActions4WaySimple(false);
        Assert.assertFalse(ok);
        
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());
        
        String resultXML = outp.outputString(em.modelToDocument());
        String controlXML = getContents(new File("./tests_maps/createDefaultActions4WaySimple/correct2.xml"));
        
        //XMLAssert.assertTrue(TestHelper.checkXMLsimilar(controlXML, resultXML));

        //System.out.println(resultXML);
        //XMLAssert.assertXMLEqual("<a><b/></a>", "<a><b> </b></a>");
        XMLAssert.assertXMLEqual(controlXML, resultXML);
    }
    
    @Test
    public void ddd() {
        Node node1 = new Intersection("id",10,20,new DisplaySettings());
        Node node2 = new Intersection("id2",10,30,new DisplaySettings());
        
        ElementManager em = new ElementManager(new DisplaySettings());
        em.addRoad(node1, node2);
        Assert.assertEquals(1, em.getRoads().size());
        
        em.addRoad(node2, node1);
        Assert.assertEquals(1, em.getRoads().size());
    }
    
}
