package fun.qianrui.staticUtil.swing;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * @author 88382571
 * 2019/5/7
 */
public class SimpleScrollBarUI extends BasicScrollBarUI {
     SimpleScrollBarUI() {
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        c.setPreferredSize(new Dimension(8, 0));
        return super.getPreferredSize(c);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        g.translate(thumbBounds.x, thumbBounds.y);
        g.setColor(Color.gray);
        g.fillRoundRect(0, 0, 8, thumbBounds.height - 1, 0, 0);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createDecreaseButton(orientation);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton jButton = new JButton();
        jButton.setBorderPainted(false);
        jButton.setContentAreaFilled(false);
        jButton.setBorder(null);
        return jButton;
    }

}
