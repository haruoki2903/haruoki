package mainui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ImageButton extends JButton {

    private BufferedImage iconImage;

    public ImageButton(String text, String imagePath) {
        // Chúng ta không đặt text cho nút cha, mà sẽ tự vẽ
        super(); 
        setText(text); 
        
        try {
            // Tải hình ảnh từ classpath
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                this.iconImage = ImageIO.read(imageUrl);
            } else {
                System.err.println("Không tìm thấy hình ảnh: " + imagePath);
                this.iconImage = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải hình ảnh: " + imagePath);
            e.printStackTrace();
            this.iconImage = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Ép kiểu Graphics sang Graphics2D để có nhiều tùy chọn vẽ hơn
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Bật chế độ khử răng cưa cho cả chữ và ảnh
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // --- Vẽ nền ---
        // Lấy màu nền dựa trên trạng thái của nút (có được di chuột vào hay không)
        if (getModel().isRollover()) {
            g2.setColor(getBackground().brighter());
        } else {
            g2.setColor(getBackground());
        }
        g2.fillRect(0, 0, getWidth(), getHeight());

        // --- Vẽ hình ảnh ở bên phải ---
        if (iconImage != null) {
            int padding = 5; // Khoảng cách từ mép
            int imageSize = getHeight() - 2 * padding; // Chiều cao ảnh = chiều cao nút - 2 lần padding
            
            // Duy trì tỷ lệ của ảnh gốc
            int originalWidth = iconImage.getWidth();
            int originalHeight = iconImage.getHeight();
            int newWidth = imageSize;
            int newHeight = (int) (((double) imageSize / originalWidth) * originalHeight);
            
            if (newHeight > imageSize) {
                 newHeight = imageSize;
                 newWidth = (int) (((double) imageSize / originalHeight) * originalWidth);
            }
            
            int x = getWidth() - newWidth - padding;
            int y = (getHeight() - newHeight) / 2;
            
            g2.drawImage(iconImage, x, y, newWidth, newHeight, this);
        }

        // --- Vẽ chữ ở bên trái ---
        g2.setFont(this.getFont());
        g2.setColor(this.getForeground());
        
        FontMetrics fm = g2.getFontMetrics();
        int stringAscent = fm.getAscent();
        
        int xText = 30; // Cách mép trái 30px
        int yText = (getHeight() + stringAscent) / 2 - fm.getDescent();
        g2.drawString(getText(), xText, yText);
        
        g2.dispose();
    }
}