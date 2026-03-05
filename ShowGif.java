import javax.swing.*;

public class ShowGif {

    public static void display(String path) {

        JFrame frame = new JFrame("Hero Preview");

        ImageIcon gif = new ImageIcon(path);
        JLabel label = new JLabel(gif);

        frame.add(label);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        try {
            Thread.sleep(3000); // show gif for 3 seconds
        } catch (Exception e) {}

        frame.dispose();
    }
}
