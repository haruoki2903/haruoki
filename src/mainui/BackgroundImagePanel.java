package mainui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class BackgroundImagePanel extends JPanel {

    private BufferedImage backgroundImage;

    public BackgroundImagePanel(String imagePath) {
        try {
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                this.backgroundImage = ImageIO.read(imageUrl);
            } else {
                System.err.println("Không tìm thấy hình nền tại: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            
            // 1. Vẽ ảnh nền gốc trước
            g2d.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            
            // 2. Thiết lập độ mờ cho lớp phủ
            // <<< THAY ĐỔI Ở ĐÂY: Giảm để ảnh nền rõ hơn >>>
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f); 
            g2d.setComposite(ac);
            
            // 3. Vẽ một hình chữ nhật màu nền của ứng dụng lên trên ảnh
            g2d.setColor(getBackground()); 
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.dispose();
        }
    }
}