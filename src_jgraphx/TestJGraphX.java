import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import javax.swing.*;

/**
 * Test class to verify that JGraphX library is properly installed.
 * Run this FIRST before running FlowchartEditorApp.
 */
public class TestJGraphX {
    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println("Testing JGraphX Library...");
        System.out.println("==================================");

        try {
            // Test 1: Create mxGraph
            System.out.print("Test 1: Creating mxGraph... ");
            mxGraph graph = new mxGraph();
            System.out.println("✓ OK");

            // Test 2: Create mxGraphComponent
            System.out.print("Test 2: Creating mxGraphComponent... ");
            mxGraphComponent component = new mxGraphComponent(graph);
            System.out.println("✓ OK");

            // Test 3: Add a simple vertex
            System.out.print("Test 3: Adding vertex... ");
            Object parent = graph.getDefaultParent();
            graph.getModel().beginUpdate();
            try {
                graph.insertVertex(parent, null, "Test", 20, 20, 80, 30);
            } finally {
                graph.getModel().endUpdate();
            }
            System.out.println("✓ OK");

            // Test 4: Create a simple window
            System.out.print("Test 4: Creating window... ");
            JFrame frame = new JFrame("JGraphX Test - SUCCESS!");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(component);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            System.out.println("✓ OK");

            System.out.println("==================================");
            System.out.println("✓✓✓ ALL TESTS PASSED! ✓✓✓");
            System.out.println("JGraphX is working correctly!");
            System.out.println("You can now run FlowchartEditorApp");
            System.out.println("==================================");

            // Show the window
            SwingUtilities.invokeLater(() -> frame.setVisible(true));

        } catch (NoClassDefFoundError e) {
            System.err.println("✗✗✗ FAILED!");
            System.err.println("==================================");
            System.err.println("ERROR: JGraphX library NOT found!");
            System.err.println("==================================");
            System.err.println("\nPlease follow these steps:");
            System.err.println("1. Download jgraphx.jar");
            System.err.println("2. In Eclipse: Right-click project → Properties");
            System.err.println("3. Go to: Java Build Path → Libraries");
            System.err.println("4. Click: Add External JARs...");
            System.err.println("5. Select: jgraphx.jar");
            System.err.println("6. Click: Apply and Close");
            System.err.println("7. Run this test again");
            System.err.println("\nError details:");
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("✗ FAILED!");
            System.err.println("Unexpected error:");
            e.printStackTrace();
        }
    }
}
