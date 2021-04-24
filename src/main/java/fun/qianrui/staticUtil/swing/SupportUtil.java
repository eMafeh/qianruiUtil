package fun.qianrui.staticUtil.swing;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Predicate;

public class SupportUtil {
    private static final JFrame root = new JFrame();
    private static ImageIcon icon;
    private static volatile Predicate<String> callBack;
    static final int R = Toolkit.getDefaultToolkit()
            .getScreenSize().height / 2 - 50;
    static final JTextField timeInput = new JTextField() {
        {
            JTextField that = this;
            that.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (callBack != null) {
                            if (callBack.test(that.getText())) {
                                that.setText("");
                                callBack = null;
                            }
                        }
                    }
                }
            });
        }
    };
    private static final JComponent png = new JComponent() {
        @Override
        protected void paintComponent(Graphics g) {
            ImageIcon icon = SupportUtil.icon;
            if (icon != null) {
                icon.paintIcon(this, g, 0, 0);
            }
        }
    };

    static {
        root.add(timeInput);
        root.add(png);
        root.setBounds(R, R, R / 2, R / 6);
        root.setAlwaysOnTop(true);
    }

    public static synchronized void showPic(BufferedImage image, Predicate<String> callBack) {
        if (SupportUtil.callBack != null) throw new RuntimeException("上一个未结束");
        SupportUtil.callBack = callBack;
        EventQueue.invokeLater(() -> {
            if (image != null) {
                icon = new ImageIcon(image);
                timeInput.setBounds(icon.getIconWidth() + 30, 0, R / 8, R / 12);
                root.setVisible(true);
                root.repaint();
            } else {
                root.setVisible(true);
                root.repaint();
            }
        });
    }

    public static JScrollPane scroll(Component view, Container parent, Dimension dimension, Border border) {
        //滚动布局包装
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setPreferredSize(dimension);
        scrollPane.setBorder(border);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //滚动布局ui
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setBorder(border);
        bar.setUI(new SimpleScrollBarUI());
        bar.setUnitIncrement(40);
        parent.add(scrollPane);
        return scrollPane;
    }

    public static void beDragged(JFrame main, JComponent comp) {
        comp.addMouseMotionListener(new MouseMotionAdapter() {
            int xx;
            int yy;
            long last;

            @Override
            public void mouseDragged(final MouseEvent e) {
                long when = e.getWhen();
                if (when - last > 100) {
                    xx = e.getX();
                    yy = e.getY();
                }
                last = when;
                move(main, e.getX() - xx, e.getY() - yy);
            }
        });
    }

    private static void move(final JFrame main, final int dx, final int dy) {
        Point location = main.getLocation();
        main.setLocation(location.x + dx, location.y + dy);
    }
}
